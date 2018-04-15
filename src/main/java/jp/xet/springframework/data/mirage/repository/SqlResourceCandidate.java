/*
 * Copyright 2011-2018 the original author or authors.
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
	 */
	public SqlResourceCandidate(Class<?> scope, String name) {
		this.scope = scope;
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SqlResourceCandidate other = (SqlResourceCandidate) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (scope == null) {
			if (other.scope != null) {
				return false;
			}
		} else if (!scope.equals(other.scope)) {
			return false;
		}
		return true;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<?> getScope() {
		return scope;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}
	
	@Override
	public String toString() {
		return "SqlResourceCandidate [scope=" + scope + ", name=" + name + "]";
	}
}
