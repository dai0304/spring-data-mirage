/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.xet.springframework.data.mirage.repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.util.Assert;

import org.ws2ten1.chunks.Chunk;
import org.ws2ten1.chunks.ChunkImpl;
import org.ws2ten1.chunks.Chunkable;
import org.ws2ten1.chunks.Chunkable.PaginationRelation;
import org.ws2ten1.chunks.PaginationTokenEncoder;
import org.ws2ten1.chunks.SimplePaginationTokenEncoder;
import org.ws2ten1.repositories.BatchCreatableRepository;
import org.ws2ten1.repositories.BatchDeletableRepository;
import org.ws2ten1.repositories.BatchReadableRepository;
import org.ws2ten1.repositories.BatchUpsertableRepository;
import org.ws2ten1.repositories.ChunkableRepository;
import org.ws2ten1.repositories.ConditionalDeletableRepository;
import org.ws2ten1.repositories.ConditionalUpdatableRepository;
import org.ws2ten1.repositories.CrudRepository;
import org.ws2ten1.repositories.LockableCrudRepository;
import org.ws2ten1.repositories.PageableRepository;
import org.ws2ten1.repositories.ScannableRepository;
import org.ws2ten1.repositories.TruncatableRepository;

import com.google.common.collect.Lists;
import com.miragesql.miragesql.IterationCallback;
import com.miragesql.miragesql.SqlManager;
import com.miragesql.miragesql.SqlResource;
import com.miragesql.miragesql.StringSqlResource;
import com.miragesql.miragesql.annotation.Column;
import com.miragesql.miragesql.exception.SQLRuntimeException;
import com.miragesql.miragesql.naming.NameConverter;
import com.miragesql.miragesql.util.MirageUtil;
import com.miragesql.miragesql.util.Validate;

import jp.xet.springframework.data.mirage.repository.handler.RepositoryActionListener;

/**
 * Default {@link org.springframework.data.repository.Repository} implementation using Mirage SQL.
 *
 * @param <E> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
@Slf4j // -@cs[MethodCount|ClassFanOutComplexity]
public class DefaultMirageRepository<E, ID extends Serializable, C> implements // NOPMD God class
		ScannableRepository<E, ID>, CrudRepository<E, ID>, LockableCrudRepository<E, ID>,
		BatchCreatableRepository<E, ID>, BatchReadableRepository<E, ID>,
		BatchDeletableRepository<E, ID>, BatchUpsertableRepository<E, ID>,
		TruncatableRepository<E, ID>, ChunkableRepository<E, ID>, PageableRepository<E, ID>,
		ConditionalUpdatableRepository<E, ID, C>, ConditionalDeletableRepository<E, ID, C> {
	
	static final SqlResource BASE_SELECT_SQL =
			new ScopeClasspathSqlResource(DefaultMirageRepository.class, "baseSelect.sql");
	
	
	/**
	 * 新しい {@link SqlResource} を生成する。
	 *
	 * @param scope クラスパス上のSQLの位置を表すクラス。無名パッケージの場合は{@code null}
	 * @param filename クラスパス上のSQLファイル名
	 * @return {@link SqlResource}
	 * @throws NoSuchSqlResourceException 指定したリソースが見つからない場合
	 * @throws IllegalArgumentException 引数{@code filename}に{@code null}を与えた場合
	 */
	public static SqlResource newSqlResource(Class<?> scope, String filename) {
		Validate.notNull(filename);
		return new ScopeClasspathSqlResource(scope, filename);
	}
	
	private static String join(List<String> orders) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> parts = orders.iterator();
		if (parts.hasNext()) {
			sb.append(parts.next());
			while (parts.hasNext()) {
				sb.append(", ");
				sb.append(parts.next());
			}
		}
		return sb.toString();
	}
	
	private static <E> List<E> newArrayList(Iterable<E> iterable) {
		List<E> list = new ArrayList<>();
		for (E element : iterable) {
			list.add(element);
		}
		return list;
	}
	
	
	@Getter(AccessLevel.PROTECTED)
	private final SqlManager sqlManager;
	
	private final NameConverter nameConverter; // nullable
	
	private final DataSource dataSource; // nullable
	
	private final List<RepositoryActionListener> handlers;
	
	@Getter(AccessLevel.PROTECTED)
	private SqlResource baseSelectSqlResource = BASE_SELECT_SQL;
	
	private transient SQLExceptionTranslator exceptionTranslator;
	
	private final Class<E> entityClass;
	
	private PaginationTokenEncoder encoder = new SimplePaginationTokenEncoder();
	
	
	/**
	 * インスタンスを生成する。
	 *
	 * @param entityInformation {@link EntityInformation}
	 * @param sqlManager {@link SqlManager}
	 */
	public DefaultMirageRepository(EntityInformation<E, ? extends Serializable> entityInformation,
			SqlManager sqlManager, NameConverter nameConverter,
			DataSource dataSource, List<RepositoryActionListener> handlers) {
		Assert.notNull(entityInformation, "entityInformation is required");
		this.entityClass = entityInformation.getJavaType();
		this.sqlManager = sqlManager;
		this.handlers = handlers;
		this.nameConverter = nameConverter;
		this.dataSource = dataSource;
	}
	
	public void setBaseSelectSqlResource(SqlResource baseSelectSqlResource) {
		if (baseSelectSqlResource == null) {
			this.baseSelectSqlResource = BASE_SELECT_SQL;
		} else {
			this.baseSelectSqlResource = baseSelectSqlResource;
		}
	}
	
	// BaseRepository
	
	@Override
	@SuppressWarnings("unchecked")
	public ID getId(E entity) {
		Class<?> c = entityClass;
		while (c != null && c != Object.class) {
			Field[] declaredFields = c.getDeclaredFields();
			for (Field field : declaredFields) {
				Id idAnnotation = field.getAnnotation(Id.class);
				if (idAnnotation != null) {
					field.setAccessible(true);
					try {
						return (ID) field.get(entity);
					} catch (Exception e) { // NOPMD
						// ignore
						log.debug("failed", e);
					}
				}
			}
			c = c.getSuperclass();
		}
		return null;
	}
	
	// ScannableRepository
	
	@Override
	public Iterable<E> findAll() {
		return getResultList("findAll", getBaseSelectSqlResource(), createParams());
	}
	
	@Override
	public List<E> findAll(Sort sort) {
		return getResultList("findAll", getBaseSelectSqlResource(), createParams(sort));
	}
	
	@Override
	public long count() {
		return getCount("count", getBaseSelectSqlResource(), createParams());
	}
	
	// CreatableRepository
	
	@Override
	public <S extends E> S create(S entity) {
		if (entity == null) {
			return null;
		}
		insertEntity("create", entity);
		return entity;
	}
	
	// DeletableRepository
	
	@Override
	public void deleteById(ID id) {
		Map<String, Object> params = createParams(id, false);
		int rows = executeUpdate("deleteById", new StringSqlResource(
				"DELETE FROM /*$table*/example_table WHERE /*$id_column_name*/id = /*id*/'aa'"), params);
		if (rows == 0) {
			log.warn("entity id [{}] not found", id);
		} else if (rows > 1) {
			throw new AssertionError(rows + " rows deleted by primary key!?");
		}
	}
	
	@Override
	public void delete(E entity) {
		if (entity == null) {
			throw new NullPointerException("entity is null"); // NOPMD
		}
		deleteEntity("delete", entity);
	}
	
	// BatchDeletableRepository
	
	@Override
	public Iterable<E> deleteAllById(Iterable<ID> ids) {
		Iterable<E> found = findAll(ids);
		deleteAll(found);
		return found;
	}
	
	@Override
	public void deleteAll(Iterable<? extends E> entities) {
		if (entities == null) {
			throw new NullPointerException("entities is null"); // NOPMD
		}
		for (E entity : entities) {
			if (entity == null) {
				throw new NullPointerException("entity is null"); // NOPMD
			}
		}
		
		String tableName = MirageUtil.getTableName(entityClass, nameConverter);
		List<? extends E> entityList = Lists.newArrayList(entities);
		deleteBatch("deleteAll", tableName, entityList);
	}
	
	// TruncatableRepository
	
	@Override
	public void deleteAll() {
		executeUpdate("deleteAll", new StringSqlResource("TRUNCATE TABLE /*$table*/example_table"), createParams());
	}
	
	// ReadableRepository
	
	@Override
	public Optional<E> findById(ID id) {
		return findById(id, false);
	}
	
	@Override
	public boolean existsById(ID id) {
		return exists(id, false);
	}
	
	// LockableCrudRepository
	
	@Override
	public Optional<E> findById(ID id, boolean forUpdate) {
		Assert.notNull(id, "id must not be null");
		Map<String, Object> param = createParams(id, forUpdate);
		return Optional.ofNullable(getSingleResult("findById", getBaseSelectSqlResource(), param));
	}
	
	@Override
	public boolean exists(ID id, boolean forUpdate) {
		Assert.notNull(id, "id must not be null");
		return getCount("exists", getBaseSelectSqlResource(), createParams(id, forUpdate)) > 0;
	}
	
	// ChunkableRepository
	
	@Override
	public Chunk<E> findAll(Chunkable chunkable) {
		if (chunkable == null) {
			return new ChunkImpl<>(newArrayList(findAll()), null, null);
		}
		
		Map<String, Object> param = createParams(chunkable);
		List<E> resultList = getResultList("findAll", getBaseSelectSqlResource(), param);
		if (chunkable.getPaginationToken() != null && isForward(chunkable) == false
				&& param.get("before") != null) {
			Collections.reverse(resultList);
		}
		
		String pt;
		if (resultList.isEmpty()) {
			pt = encoder.encode(null, null);
		} else {
			String firstKey = null;
			if (chunkable.getPaginationToken() != null && resultList.isEmpty() == false) {
				firstKey = Objects.toString(getId(resultList.get(0)));
			}
			String lastKey = null;
			if (resultList.isEmpty() == false) {
				lastKey = Objects.toString(getId(resultList.get(resultList.size() - 1)));
			}
			pt = encoder.encode(firstKey, lastKey);
		}
		return new ChunkImpl<>(resultList, pt, chunkable);
	}
	
	// BatchReadableRepository
	
	@Override
	public Iterable<E> findAll(Iterable<ID> ids) {
		Assert.notNull(ids, "ids must not be null");
		if (ids.iterator().hasNext() == false) {
			return Collections.emptySet();
		}
		
		Map<String, Object> params = createParams();
		params.put("ids", ids);
		return getResultList("findAll", getBaseSelectSqlResource(), params);
	}
	
	// PageableRepository
	
	@Override
	public Page<E> findAll(Pageable pageable) {
		if (pageable == null || pageable == Pageable.unpaged()) {
			return new PageImpl<>(newArrayList(findAll()));
		}
		
		List<E> result = getResultList("findAll", getBaseSelectSqlResource(), createParams(pageable));
		Long foundRows = getFoundRows();
		return new PageImpl<>(result, pageable, foundRows != null ? foundRows : count());
	}
	
	// BatchUpsertableRepository
	
	@Override
	public <S extends E> Iterable<S> saveAll(Iterable<S> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		List<E> toUpdate = new ArrayList<>();
		List<E> toInsert = new ArrayList<>();
		for (S entity : entities) {
			if (entity != null) {
				if (exists(getId(entity), true)) {
					toUpdate.add(entity);
				} else {
					toInsert.add(entity);
				}
			}
		}
		
		updateBatch("saveAll", toUpdate);
		insertBatch("saveAll", toInsert);
		return newArrayList(entities);
	}
	
	// UpsertableRepository
	
	@Override
	public <S extends E> S save(S entity) {
		if (entity == null) {
			return null;
		}
		try {
			if (exists(getId(entity), true)) {
				updateEntity("save", entity);
				log.debug("entity updated: {}", entity);
			} else {
				insertEntity("save", entity);
				log.debug("entity inserted: {}", entity);
			}
		} catch (SQLRuntimeException e) {
			throw dataAccessException("save", e);
		}
		return entity;
	}
	
	// UpdatableRepository
	
	@Override
	public <S extends E> S update(S entity) {
		if (entity == null) {
			return null;
		}
		int rowCount = updateEntity("update", entity);
		if (rowCount == 1) {
			log.debug("entity updated: {}", entity);
		} else {
			throw new IncorrectResultSizeDataAccessException(1, rowCount);
		}
		return entity;
	}
	
	// BatchCreatableRepository
	
	@Override
	public <S extends E> Iterable<S> createAll(Iterable<S> entities) {
		try {
			insertBatch("createAll", newArrayList(entities));
			return entities;
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertBatch", e);
		}
	}
	
	// ConditionalRepository
	
	@Override
	@SuppressWarnings("unchecked")
	public C getCondition(E entity) {
		Class<?> c = entityClass;
		while (c != null && c != Object.class) {
			Field[] declaredFields = c.getDeclaredFields();
			for (Field field : declaredFields) {
				Version versionAnnotation = field.getAnnotation(Version.class);
				if (versionAnnotation != null) {
					field.setAccessible(true);
					try {
						return (C) field.get(entity);
					} catch (Exception e) { // NOPMD
						// ignore
						log.debug("failed", e);
					}
				}
			}
			c = c.getSuperclass();
		}
		return null;
	}
	// ConditionalUpdatableRepository
	
	@Override
	public <S extends E> S update(S entity, C condition) {
		if (entity == null) {
			return null;
		}
		try {
			E found = findById(getId(entity), true)
				.orElseThrow(() -> new IncorrectResultSizeDataAccessException(1, 0));
			C actualCondition = getCondition(found);
			if (condition == null || condition.equals(actualCondition)) {
				return update(entity);
			} else {
				String message =
						String.format(Locale.ENGLISH, "expected is %s, but actual is %s", condition, actualCondition);
				throw new OptimisticLockingFailureException(message);
			}
		} catch (SQLRuntimeException e) {
			throw dataAccessException("update", e);
		}
	}
	
	// ConditionalDeletableRepository
	
	@Override
	public void deleteById(ID id, C condition) {
		if (id == null) {
			return;
		}
		try {
			E found = findById(id, true)
				.orElseThrow(() -> new IncorrectResultSizeDataAccessException(1, 0));
			C actualCondition = getCondition(found);
			if (condition == null || condition.equals(actualCondition)) {
				deleteById(id);
			} else {
				String message =
						String.format(Locale.ENGLISH, "expected is %s, but actual is %s", condition, actualCondition);
				throw new OptimisticLockingFailureException(message);
			}
		} catch (SQLRuntimeException e) {
			throw dataAccessException("update", e);
		}
	}
	
	@Override
	public void delete(E entity, C condition) {
		if (entity == null) {
			return;
		}
		deleteById(getId(entity), condition);
	}
	
	// delegate methods to sqlManager
	
	/**
	 * @see SqlManager#call(Class, String)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected E call(String task, Class<E> resultClass, String functionName) {
		try {
			return sqlManager.call(resultClass, functionName);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#call(Class, String, Object)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected E call(String task, Class<E> resultClass, String functionName, Object param) {
		try {
			return sqlManager.call(resultClass, functionName, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#call(String)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected void call(String task, String procedureName) {
		try {
			sqlManager.call(procedureName);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#call(String, Object)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected void call(String task, String procedureName, Object parameter) {
		try {
			sqlManager.call(procedureName, parameter);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#callForList(Class, String)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected List<E> callForList(String task, Class<E> resultClass, String functionName) {
		try {
			return sqlManager.callForList(resultClass, functionName);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("callForList-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#callForList(Class, String, Object)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected List<E> callForList(String task, Class<E> resultClass, String functionName, Object param) {
		try {
			return sqlManager.callForList(resultClass, functionName, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("callForList-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#deleteBatch(Object...)
	 */
	@SuppressWarnings({
		"javadoc",
		"unchecked",
		"unused"
	})
	protected int deleteBatch(String task, E... entities) {
		try {
			return sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#deleteBatch(String, Object...)
	 */
	@SuppressWarnings({
		"javadoc",
		"unchecked",
		"unused"
	})
	protected int deleteBatch(String task, String entityName, E... entities) {
		try {
			return sqlManager.deleteBatch(entityName, entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#deleteBatch(List)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected int deleteBatch(String task, List<? extends E> entities) {
		try {
			return sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#deleteBatch(String, List)
	 */
	@SuppressWarnings("javadoc")
	protected int deleteBatch(String task, String entityName, List<? extends E> entities) {
		try {
			return sqlManager.deleteBatch(entityName, entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#deleteEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int deleteEntity(String task, Object entity) {
		try {
			return sqlManager.deleteEntity(entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteEntity-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#executeUpdate(SqlResource)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected int executeUpdate(String task, SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.executeUpdate(resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("executeUpdate-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#executeUpdate(SqlResource, Object)
	 */
	@SuppressWarnings("javadoc")
	protected int executeUpdate(String task, SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.executeUpdate(resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("executeUpdate- " + task, e);
		}
	}
	
	/**
	 * @see SqlManager#getCount(SqlResource)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected int getCount(String task, SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getCount(resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getCount-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#getCount(SqlResource, Object)
	 */
	@SuppressWarnings("javadoc")
	protected int getCount(String task, SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getCount(resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getCount-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#getResultList(Class, SqlResource)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected List<E> getResultList(String task, SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getResultList(entityClass, resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getResultList-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#getResultList(Class, SqlResource, Object)
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	@SuppressWarnings("javadoc")
	protected List<E> getResultList(String task, SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getResultList(entityClass, resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getResultList-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#getSingleResult(Class, SqlResource)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected E getSingleResult(String task, SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getSingleResult(entityClass, resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getSingleResult-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#getSingleResult(Class, SqlResource, Object)
	 */
	@SuppressWarnings("javadoc")
	protected E getSingleResult(String task, SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getSingleResult(entityClass, resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getSingleResult-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#insertBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int insertBatch(String task, List<? extends E> entities) {
		try {
			entities.forEach(e -> handlers.forEach(handler -> handler.beforeCreate(e)));
			return sqlManager.insertBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#insertBatch(Object...)
	 */
	@SuppressWarnings({
		"javadoc",
		"unchecked"
	})
	protected int insertBatch(String task, E... entities) {
		try {
			Arrays.stream(entities).forEach(e -> handlers.forEach(handler -> handler.beforeCreate(e)));
			return sqlManager.insertBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#insertEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int insertEntity(String task, Object entity) {
		try {
			handlers.forEach(handler -> handler.beforeCreate(entity));
			return sqlManager.insertEntity(entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertEntity-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#iterate(Class, IterationCallback, SqlResource)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected <R> R iterate(String task, IterationCallback<E, R> callback, SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.iterate(entityClass, callback, resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("iterate-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#iterate(Class, IterationCallback, SqlResource, Object)
	 */
	@SuppressWarnings({
		"javadoc",
		"unused"
	})
	protected <R> R iterate(String task, IterationCallback<E, R> callback, SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.iterate(entityClass, callback, resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("iterate-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#updateBatch(Object...)
	 */
	@SuppressWarnings({
		"javadoc",
		"unchecked",
		"unused"
	})
	protected int updateBatch(String task, E... entities) {
		try {
			Arrays.stream(entities).forEach(e -> handlers.forEach(handler -> handler.beforeUpdate(e)));
			return sqlManager.updateBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("updateBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#updateBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int updateBatch(String task, List<? extends E> entities) {
		try {
			entities.forEach(e -> handlers.forEach(handler -> handler.beforeUpdate(e)));
			return sqlManager.updateBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("updateBatch-" + task, e);
		}
	}
	
	/**
	 * @see SqlManager#updateEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int updateEntity(String task, E entity) {
		try {
			handlers.forEach(handler -> handler.beforeUpdate(entity));
			return sqlManager.updateEntity(entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("updateEntity-" + task, e);
		}
	}
	
	// parameters
	
	private Map<String, Object> createParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("table", MirageUtil.getTableName(entityClass, nameConverter));
		params.put("id", null); // 何故これが要るのだろう。無いとコケる
		params.put("id_column_name", findIdColumnName());
		
		return params;
	}
	
	private Map<String, Object> createParams(Chunkable chunkable) {
		Map<String, Object> params = createParams();
		addChunkParam(params, chunkable);
		return params;
	}
	
	private Map<String, Object> createParams(ID id, boolean forUpdate) {
		Map<String, Object> params = createParams();
		addIdParam(params, id);
		params.put("forUpdate", forUpdate);
		return params;
	}
	
	private Map<String, Object> createParams(Pageable pageable) {
		Map<String, Object> params = createParams();
		addPageParam(params, pageable);
		return params;
	}
	
	private Map<String, Object> createParams(Sort sort) {
		Map<String, Object> params = createParams();
		addSortParam(params, sort);
		return params;
	}
	
	private void addChunkParam(Map<String, Object> params, Chunkable chunkable) {
		if (chunkable == null) {
			return;
		}
		boolean ascending = isAscending(chunkable);
		boolean forward = isForward(chunkable);
		log.debug("Chunk param for {} {}", ascending ? "ascending" : "descending", forward ? "forward" : "backward");
		
		if (chunkable.getPaginationToken() != null) {
			String key;
			
			if (forward) {
				key = encoder.extractLastKey(chunkable.getPaginationToken()).orElse(null);
				params.put("after", key);
				log.debug("Using last key as after: {}", key);
			} else {
				key = encoder.extractFirstKey(chunkable.getPaginationToken()).orElse(null);
				params.put("before", key);
				log.debug("Using first key as before: {}", key);
			}
		}
		
		params.put("size", chunkable.getMaxPageSize());
		if (ascending == false) {
			params.put("direction", chunkable.getDirection().name());
		}
	}
	
	private void addIdParam(Map<String, Object> params, ID id) {
		params.put("id", id);
	}
	
	private void addPageParam(Map<String, Object> params, Pageable pageable) {
		params.put("offset", pageable == null ? null : pageable.getOffset());
		params.put("size", pageable == null ? null : pageable.getPageSize());
		if (pageable != null) {
			List<String> orders = new ArrayList<String>();
			Sort sort = pageable.getSort();
			for (Order order : sort) {
				orders.add(String.format(Locale.ENGLISH, "%s %s", order.getProperty(), order.getDirection().name()));
			}
			if (orders.isEmpty() == false) {
				params.put("orders", join(orders));
			}
		}
	}
	
	private void addSortParam(Map<String, Object> params, Sort sort) {
		params.put("orders", null);
		if (sort == null) {
			return;
		}
		List<String> list = new ArrayList<String>();
		for (Order order : sort) {
			String orderDefinition =
					String.format(Locale.ENGLISH, "%s %s", order.getProperty(), order.getDirection()).trim();
			if (orderDefinition.isEmpty() == false) {
				list.add(orderDefinition);
			}
		}
		if (list.isEmpty() == false) {
			params.put("orders", join(list));
		}
	}
	
	// others
	
	private Long getFoundRows() {
		return null; // TODO
	}
	
	private String findIdColumnName() {
		Class<?> c = entityClass;
		while (c != null && c != Object.class) {
			Field[] declaredFields = c.getDeclaredFields();
			for (Field field : declaredFields) {
				Id idAnnotation = field.getAnnotation(Id.class);
				if (idAnnotation != null) {
					Column columnAnnotation = field.getAnnotation(Column.class);
					return columnAnnotation.name();
				}
			}
			c = c.getSuperclass();
		}
		return null;
	}
	
	private boolean isAscending(Chunkable chunkable) {
		return Optional.ofNullable(chunkable.getDirection())
			.map(Direction.ASC::equals)
			.orElse(true);
	}
	
	private boolean isForward(Chunkable chunkable) {
		return Optional.ofNullable(chunkable.getPaginationRelation())
			.map(PaginationRelation.NEXT::equals)
			.orElse(true);
	}
	
	private synchronized SQLExceptionTranslator getExceptionTranslator() { // NOPMD
		if (this.exceptionTranslator == null) {
			if (dataSource != null) {
				this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			} else {
				this.exceptionTranslator = new SQLStateSQLExceptionTranslator();
			}
		}
		return this.exceptionTranslator;
	}
	
	private DataAccessException dataAccessException(String task, SQLRuntimeException e) {
		return getExceptionTranslator().translate(task, null, e.getCause());
	}
}
