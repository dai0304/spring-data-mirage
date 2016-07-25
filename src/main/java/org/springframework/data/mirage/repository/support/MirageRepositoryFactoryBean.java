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
package org.springframework.data.mirage.repository.support;

import java.io.Serializable;

import jp.sf.amateras.mirage.SqlManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mirage.repository.BaseMirageRepository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

/**
 * TODO for daisuke
 * 
 * @param <T> 
 * @param <S> 
 * @param <ID> 
 * @since 0.1
 * @version $Id$
 * @author daisuke
 */
public class MirageRepositoryFactoryBean<T extends BaseMirageRepository<S, ID>, S, ID extends Serializable> extends
		TransactionalRepositoryFactoryBeanSupport<T, S, ID> {
	
	private SqlManager sqlManager;
	
	
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(sqlManager);
	}
	
	/**
	 * TODO for daisuke
	 * 
	 * @param sqlManager {@link SqlManager}
	 * @since 0.1
	 */
	@Autowired
	public void setSqlManager(SqlManager sqlManager) {
		this.sqlManager = sqlManager;
	}
	
	@Override
	protected RepositoryFactorySupport doCreateRepositoryFactory() {
		return new MirageRepositoryFactory(sqlManager);
	}
}
