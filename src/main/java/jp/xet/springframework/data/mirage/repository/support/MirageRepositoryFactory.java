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
package jp.xet.springframework.data.mirage.repository.support;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import org.ws2ten1.chunks.PaginationTokenEncoder;
import org.ws2ten1.chunks.SimplePaginationTokenEncoder;

import com.miragesql.miragesql.SqlManager;
import com.miragesql.miragesql.naming.DefaultNameConverter;
import com.miragesql.miragesql.naming.NameConverter;

import jp.xet.springframework.data.mirage.repository.DefaultMirageRepository;
import jp.xet.springframework.data.mirage.repository.NoSuchSqlResourceException;
import jp.xet.springframework.data.mirage.repository.handler.RepositoryActionListener;
import jp.xet.springframework.data.mirage.repository.query.MirageQueryLookupStrategy;

/**
 * Mirage Repository factory.
 */
@Slf4j
@RequiredArgsConstructor
public class MirageRepositoryFactory extends RepositoryFactorySupport {
	
	@NonNull
	private final SqlManager sqlManager;
	
	private final NameConverter nameConverter;
	
	private final DataSource dataSource;
	
	@NonNull
	private final List<RepositoryActionListener> handlers;
	
	private final PaginationTokenEncoder encoder;
	
	
	public MirageRepositoryFactory(SqlManager sqlManager) {
		this(sqlManager, new DefaultNameConverter(), null, Collections.emptyList(),
				new SimplePaginationTokenEncoder());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return (EntityInformation<T, ID>) MirageEntityInformationSupport.getMetadata(domainClass, nameConverter);
	}
	
	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {
		return Optional.of(MirageQueryLookupStrategy.create(key, sqlManager, encoder));
	}
	
	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return DefaultMirageRepository.class;
	}
	
	@Override
	@SuppressWarnings({
		"rawtypes",
		"unchecked"
	})
	protected Object getTargetRepository(RepositoryInformation metadata) {
		Class<?> repositoryInterface = metadata.getRepositoryInterface();
		MirageEntityInformation mei = (MirageEntityInformation) getEntityInformation(metadata.getDomainType());
		
		DefaultMirageRepository repo = new DefaultMirageRepository(mei, sqlManager, handlers, encoder, dataSource);
		try {
			String name = repositoryInterface.getSimpleName() + ".sql";
			repo.setBaseSelectSqlResource(DefaultMirageRepository.newSqlResource(repositoryInterface, name));
		} catch (NoSuchSqlResourceException e) {
			log.debug("Repository Default SQL [{}] not found, default used.", repositoryInterface);
		}
		return repo;
	}
}
