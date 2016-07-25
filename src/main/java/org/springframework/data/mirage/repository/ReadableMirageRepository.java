/*
 * Copyright 2012 the original author or authors.
 * Created on 2013/09/11
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

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repository interface to retrieve single entity.
 * 
 * @param <E> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @since 0.2.1
 * @version $Id$
 * @author daisuke
 */
@NoRepositoryBean
public interface ReadableMirageRepository<E, ID extends Serializable>extends BaseMirageRepository<E, ID> {
	
	/**
	 * Returns whether an entity with the given id exists.
	 * 
	 * @param id must not be {@literal null}.
	 * @return true if an entity with the given id exists, {@literal false} otherwise
	 * @throws IllegalArgumentException if {@code id} is {@literal null}
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.2.1
	 */
	boolean exists(ID id);
	
	/**
	 * Retrieves an entity by its id.
	 * 
	 * @param id must not be {@literal null}.
	 * @return the entity with the given id or {@literal null} if none found
	 * @throws IllegalArgumentException if {@code id} is {@literal null}
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.2.1
	 */
	E findOne(ID id);
	
	/**
	 * 指定したエンティティの識別子(ID)を返す。
	 * 
	 * @param entity エンティティ
	 * @return ID
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.1
	 */
	ID getId(E entity);
	
}
