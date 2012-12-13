/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/11/11
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

import java.io.Serializable;

import jp.sf.amateras.mirage.SqlManager;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.util.Assert;

/**
 * TODO for daisuke
 * 
 * @param <E> the domain type the repository manages
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
public class IdentifiableMirageRepository<E extends Identifiable> extends SimpleMirageRepository<E, Long> {
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param entityClass エンティティの型
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 * @since 1.0
	 */
	public IdentifiableMirageRepository(Class<E> entityClass) {
		super(entityClass);
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param entityInformation
	 * @param sqlManager {@link SqlManager}
	 * @since 1.0
	 */
	public IdentifiableMirageRepository(EntityInformation<E, ? extends Serializable> entityInformation,
			SqlManager sqlManager) {
		super(entityInformation, sqlManager);
	}
	
	@Override
	public final Long getId(E entity) {
		Assert.notNull(entity);
		return entity.getId();
	}
}
