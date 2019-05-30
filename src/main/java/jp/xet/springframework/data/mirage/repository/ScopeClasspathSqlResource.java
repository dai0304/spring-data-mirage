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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miragesql.miragesql.ClasspathSqlResource;

/**
 * TODO daisuke
 * 
 * @since 0.1
 * @version $Id$
 * @author daisuke
 */
public class ScopeClasspathSqlResource extends ClasspathSqlResource {
	
	private static Logger log = LoggerFactory.getLogger(ScopeClasspathSqlResource.class);
	
	
	private static boolean existsResource(String absolutePath) {
		if (absolutePath == null) {
			return false;
		}
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return cl.getResource(absolutePath) != null;
	}
	
	private static String join(List<String> list) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> parts = list.iterator();
		if (parts.hasNext()) {
			sb.append(parts.next());
			while (parts.hasNext()) {
				sb.append("/");
				sb.append(parts.next());
			}
		}
		return sb.toString();
	}
	
	private static String toAbsolutePath(SqlResourceCandidate[] candidates) {
		Assert.noNullElements(candidates, "candidates must not be contains null element");
		
		for (SqlResourceCandidate candidate : candidates) {
			Class<?> scope = candidate.getScope();
			String name = candidate.getName();
			String packageName = scope != null ? scope.getPackage().getName() : "";
			String currentPath = toAbsolutePath(packageName, name);
			if (existsResource(currentPath)) {
				return toAbsolutePath(packageName, name);
			} else {
				log.trace("{} not exists", currentPath);
			}
		}
		throw new NoSuchSqlResourceException(candidates);
	}
	
	private static String toAbsolutePath(final String packageName, final String relativePath) {
		// Is path already absolute?
		if (relativePath.startsWith("/")) {
			return relativePath;
		} else {
			// Break package into list of package names
			List<String> absolutePath = new ArrayList<String>(Arrays.asList(packageName.split("\\.")));
			
			// Break path into folders
			final String[] folders = relativePath.split("[/\\\\]");
			
			// Iterate through folders
			for (String folder : folders) {
				// Up one?
				if ("..".equals(folder)) {
					// Pop off stack
					if (absolutePath.size() > 0) {
						absolutePath.remove(absolutePath.size() - 1);
					} else {
						throw new IllegalArgumentException("Invalid path " + relativePath);
					}
				} else {
					// Add to stack
					absolutePath.add(folder);
				}
			}
			
			// Return absolute path
			return join(absolutePath);
		}
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param scope
	 * @param name
	 * @throws NoSuchSqlResourceException 指定したリソースが見つからない場合
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 * @since 0.1
	 */
	public ScopeClasspathSqlResource(Class<?> scope, String name) {
		this(new SqlResourceCandidate(scope, name));
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param candidate
	 * @since 0.2.5
	 */
	public ScopeClasspathSqlResource(SqlResourceCandidate candidate) {
		this(new SqlResourceCandidate[] {
			candidate
		});
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param candidates
	 * @since 0.2.5
	 */
	public ScopeClasspathSqlResource(SqlResourceCandidate[] candidates) {
		super(toAbsolutePath(candidates));
	}
	
	@Override
	public String toString() {
		return "SimpleSqlResource " + super.toString().substring(20);
	}
}
