/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2012/05/16
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.springframework.data.mirage.repository.query;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.sf.amateras.mirage.SqlManager;
import jp.sf.amateras.mirage.SqlResource;
import jp.sf.amateras.mirage.StringSqlResource;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mirage.repository.ScopeClasspathSqlResource;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

/**
 * TODO for daisuke
 * 
 * @since 0.1
 * @version $Id$
 * @author daisuke
 */
public class MirageQuery implements RepositoryQuery {
	
	private static Logger logger = LoggerFactory.getLogger(MirageQuery.class);
	
	
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
		} catch (Exception e) {
			return "<" + e + ">";
		}
	}
	
	private static void addPageParam(Map<String, Object> params, Pageable pageable) {
		if (pageable == null) {
			return;
		}
		params.put("offset", pageable.getOffset());
		params.put("size", pageable.getPageSize());
		if (pageable.getSort() != null) {
			Sort sort = pageable.getSort();
			addSortParam(params, sort);
		}
	}
	
	private static void addSortParam(Map<String, Object> params, Sort sort) {
		if (sort == null) {
			return;
		}
		List<String> orders = new ArrayList<String>();
		for (Order order : sort) {
			orders.add(String.format("%s %s", order.getProperty(), order.getDirection().name()));
		}
		if (orders.size() != 0) {
			params.put("orders", join(orders));
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
				StringBuffer sb = new StringBuffer();
				sb.append(cl.getName());
				for (int i = 0; i < dimensions; i++) {
					sb.append("[]");
				}
				return sb.toString();
			} catch (Throwable e) {
				//$FALL-THROUGH$
			}
		}
		return type.getName();
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
	
	
	private final MirageQueryMethod mirageQueryMethod;
	
	private final SqlManager sqlManager;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param mirageQueryMethod {@link MirageQueryMethod}
	 * @param sqlManager {@link SqlManager}
	 * @throws IllegalArgumentException if the argument is {@code null}
	 */
	public MirageQuery(MirageQueryMethod mirageQueryMethod, SqlManager sqlManager) {
		Assert.notNull(mirageQueryMethod, "MirageQueryMethod must not to be null");
		Assert.notNull(sqlManager, "SqlManager must not to be null");
		this.mirageQueryMethod = mirageQueryMethod;
		this.sqlManager = sqlManager;
	}
	
	@Override
	public Object execute(Object[] parameters) {
		SqlResource sqlResource = createSqlResource();
		Map<String, Object> parameterMap = createParameterMap(parameters);
		
		Class<?> returnedDomainType = mirageQueryMethod.getReturnedObjectType();
		ParameterAccessor accessor = new ParametersParameterAccessor(mirageQueryMethod.getParameters(), parameters);
		
		if (mirageQueryMethod.isModifyingQuery()) {
			return sqlManager.executeUpdate(sqlResource, parameterMap);
		} else if (mirageQueryMethod.isCollectionQuery()) {
			Sort sort = accessor.getSort();
			if (sort != null) {
				addSortParam(parameterMap, sort);
			}
			return sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
		} else if (mirageQueryMethod.isPageQuery()) {
			Pageable pageable = accessor.getPageable();
			if (pageable != null) {
				addPageParam(parameterMap, pageable);
			} else if (accessor.getSort() != null) {
				Sort sort = accessor.getSort();
				addSortParam(parameterMap, sort);
			}
			List<?> resultList = sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
			
			if (List.class.isAssignableFrom(mirageQueryMethod.getReturnType())) {
				return resultList;
			}
			
			int totalCount = getTotalCount(sqlResource);
			
			@SuppressWarnings({
				"rawtypes",
				"unchecked"
			})
			PageImpl<?> page = new PageImpl(resultList, pageable, totalCount);
			return page;
		} else {
			return sqlManager.getSingleResult(returnedDomainType, sqlResource, parameterMap);
		}
	}
	
	@Override
	public QueryMethod getQueryMethod() {
		return mirageQueryMethod;
	}
	
	private Map<String, Object> createParameterMap(Object[] parameters) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("orders", null);
		for (Parameter p : mirageQueryMethod.getParameters()) {
			String parameterName = p.getName();
			if (parameterName == null) {
				if (Pageable.class.isAssignableFrom(p.getType()) == false) {
					logger.warn("null name parameter [{}] is ignored", p);
				}
			} else {
				parameterMap.put(parameterName, parameters[p.getIndex()]);
			}
		}
		Iterable<StaticParam> staticParams = mirageQueryMethod.getStaticParameters();
		for (StaticParam p : staticParams) {
			parameterMap.put(p.key(), p.value());
		}
		return parameterMap;
	}
	
	private String[] createQueryNameCandidates() {
		Class<?> declaringClass = mirageQueryMethod.getDeclaringClass();
		String name = mirageQueryMethod.getAnnotatedQuery();
		if (name != null) {
			return new String[] {
				name
			};
		}
		String simpleName = declaringClass.getSimpleName();
		String args = getArgsPartOfSignature(mirageQueryMethod.asMethod());
		return new String[] {
			simpleName + "#" + mirageQueryMethod.getName() + args + ".sql",
			simpleName + "#" + mirageQueryMethod.getName() + ".sql",
			simpleName + "_" + mirageQueryMethod.getName() + args + ".sql",
			simpleName + "_" + mirageQueryMethod.getName() + ".sql",
			simpleName + ".sql"
		};
	}
	
	private SqlResource createSqlResource() {
		String[] names = createQueryNameCandidates();
		return new ScopeClasspathSqlResource(mirageQueryMethod.getDeclaringClass(), names);
	}
	
	@SuppressWarnings("deprecation")
	private int getTotalCount(SqlResource sqlResource) {
		Reader r = null;
		try {
			r = new InputStreamReader(sqlResource.getInputStream(), Charsets.UTF_8);
			String query = CharStreams.toString(r);
			if (query.contains("SQL_CALC_FOUND_ROWS")) { // TODO MySQL固有処理
				return sqlManager.getSingleResult(Integer.class, new StringSqlResource("SELECT FOUND_ROWS()"));
			}
		} catch (IOException e) {
			logger.error("IOException", e);
		} finally {
			Closeables.closeQuietly(r);
		}
		return 0; // TODO
	}
}
