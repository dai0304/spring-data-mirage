/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/20
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
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * TODO
 * 
 * @param <E> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @since 0.1
 * @version $Id: MirageRepository.java 161 2011-10-21 10:08:21Z daisuke $
 * @author daisuke
 */
@NoRepositoryBean
public interface MirageRepository<E, ID extends Serializable> extends PagingAndSortingRepository<E, ID> {
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @throws NullPointerException 引数に{@code null}を与えた場合
	 * @since 0.1
	 */
	@Override
	void delete(E entity);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.1
	 */
	@Override
	void delete(ID id);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 * @since 0.1
	 */
	@Override
	void delete(Iterable<? extends E> entities);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.1
	 */
	@Override
	void deleteAll();
	
	/**
	 * エンティティのバッチ削除を行う。
	 * 
	 * @param entities 削除するエンティティ
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.1
	 */
	void deleteInBatch(Iterable<E> entities);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 */
	@Override
	Iterable<E> findAll();
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 */
	@Override
	List<E> findAll(Sort sort);
	
	/**
	 * 指定したエンティティの識別子(ID)を返す。
	 * 
	 * @param entity エンティティ
	 * @return ID
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.1
	 */
	ID getId(E entity);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.1
	 */
	@Override
	<S extends E>Iterable<S> save(Iterable<S> entities);
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>{@code entity}として{@code null}を渡した場合、何もせずに{@code null}を返す。</p>
	 * 
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @since 0.1
	 */
	@Override
	<S extends E>S save(S entity);
}
