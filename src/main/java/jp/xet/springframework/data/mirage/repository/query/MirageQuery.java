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
package jp.xet.springframework.data.mirage.repository.query;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import org.polycreo.chunkrequests.Chunkable;
import org.polycreo.chunkrequests.Chunkable.PaginationRelation;
import org.polycreo.chunkrequests.Direction;
import org.polycreo.chunkrequests.PaginationTokenEncoder;

import com.miragesql.miragesql.SqlManager;
import com.miragesql.miragesql.SqlResource;
import com.miragesql.miragesql.StringSqlResource;

import jp.xet.springframework.data.mirage.repository.ScopeClasspathSqlResource;
import jp.xet.springframework.data.mirage.repository.SqlResourceCandidate;

/**
 * {@link RepositoryQuery} implementation for spring-data-mirage.
 */
@Slf4j
public class MirageQuery implements RepositoryQuery { // NOPMD God class
	
	private static final int BUFFER_SIZE = 1024 * 4;
	
	private final SqlResource sqlResource;
	
	
	static String getArgsPartOfSignature(Method method) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append('(');
			Class<?>[] params = method.getParameterTypes(); // avoid clone
			for (int j = 0; j < params.length; j++) {
				sb.append(getTypeName(params[j]));
				if (j < (params.length - 1)) {
					sb.append(',');
				}
			}
			sb.append(')');
			return sb.toString();
		} catch (Exception e) { // NOPMD
			return "<" + e + ">";
		}
	}
	
	private static void addPageParam(Map<String, Object> params, Pageable pageable) {
		if (pageable == null) {
			return;
		}
		params.put("offset", pageable.getOffset());
		params.put("size", pageable.getPageSize());
		addSortParam(params, pageable.getSort());
	}
	
	private static void addSortParam(Map<String, Object> params, Sort sort) {
		if (sort == null) {
			return;
		}
		List<String> orders = new ArrayList<String>();
		for (Order order : sort) {
			orders.add(String.format(Locale.ENGLISH, "%s %s", order.getProperty(), order.getDirection().name()));
		}
		if (orders.isEmpty() == false) {
			params.put("orders", String.join(", ", orders));
		}
	}
	
	private static String getTypeName(Class<?> type) {
		if (type.isArray()) {
			try {
				Class<?> cl = type;
				int dimensions = 0;
				while (cl.isArray()) {
					dimensions++;
					cl = cl.getComponentType();
				}
				StringBuilder sb = new StringBuilder();
				sb.append(cl.getName());
				for (int i = 0; i < dimensions; i++) {
					sb.append("[]");
				}
				return sb.toString();
			} catch (Throwable e) { // NOPMD
				// NOPMD fall through
			}
		}
		return type.getName();
	}
	
	
	private final MirageQueryMethod mirageQueryMethod;
	
	private final SqlManager sqlManager;
	
	private final PaginationTokenEncoder encoder;
	
	
	/**
	 * インスタンスを生成する。
	 *
	 * @param mirageQueryMethod {@link MirageQueryMethod}
	 * @param sqlManager {@link SqlManager}
	 * @throws IllegalArgumentException if the argument is {@code null}
	 */
	public MirageQuery(MirageQueryMethod mirageQueryMethod, SqlManager sqlManager, PaginationTokenEncoder encoder) {
		Assert.notNull(mirageQueryMethod, "MirageQueryMethod must not to be null");
		Assert.notNull(sqlManager, "SqlManager must not to be null");
		this.mirageQueryMethod = mirageQueryMethod;
		this.sqlManager = sqlManager;
		this.encoder = encoder;
		sqlResource = createSqlResource();
	}
	
	@Override
	public Object execute(Object[] parameters) {
		Map<String, Object> parameterMap = createParameterMap(parameters);
		
		Class<?> returnedDomainType = mirageQueryMethod.getReturnedObjectType();
		ChunkableParameterAccessor accessor =
				new ParameterChunkableParameterAccessor(mirageQueryMethod.getParameters(), parameters);
		
		if (mirageQueryMethod.isModifyingQuery()) {
			return sqlManager.executeUpdate(sqlResource, parameterMap);
		} else if (mirageQueryMethod.isCollectionQuery()) {
			addSortParam(parameterMap, accessor.getSort());
			return sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
		} else if (mirageQueryMethod.isChunkQuery()) {
			return processChunkQuery(sqlResource, parameterMap, returnedDomainType, accessor);
		} else if (mirageQueryMethod.isSliceQuery()) {
			return processSliceQuery(sqlResource, parameterMap, returnedDomainType, accessor);
		} else if (mirageQueryMethod.isPageQuery()) {
			return processPageQuery(sqlResource, parameterMap, returnedDomainType, accessor);
		} else {
			return sqlManager.getSingleResult(returnedDomainType, sqlResource, parameterMap);
		}
	}
	
	@Override
	public QueryMethod getQueryMethod() {
		return mirageQueryMethod;
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
	
	private Map<String, Object> createParameterMap(Object... parameters) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("orders", null);
		for (Parameter p : mirageQueryMethod.getParameters()) {
			p.getName().ifPresent(parameterName -> parameterMap.put(parameterName, parameters[p.getIndex()]));
			if (p.getName().isPresent() == false) {
				if (Pageable.class.isAssignableFrom(p.getType()) == false
						&& Chunkable.class.isAssignableFrom(p.getType()) == false) {
					log.warn("null name parameter [{}] is ignored", p);
				}
			}
		}
		Iterable<StaticParam> staticParams = mirageQueryMethod.getStaticParameters();
		for (StaticParam p : staticParams) {
			parameterMap.put(p.key(), p.value());
		}
		return parameterMap;
	}
	
	private SqlResourceCandidate[] createQueryNameCandidates() {
		String name = mirageQueryMethod.getAnnotatedQuery();
		if (name != null) {
			return new SqlResourceCandidate[] {
				new SqlResourceCandidate(mirageQueryMethod.getDeclaringClass(), name),
			};
		}
		
		List<SqlResourceCandidate> candidates = new ArrayList<SqlResourceCandidate>();
		for (Class<?> clazz : new Class<?>[] {
			mirageQueryMethod.getRepositoryInterface(),
			mirageQueryMethod.getDeclaringClass(),
		}) {
			String simpleName = clazz.getSimpleName();
			String args = getArgsPartOfSignature(mirageQueryMethod.asMethod());
			String nameWithMethodName = simpleName + "_" + mirageQueryMethod.getName();
			candidates.addAll(Arrays.asList(
					new SqlResourceCandidate(clazz, nameWithMethodName + args + ".sql"),
					new SqlResourceCandidate(clazz, nameWithMethodName + ".sql"),
					new SqlResourceCandidate(clazz, simpleName + ".sql")));
		}
		return candidates.toArray(new SqlResourceCandidate[0]);
	}
	
	private SqlResource createSqlResource() {
		SqlResourceCandidate[] candidates = createQueryNameCandidates();
		return new ScopeClasspathSqlResource(candidates);
	}
	
	private int getTotalCount(SqlResource sqlResource) {
		try (Reader reader = new InputStreamReader(sqlResource.getInputStream(), StandardCharsets.UTF_8)) {
			String query = toString(reader);
			if (query.contains("SQL_CALC_FOUND_ROWS")) { // TODO MySQL固有処理
				return sqlManager.getSingleResult(Integer.class, new StringSqlResource("SELECT FOUND_ROWS()"));
			}
		} catch (IOException e) {
			log.error("IOException", e);
		}
		// ignore
		return Integer.MAX_VALUE;
	}
	
	private boolean isAscending(Chunkable chunkable) {
		return Optional.ofNullable(chunkable.getDirection()).orElse(Direction.ASC) == Direction.ASC;
	}
	
	private boolean isForward(Chunkable chunkable) {
		return Optional.ofNullable(chunkable.getPaginationRelation())
			.orElse(PaginationRelation.NEXT) == PaginationRelation.NEXT;
	}
	
	private Object processChunkQuery(SqlResource sqlResource, Map<String, Object> parameterMap,
			Class<?> returnedDomainType, ChunkableParameterAccessor accessor) {
		Chunkable chunkable = accessor.getChunkable();
		if (chunkable != null) {
			addChunkParam(parameterMap, chunkable);
		}
		
		return sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
	}
	
	private Object processPageQuery(SqlResource sqlResource, Map<String, Object> parameterMap,
			Class<?> returnedDomainType, ChunkableParameterAccessor accessor) {
		Pageable pageable = accessor.getPageable();
		addPageParam(parameterMap, pageable);
		
		List<?> resultList = sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
		
		if (List.class.isAssignableFrom(mirageQueryMethod.getReturnType())) {
			return resultList;
		}
		
		int totalCount = getTotalCount(sqlResource);
		
		return new PageImpl<>(resultList, pageable, totalCount);
	}
	
	private Object processSliceQuery(SqlResource sqlResource, Map<String, Object> parameterMap,
			Class<?> returnedDomainType, ChunkableParameterAccessor accessor) {
		Pageable pageable = accessor.getPageable();
		addPageParam(parameterMap, pageable);
		
		List<?> resultList = sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
		
		if (List.class.isAssignableFrom(mirageQueryMethod.getReturnType())) {
			return resultList;
		}
		
		return new SliceImpl<>(resultList, pageable, true/*TODO*/);
	}
	
	private String toString(Reader input) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[BUFFER_SIZE];
		int n = 0;
		while ((n = input.read(buffer)) != -1) { // NOPMD
			sb.append(buffer, 0, n);
		}
		return sb.toString();
	}
}
