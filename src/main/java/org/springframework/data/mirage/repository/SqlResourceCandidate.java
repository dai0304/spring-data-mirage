/*
 * Copyright 2012 the original author or authors.
 * Created on 2014/08/07
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

/**
 * TODO for daisuke
 * 
 * @since 0.2.5
 * @version $Id$
 * @author daisuke
 */
public class SqlResourceCandidate {
	
	private final Class<?> scope;
	
	private final String name;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param scope
	 * @param name
	 * @since 0.2.5
	 */
	public SqlResourceCandidate(Class<?> scope, String name) {
		super();
		this.scope = scope;
		this.name = name;
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @return
	 * @since 0.2.5
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @return
	 * @since 0.2.5
	 */
	public Class<?> getScope() {
		return scope;
	}
	
	@Override
	public String toString() {
		return "SqlResourceCandidate [scope=" + scope + ", name=" + name + "]";
	}
}
