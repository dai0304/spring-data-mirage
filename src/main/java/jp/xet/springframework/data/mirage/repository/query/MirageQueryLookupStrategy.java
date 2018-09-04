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
package jp.xet.springframework.data.mirage.repository.query;

import java.lang.reflect.Method;
import java.util.Locale;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import com.miragesql.miragesql.SqlManager;

/**
 * Query lookup strategy to execute finders.
 * 
 * <p>Base class for {@link QueryLookupStrategy} implementations that need access to an {@link SqlManager}.</p>
 * 
 * @since 0.1
 * @version $Id$
 * @author daisuke
 */
public abstract class MirageQueryLookupStrategy implements QueryLookupStrategy {
	
	/**
	 * Creates a {@link QueryLookupStrategy} for the given {@link SqlManager} and
	 * {@link org.springframework.data.repository.query.QueryLookupStrategy.Key}.
	 * 
	 * @param sqlManager {@link SqlManager}
	 * @param key
	 * @return
	 * @since 0.1
	 */
	public static QueryLookupStrategy create(SqlManager sqlManager, Key key) {
		if (key == null) {
			return new CreateIfNotFoundQueryLookupStrategy(sqlManager);
		}
		
		switch (key) {
			case CREATE:
				return new CreateQueryLookupStrategy(sqlManager);
			case USE_DECLARED_QUERY:
				return new DeclaredQueryLookupStrategy(sqlManager);
			case CREATE_IF_NOT_FOUND:
				return new CreateIfNotFoundQueryLookupStrategy(sqlManager);
			default:
				throw new IllegalArgumentException(
						String.format(Locale.ENGLISH, "Unsupported query lookup strategy %s!", key));
		}
	}
	
	
	private final SqlManager sqlManager;
	
	
	MirageQueryLookupStrategy(SqlManager sqlManager) {
		this.sqlManager = sqlManager;
	}
	
	@Override
	public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
			NamedQueries namedQueries) {
		return resolveQuery(new MirageQueryMethod(method, metadata, factory), sqlManager, namedQueries);
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @param method
	 * @param sqlManager {@link SqlManager}
	 * @param namedQueries
	 * @return
	 * @since 0.1
	 */
	protected abstract RepositoryQuery resolveQuery(MirageQueryMethod method, SqlManager sqlManager,
			NamedQueries namedQueries);
	
	
	/**
	 * {@link QueryLookupStrategy} to try to detect a declared query first ( {@link Query}, Mirage named query). In case none
	 * is found we fall back on query creation.
	 */
	private static class CreateIfNotFoundQueryLookupStrategy extends MirageQueryLookupStrategy {
		
		private final DeclaredQueryLookupStrategy strategy;
		
		private final CreateQueryLookupStrategy createStrategy;
		
		
		CreateIfNotFoundQueryLookupStrategy(SqlManager sqlManager) {
			super(sqlManager);
			strategy = new DeclaredQueryLookupStrategy(sqlManager);
			createStrategy = new CreateQueryLookupStrategy(sqlManager);
		}
		
		@Override
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
				NamedQueries namedQueries) {
			try {
				return strategy.resolveQuery(method, metadata, factory, namedQueries);
			} catch (IllegalStateException e) {
				return createStrategy.resolveQuery(method, metadata, factory, namedQueries);
			}
		}
		
		@Override
		protected RepositoryQuery resolveQuery(MirageQueryMethod method, SqlManager sqlManager,
				NamedQueries namedQueries) {
			try {
				return strategy.resolveQuery(method, sqlManager, namedQueries);
			} catch (IllegalStateException e) {
				return createStrategy.resolveQuery(method, sqlManager, namedQueries);
			}
		}
	}
	
	/**
	 * {@link QueryLookupStrategy} to create a query from the method name.
	 */
	private static class CreateQueryLookupStrategy extends MirageQueryLookupStrategy {
		
		CreateQueryLookupStrategy(SqlManager sqlManager) {
			super(sqlManager);
		}
		
		@Override
		protected RepositoryQuery resolveQuery(MirageQueryMethod method, SqlManager sqlManager,
				NamedQueries namedQueries) {
			return null; // new PartTreeJpaQuery(method, sqlManager); // TODO
		}
	}
	
	/**
	 * {@link QueryLookupStrategy} that tries to detect a declared query declared via {@link Query} annotation followed by
	 * a Mirage named query lookup.
	 */
	private static class DeclaredQueryLookupStrategy extends MirageQueryLookupStrategy {
		
		DeclaredQueryLookupStrategy(SqlManager sqlManager) {
			super(sqlManager);
		}
		
		@Override
		protected RepositoryQuery resolveQuery(MirageQueryMethod method, SqlManager sqlManager,
				NamedQueries namedQueries) {
			return new MirageQuery(method, sqlManager); // TODO
		}
	}
}
