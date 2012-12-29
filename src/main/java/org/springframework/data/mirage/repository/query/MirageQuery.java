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

import java.util.HashMap;
import java.util.Map;

import jp.sf.amateras.mirage.SqlManager;

import org.springframework.data.mirage.repository.SimpleSqlResource;
import org.springframework.data.mirage.repository.SqlResource;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
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
		String[] names;
		
		Class<?> declaringClass = mirageQueryMethod.getDeclaringClass();
		String name = mirageQueryMethod.getAnnotatedQuery();
		if (name != null) {
			names = new String[] {
				name
			};
		} else {
			names = new String[] {
				declaringClass.getSimpleName() + "_" + mirageQueryMethod.getName() + ".sql",
				declaringClass.getSimpleName() + ".sql"
			};
		}
		SqlResource sqlResource = new SimpleSqlResource(declaringClass, names);
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("orders", null);
		Parameters params = mirageQueryMethod.getParameters();
		for (Parameter p : params) {
			parameterMap.put(p.getName(), parameters[p.getIndex()]);
		}
		
		String absolutePath = sqlResource.getAbsolutePath();
		switch (mirageQueryMethod.getType()) {
			case SINGLE_ENTITY:
				return getSqlManager().getSingleResult(mirageQueryMethod.getReturnType(), absolutePath, parameterMap);
				
			case PAGING:
			case COLLECTION:
				return getSqlManager().getResultList(mirageQueryMethod.getReturnType(), absolutePath, parameterMap);
				
			case MODIFYING:
				return getSqlManager().executeUpdate(absolutePath, parameterMap);
				
			default:
				return null;
		}
	}
	
	@Override
	public QueryMethod getQueryMethod() {
		return mirageQueryMethod;
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @return {@link SqlManager}
	 * @since 1.0
	 */
	protected SqlManager getSqlManager() {
		return sqlManager;
	}
}
