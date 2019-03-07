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

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.annotation.Id;
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
import org.ws2ten1.repositories.LockableCrudRepository;
import org.ws2ten1.repositories.PageableRepository;
import org.ws2ten1.repositories.ScannableRepository;
import org.ws2ten1.repositories.TruncatableRepository;

import com.miragesql.miragesql.IterationCallback;
import com.miragesql.miragesql.SqlManager;
import com.miragesql.miragesql.SqlResource;
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
public class DefaultMirageRepository<E, ID extends Serializable> implements // NOPMD God class
		ScannableRepository<E, ID>, BatchCreatableRepository<E, ID>, BatchReadableRepository<E, ID>,
		BatchUpsertableRepository<E, ID>, BatchDeletableRepository<E, ID>,
		LockableCrudRepository<E, ID>, TruncatableRepository<E, ID>,
		ChunkableRepository<E, ID>, PageableRepository<E, ID> {
	
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
	
	
	private final SqlManager sqlManager;
	
	private final NameConverter nameConverter; // nullable
	
	private final DataSource dataSource; // nullable
	
	private final List<RepositoryActionListener> handlers;
	
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
	
	@Override
	public long count() {
		return getCount(getBaseSelectSqlResource(), createParams());
	}
	
	@Override
	public <S extends E> S create(S entity) {
		if (entity == null) {
			return null;
		}
		try {
			handlers.forEach(handler -> handler.beforeCreate(entity));
			sqlManager.insertEntity(entity);
			log.debug("entity inserted: {}", entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("create", e);
		}
		return entity;
	}
	
	@Override
	public void delete(E entity) {
		if (entity == null) {
			throw new NullPointerException("entity is null"); // NOPMD
		}
		try {
			sqlManager.deleteEntity(entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("delete", e);
		}
	}
	
	@Override
	public void deleteById(ID id) {
		E found = findById(id).orElse(null);
		if (found != null) {
			try {
				sqlManager.deleteEntity(found);
			} catch (SQLRuntimeException e) {
				throw dataAccessException("delete", e);
			}
		} else {
			log.warn("entity id [{}] not found", id);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void deleteAll(Iterable<? extends E> entities) {
		if (entities == null) {
			throw new NullPointerException("entities is null"); // NOPMD
		}
		for (E entity : entities) {
			if (entity == null) {
				throw new NullPointerException("entity is null"); // NOPMD
			}
		}
		
		try {
			sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("delete", e);
		}
	}
	
	@Override
	public void deleteAll() {
		try {
			deleteAll(findAll());
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteAll", e);
		}
	}
	
	@Override
	public boolean existsById(ID id) {
		return exists(id, false);
	}
	
	@Override
	public boolean exists(ID id, boolean forUpdate) {
		Assert.notNull(id, "id must not be null");
		try {
			return getCount(getBaseSelectSqlResource(), createParams(id, forUpdate)) > 0;
		} catch (SQLRuntimeException e) {
			throw dataAccessException("exists", e);
		}
	}
	
	@Override
	public Iterable<E> findAll() {
		try {
			return getResultList(getBaseSelectSqlResource(), createParams());
		} catch (SQLRuntimeException e) {
			throw dataAccessException("findAll", e);
		}
	}
	
	@Override
	public Chunk<E> findAll(Chunkable chunkable) {
		if (chunkable == null) {
			return new ChunkImpl<E>(newArrayList(findAll()), null, null);
		}
		
		try {
			Map<String, Object> param = createParams(chunkable);
			List<E> resultList = getResultList(getBaseSelectSqlResource(), param);
			String pt = null;
			if (resultList.isEmpty() == false) {
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
			return new ChunkImpl<E>(resultList, pt, chunkable);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("findAll", e);
		}
	}
	
	@Override
	public Iterable<E> findAll(Iterable<ID> ids) {
		Assert.notNull(ids, "ids must not be null");
		
		Map<String, Object> params = createParams();
		params.put("ids", ids);
		try {
			return getResultList(getBaseSelectSqlResource(), params);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("findAll", e);
		}
	}
	
	@Override
	public Page<E> findAll(Pageable pageable) {
		if (null == pageable) {
			return new PageImpl<E>(newArrayList(findAll()));
		}
		
		try {
			List<E> result = getResultList(getBaseSelectSqlResource(), createParams(pageable));
			Long foundRows = getFoundRows();
			return new PageImpl<E>(result, pageable, foundRows != null ? foundRows : count());
		} catch (SQLRuntimeException e) {
			throw dataAccessException("findAll", e);
		}
	}
	
	@Override
	public List<E> findAll(Sort sort) {
		try {
			return getResultList(getBaseSelectSqlResource(), createParams(sort));
		} catch (SQLRuntimeException e) {
			throw dataAccessException("findAll", e);
		}
	}
	
	@Override
	public Optional<E> findById(ID id) {
		return findById(id, false);
	}
	
	@Override
	public Optional<E> findById(ID id, boolean forUpdate) {
		Assert.notNull(id, "id must not be null");
		
		try {
			return Optional.ofNullable(getSingleResult(getBaseSelectSqlResource(), createParams(id, forUpdate)));
		} catch (SQLRuntimeException e) {
			throw dataAccessException("findOne", e);
		}
	}
	
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
	
	@Override
	public <S extends E> Iterable<S> saveAll(Iterable<S> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}
		List<E> toUpdate = new ArrayList<E>();
		List<E> toInsert = new ArrayList<E>();
		Iterator<? extends E> iterator = entities.iterator();
		try {
			while (iterator.hasNext()) {
				E entity = iterator.next();
				if (entity != null) {
					if (exists(getId(entity), true)) {
						toUpdate.add(entity);
					} else {
						toInsert.add(entity);
					}
				}
			}
			
			toUpdate.forEach(e -> handlers.forEach(handler -> handler.beforeUpdate(e)));
			sqlManager.updateBatch(toUpdate);
			toUpdate.forEach(e -> handlers.forEach(handler -> handler.beforeCreate(e)));
			sqlManager.insertBatch(toInsert);
			return newArrayList(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("save", e);
		}
	}
	
	@Override
	public <S extends E> S save(S entity) {
		if (entity == null) {
			return null;
		}
		try {
			if (exists(getId(entity), true)) {
				handlers.forEach(handler -> handler.beforeUpdate(entity));
				sqlManager.updateEntity(entity);
				log.debug("entity updated: {}", entity);
			} else {
				handlers.forEach(handler -> handler.beforeCreate(entity));
				sqlManager.insertEntity(entity);
				log.debug("entity inserted: {}", entity);
			}
		} catch (SQLRuntimeException e) {
			throw dataAccessException("save", e);
		}
		return entity;
	}
	
	public void setBaseSelectSqlResource(SqlResource baseSelectSqlResource) {
		if (baseSelectSqlResource == null) {
			this.baseSelectSqlResource = BASE_SELECT_SQL;
		} else {
			this.baseSelectSqlResource = baseSelectSqlResource;
		}
	}
	
	@Override
	public <S extends E> S update(S entity) {
		if (entity == null) {
			return null;
		}
		try {
			handlers.forEach(handler -> handler.beforeUpdate(entity));
			int rowCount = sqlManager.updateEntity(entity);
			if (rowCount == 1) {
				log.debug("entity updated: {}", entity);
			} else {
				throw new IncorrectResultSizeDataAccessException(1, rowCount);
			}
		} catch (SQLRuntimeException e) {
			throw dataAccessException("update", e);
		}
		return entity;
	}
	
	/**
	 * @see SqlManager#call(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected E call(Class<E> resultClass, String functionName) {
		try {
			return sqlManager.call(resultClass, functionName);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call", e);
		}
	}
	
	/**
	 * @see SqlManager#call(Class, String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected E call(Class<E> resultClass, String functionName, Object param) {
		try {
			return sqlManager.call(resultClass, functionName, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call", e);
		}
	}
	
	/**
	 * @see SqlManager#call(String)
	 */
	@SuppressWarnings("javadoc")
	protected void call(String procedureName) {
		try {
			sqlManager.call(procedureName);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call", e);
		}
	}
	
	/**
	 * @see SqlManager#call(String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected void call(String procedureName, Object parameter) {
		try {
			sqlManager.call(procedureName, parameter);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("call", e);
		}
	}
	
	/**
	 * @see SqlManager#callForList(Class, String)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> callForList(Class<E> resultClass, String functionName) {
		try {
			return sqlManager.callForList(resultClass, functionName);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("callForList", e);
		}
	}
	
	/**
	 * @see SqlManager#callForList(Class, String, Object)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> callForList(Class<E> resultClass, String functionName, Object param) {
		try {
			return sqlManager.callForList(resultClass, functionName, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("callForList", e);
		}
	}
	
	protected Map<String, Object> createParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", MirageUtil.getTableName(entityClass, nameConverter));
		params.put("id", null); // 何故これが要るのだろう。無いとコケる
		params.put("id_column_name", findIdColumnName());
		
		return params;
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @param chunkable
	 * @return
	 * @since 0.1
	 */
	protected Map<String, Object> createParams(Chunkable chunkable) {
		Map<String, Object> params = createParams();
		addChunkParam(params, chunkable);
		return params;
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @param id
	 * @param forUpdate
	 * @return
	 * @since 0.1
	 */
	protected Map<String, Object> createParams(ID id, boolean forUpdate) {
		Map<String, Object> params = createParams();
		addIdParam(params, id);
		params.put("forUpdate", forUpdate);
		return params;
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @param pageable
	 * @return
	 * @since 0.1
	 */
	protected Map<String, Object> createParams(Pageable pageable) {
		Map<String, Object> params = createParams();
		addPageParam(params, pageable);
		return params;
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @param sort
	 * @return
	 * @since 0.1
	 */
	protected Map<String, Object> createParams(Sort sort) {
		Map<String, Object> params = createParams();
		addSortParam(params, sort);
		return params;
	}
	
	/**
	 * @see SqlManager#deleteBatch(Object...)
	 */
	@SuppressWarnings({
		"javadoc",
		"unchecked"
	})
	protected int deleteBatch(E... entities) {
		try {
			return sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteBatch", e);
		}
	}
	
	/**
	 * @see SqlManager#deleteBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int deleteBatch(List<E> entities) {
		try {
			return sqlManager.deleteBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteBatch", e);
		}
	}
	
	/**
	 * @see SqlManager#deleteEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int deleteEntity(Object entity) {
		try {
			return sqlManager.deleteEntity(entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("deleteEntity", e);
		}
	}
	
	/**
	 * @see SqlManager#executeUpdate(SqlResource)
	 */
	@SuppressWarnings("javadoc")
	protected int executeUpdate(SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.executeUpdate(resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("executeUpdate", e);
		}
	}
	
	/**
	 * @see SqlManager#executeUpdate(SqlResource, Object)
	 */
	@SuppressWarnings("javadoc")
	protected int executeUpdate(SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.executeUpdate(resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("executeUpdate", e);
		}
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @return
	 * @since 0.1
	 */
	protected SqlResource getBaseSelectSqlResource() {
		return baseSelectSqlResource;
	}
	
	/**
	 * @see SqlManager#getCount(SqlResource)
	 */
	@SuppressWarnings("javadoc")
	protected int getCount(SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getCount(resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getCount", e);
		}
	}
	
	/**
	 * @see SqlManager#getCount(SqlResource, Object)
	 */
	@SuppressWarnings("javadoc")
	protected int getCount(SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getCount(resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getCount", e);
		}
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @return
	 */
	protected synchronized SQLExceptionTranslator getExceptionTranslator() { // NOPMD
		if (this.exceptionTranslator == null) {
			if (dataSource != null) {
				this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			} else {
				this.exceptionTranslator = new SQLStateSQLExceptionTranslator();
			}
		}
		return this.exceptionTranslator;
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @return
	 */
	protected Long getFoundRows() {
		return null;
	}
	
	/**
	 * @see SqlManager#getResultList(Class, SqlResource)
	 */
	@SuppressWarnings("javadoc")
	protected List<E> getResultList(SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getResultList(entityClass, resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getResultList", e);
		}
	}
	
	/**
	 * @see SqlManager#getResultList(Class, SqlResource, Object)
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	@SuppressWarnings("javadoc")
	protected List<E> getResultList(SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getResultList(entityClass, resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getResultList", e);
		}
	}
	
	/**
	 * @see SqlManager#getSingleResult(Class, SqlResource)
	 */
	@SuppressWarnings("javadoc")
	protected E getSingleResult(SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getSingleResult(entityClass, resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getSingleResult", e);
		}
	}
	
	/**
	 * @see SqlManager#getSingleResult(Class, SqlResource, Object)
	 */
	@SuppressWarnings("javadoc")
	protected E getSingleResult(SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.getSingleResult(entityClass, resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("getSingleResult", e);
		}
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @return
	 * @since 0.1
	 */
	protected SqlManager getSqlManager() {
		return sqlManager;
	}
	
	/**
	 * @see SqlManager#insertBatch(Object...)
	 */
	@SuppressWarnings({
		"javadoc",
		"unchecked"
	})
	protected int insertBatch(E... entities) {
		try {
			Arrays.stream(entities).forEach(e -> handlers.forEach(handler -> handler.beforeCreate(e)));
			return sqlManager.insertBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertBatch", e);
		}
	}
	
	/**
	 * @see SqlManager#insertBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int insertBatch(List<E> entities) {
		try {
			entities.forEach(e -> handlers.forEach(handler -> handler.beforeCreate(e)));
			return sqlManager.insertBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertBatch", e);
		}
	}
	
	@Override
	public <S extends E> Iterable<S> createAll(Iterable<S> entities) {
		try {
			List<S> list = newArrayList(entities);
			list.forEach(e -> handlers.forEach(handler -> handler.beforeCreate(e)));
			sqlManager.insertBatch(list);
			return list;
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertBatch", e);
		}
	}
	
	@Override
	public Iterable<E> deleteAllById(Iterable<ID> ids) {
		return null;
	}
	
	/**
	 * @see SqlManager#insertEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int insertEntity(Object entity) {
		try {
			// handlers.forEach(handler -> handler.processBeforeInsert(entity));
			return sqlManager.insertEntity(entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("insertEntity", e);
		}
	}
	
	/**
	 * @see SqlManager#iterate(Class, IterationCallback, SqlResource)
	 */
	@SuppressWarnings("javadoc")
	protected <R> R iterate(IterationCallback<E, R> callback, SqlResource resource) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.iterate(entityClass, callback, resource);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("iterate", e);
		}
	}
	
	/**
	 * @see SqlManager#iterate(Class, IterationCallback, SqlResource, Object)
	 */
	@SuppressWarnings("javadoc")
	protected <R> R iterate(IterationCallback<E, R> callback, SqlResource resource, Object param) {
		Assert.notNull(resource, "resource is required");
		try {
			return sqlManager.iterate(entityClass, callback, resource, param);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("iterate", e);
		}
	}
	
	/**
	 * @see SqlManager#updateBatch(Object...)
	 */
	@SuppressWarnings({
		"javadoc",
		"unchecked"
	})
	protected int updateBatch(E... entities) {
		try {
			Arrays.stream(entities).forEach(e -> handlers.forEach(handler -> handler.beforeUpdate(e)));
			return sqlManager.updateBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("updateBatch", e);
		}
	}
	
	/**
	 * @see SqlManager#updateBatch(List)
	 */
	@SuppressWarnings("javadoc")
	protected int updateBatch(List<E> entities) {
		try {
			entities.forEach(e -> handlers.forEach(handler -> handler.beforeUpdate(e)));
			return sqlManager.updateBatch(entities);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("updateBatch", e);
		}
	}
	
	/**
	 * @see SqlManager#updateEntity(Object)
	 */
	@SuppressWarnings("javadoc")
	protected int updateEntity(E entity) {
		try {
			handlers.forEach(handler -> handler.beforeUpdate(entity));
			return sqlManager.updateEntity(entity);
		} catch (SQLRuntimeException e) {
			throw dataAccessException("updateEntity", e);
		}
	}
	
	private void addChunkParam(Map<String, Object> params, Chunkable chunkable) {
		if (chunkable == null) {
			return;
		}
		boolean ascending = isAscending(chunkable);
		boolean forward = isForward(chunkable);
		log.debug("Chunk param for {} {}", ascending ? "ascending" : "descending", forward ? "forward" : "backword");
		
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
		return Optional.ofNullable(chunkable.getDirection()).orElse(Direction.ASC) == Direction.ASC;
	}
	
	private boolean isForward(Chunkable chunkable) {
		return Optional.ofNullable(chunkable.getPaginationRelation())
			.orElse(PaginationRelation.NEXT) == PaginationRelation.NEXT;
	}
	
	private DataAccessException dataAccessException(String task, SQLRuntimeException e) {
		return getExceptionTranslator().translate(task, null, e.getCause());
	}
}
