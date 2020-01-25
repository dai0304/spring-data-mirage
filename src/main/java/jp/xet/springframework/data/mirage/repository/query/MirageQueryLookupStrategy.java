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

import java.lang.reflect.Method;
import java.util.Locale;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import org.polycreo.chunkrequests.PaginationTokenEncoder;

import com.miragesql.miragesql.SqlManager;

/**
 * Query lookup strategy to execute finders.
 *
 * <p>Base class for {@link QueryLookupStrategy} implementations that need access to an {@link SqlManager}.</p>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MirageQueryLookupStrategy implements QueryLookupStrategy {
	
	/**
	 * Creates a {@link QueryLookupStrategy} for the given {@link SqlManager} and
	 * {@link org.springframework.data.repository.query.QueryLookupStrategy.Key}.
	 *
	 * @param key {@link org.springframework.data.repository.query.QueryLookupStrategy.Key}
	 * @param sqlManager {@link SqlManager}
	 * @return QueryLookupStrategy
	 */
	public static QueryLookupStrategy create(Key key, SqlManager sqlManager, PaginationTokenEncoder encoder) {
		if (key == null || key == Key.USE_DECLARED_QUERY) {
			return new MirageQueryLookupStrategy(sqlManager, encoder);
		}
		String message = String.format(Locale.ENGLISH, "Unsupported query lookup strategy %s!", key);
		throw new IllegalArgumentException(message);
	}
	
	
	private final SqlManager sqlManager;
	
	private final PaginationTokenEncoder encoder;
	
	
	@Override
	public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
			NamedQueries namedQueries) {
		// TODO utilize namedQueries
		MirageQueryMethod mirageQueryMethod = new MirageQueryMethod(method, metadata, factory);
		return new MirageQuery(mirageQueryMethod, sqlManager, encoder);
	}
}
