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
package jp.xet.springframework.data.mirage.repository.support;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miragesql.miragesql.SqlManager;

import jp.xet.springframework.data.mirage.repository.DefaultMirageRepository;
import jp.xet.springframework.data.mirage.repository.Identifiable;
import jp.xet.springframework.data.mirage.repository.IdentifiableMirageRepository;
import jp.xet.springframework.data.mirage.repository.NoSuchSqlResourceException;
import jp.xet.springframework.data.mirage.repository.query.MirageQueryLookupStrategy;

/**
 * TODO for daisuke
 * 
 * @since 0.1
 * @version $Id$
 * @author daisuke
 */
public class MirageRepositoryFactory extends RepositoryFactorySupport {
	
	private static Logger logger = LoggerFactory.getLogger(MirageRepositoryFactory.class);
	
	private final SqlManager sqlManager;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param sqlManager {@link SqlManager}
	 * @throws IllegalArgumentException if the argument is {@code null}
	 * @since 0.1
	 */
	public MirageRepositoryFactory(SqlManager sqlManager) {
		Assert.notNull(sqlManager, "sqlManager is required");
		this.sqlManager = sqlManager;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return (EntityInformation<T, ID>) MirageEntityInformationSupport.getMetadata(domainClass, sqlManager);
	}
	
	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {
		return Optional.of(MirageQueryLookupStrategy.create(sqlManager, key));
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
		EntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());
		
		DefaultMirageRepository repos;
		if (isIdentifiableJdbcRepository(entityInformation)) {
			repos = new IdentifiableMirageRepository<Identifiable>(
					(EntityInformation<Identifiable, ? extends Serializable>) entityInformation, sqlManager);
		} else {
			repos = new DefaultMirageRepository(entityInformation, sqlManager);
		}
		try {
			String name = repositoryInterface.getSimpleName() + ".sql";
			repos.setBaseSelectSqlResource(DefaultMirageRepository.newSqlResource(repositoryInterface, name));
		} catch (NoSuchSqlResourceException e) {
			logger.debug("Repository Default SQL [{}] not found, default used.", repositoryInterface);
		}
		return repos;
	}
	
	private boolean isIdentifiableJdbcRepository(EntityInformation<?, Serializable> entityInformation) {
		return Identifiable.class.isAssignableFrom(entityInformation.getJavaType());
	}
}
