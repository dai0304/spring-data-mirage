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
import java.util.List;

import javax.sql.DataSource;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

import com.miragesql.miragesql.SqlManager;
import com.miragesql.miragesql.naming.NameConverter;

import jp.xet.springframework.data.mirage.repository.handler.RepositoryActionListener;

/**
 * {@link org.springframework.beans.factory.FactoryBean} for {@link MirageRepositoryFactory}.
 *
 * @param <T> type of repository
 * @param <S> type of entity
 * @param <ID> type of entity ID
 */
public class MirageRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {
	
	@Setter
	private SqlManager sqlManager;
	
	@Autowired(required = false)
	NameConverter nameConverter;
	
	@Autowired(required = false)
	DataSource dataSource;
	
	@Autowired
	List<RepositoryActionListener> handlers;
	
	
	protected MirageRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}
	
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(sqlManager, "sqlManager is required");
	}
	
	@Override
	protected RepositoryFactorySupport doCreateRepositoryFactory() {
		return new MirageRepositoryFactory(sqlManager, nameConverter, dataSource, handlers);
	}
}
