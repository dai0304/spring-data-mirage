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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.sf.amateras.mirage.SqlManager;
import jp.sf.amateras.mirage.SqlResource;
import jp.sf.amateras.mirage.StringSqlResource;

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
 * @since 1.2
 * @version $Id$
 * @author daisuke
 */
public class MirageQuery implements RepositoryQuery {
	
	private static Logger logger = LoggerFactory.getLogger(MirageQuery.class);
	
	
	private static void addPageParam(Map<String, Object> params, Pageable pageable) {
		params.put("offset", pageable == null ? null : pageable.getOffset());
		params.put("size", pageable == null ? null : pageable.getPageSize());
		if (pageable != null && pageable.getSort() != null) {
			List<String> orders = new ArrayList<String>();
			Sort sort = pageable.getSort();
			for (Order order : sort) {
				orders.add(String.format("%s %s", order.getProperty(), order.getDirection().name()));
			}
			if (orders.size() != 0) {
				params.put("orders", join(orders));
			}
		}
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
	 * @param mirageQueryMethod 
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
		if (mirageQueryMethod.isModifyingQuery()) {
			return sqlManager.executeUpdate(sqlResource, parameterMap);
		} else if (mirageQueryMethod.isCollectionQuery()) {
			return sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
		} else if (mirageQueryMethod.isPageQuery()) {
			ParameterAccessor accessor = new ParametersParameterAccessor(mirageQueryMethod.getParameters(), parameters);
			Pageable pageable = accessor.getPageable();
			addPageParam(parameterMap, pageable);
			List<?> resultList = sqlManager.getResultList(returnedDomainType, sqlResource, parameterMap);
			int totalCount;
			/*if (query.contains("SQL_CALC_FOUND_ROWS")) { */
			totalCount = sqlManager.getCount(new StringSqlResource("SELECT FOUND_ROWS();")); // TODO MySQL固有処理
			/*} else {
				totalCount = ...; // TODO
			}
			*/
			
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
	
	private SqlResource createSqlResource() {
		String[] names;
		Class<?> declaringClass = mirageQueryMethod.getDeclaringClass();
		String name = mirageQueryMethod.getAnnotatedQuery();
		if (name != null) {
			names = new String[] {
				name
			};
		} else {
			String simpleName = declaringClass.getSimpleName();
			names = new String[] {
				simpleName + "_" + mirageQueryMethod.getName() + ".sql",
				simpleName + ".sql"
			};
		}
		return new ScopeClasspathSqlResource(declaringClass, names);
	}
}
