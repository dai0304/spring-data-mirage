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

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class MirageQueryMethod extends QueryMethod {
	
	final Method method;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param method
	 * @param metadata
	 * @param provider 
	 */
	public MirageQueryMethod(Method method, RepositoryMetadata metadata, QueryExtractor provider) {
		super(method, metadata);
		Assert.notNull(method, "Method must not be null!");
		this.method = method;
		
		Assert.isTrue((isModifyingQuery() && getParameters().hasSpecialParameter()) == false,
				String.format("Modifying method must not contain %s!", Parameters.TYPES));
	}
	
	/**
	 * Returns whether the finder is a modifying one.
	 * 
	 * @return {@code true} if the finder is a modifying one
	 */
	@Override
	protected boolean isModifyingQuery() {
		return method.getAnnotation(Modifying.class) != null;
	}
	
	/**
	 * Returns the query string declared in a {@link Query} annotation or {@code null} if neither the annotation found
	 * nor the attribute was specified.
	 * 
	 * @return
	 */
	String getAnnotatedQuery() {
		Query queryAnnotation = getQueryAnnotation();
		if (queryAnnotation == null) {
			return null;
		}
		String query = (String) AnnotationUtils.getValue(queryAnnotation);
		return StringUtils.hasText(query) ? query : null;
	}
	
	/**
	 * Returns the countQuery string declared in a {@link Query} annotation or {@code null} if neither the annotation
	 * found nor the attribute was specified.
	 * 
	 * @return
	 */
	String getCountQuery() {
		Query queryAnnotation = getQueryAnnotation();
		if (queryAnnotation == null) {
			return null;
		}
		String countQuery = (String) AnnotationUtils.getValue(queryAnnotation, "countQuery");
		return StringUtils.hasText(countQuery) ? countQuery : null;
	}
	
	Class<?> getDeclaringClass() {
		return method.getDeclaringClass();
	}
	
	/**
	 * Returns the actual return type of the method.
	 * 
	 * @return
	 */
	Class<?> getReturnType() {
		return method.getReturnType();
	}
	
	/**
	 * Returns the {@link Query} annotation that is applied to the method or {@code null} if none available.
	 * 
	 * @return
	 */
	private Query getQueryAnnotation() {
		return method.getAnnotation(Query.class);
	}
}
