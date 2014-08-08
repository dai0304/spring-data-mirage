/*
 * Copyright 2012 the original author or authors.
 * Created on 2012/12/29
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
package org.springframework.data.mirage.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;

/**
 * TODO for daisuke
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
@SuppressWarnings("serial")
public class NoSuchSqlResourceException extends RuntimeException {
	
	private static <T>T notNull(T target) {
		Assert.notNull(target);
		return target;
	}
	
	
	private final SqlResourceCandidate[] candidates;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param scope
	 * @param names
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 * @since 1.0
	 */
	@Deprecated
	public NoSuchSqlResourceException(Class<?> scope, String[] names) {
		super(String.format("%s:%s", notNull(scope).getName(), Arrays.toString(notNull(names))));
		
		List<SqlResourceCandidate> c = new ArrayList<SqlResourceCandidate>();
		for (String name : names) {
			c.add(new SqlResourceCandidate(scope, name));
		}
		candidates = c.toArray(new SqlResourceCandidate[c.size()]);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param candidates
	 * @since 0.2.1
	 */
	public NoSuchSqlResourceException(SqlResourceCandidate[] candidates) {
		super(Arrays.toString(candidates));
		this.candidates = candidates;
	}
	
	/**
	 * TODO for daisuke
	 *
	 * @return the names
	 * @since 1.0
	 */
	@Deprecated
	public String[] getNames() {
		String[] names = new String[candidates.length];
		for (int i = 0; i < candidates.length; i++) {
			names[i] = candidates[i].getName();
		}
		return names;
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @return the scope
	 * @since 1.0
	 */
	@Deprecated
	public Class<?> getScope() {
		return candidates[0].getScope();
	}
}
