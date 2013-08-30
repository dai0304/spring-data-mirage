/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/10/21
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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * {@link #delete}操作として論理削除を行うリポジトリインターフェイス。
 * 
 * <p>論理削除は、そのエンティティの識別子(ID)、つまりテーブルの主キーの値を負数に反転することによって実現する。
 * つまり、主キーは数値でなければならず、このインターフェイスでは {@link Long} を採用している。</p>
 * 
 * <p>{@link #findAll()}等のクエリ操作では、主キーが負数のもの（及び、関連先が削除されているもの）は選択しない。ただし、
 * {@link #findOne}だけは例外で、引数に負数を指定すれば論理削除したエンティティも取り出せる。論理削除したエンティティに関しては
 * {@link #revert}操作で復旧ができる。{@link #revert}メソッドに与えるIDも負数とする。</p>
 * 
 * <p>物理削除は{@link #delete}ではなく{@link #physicalDelete}を用いる。</p>
 * 
 * <p>テーブル設計においては、主キーの値がUPDATEされることがあるため、このリポジトリに対応するテーブルを参照する外部キーについて、
 * その{@code ON UPDATE}のreferential actionは{@code CASCADE}である必要がある。</p>
 * 
 * @param <E> the domain type the repository manages
 * @since 1.0
 * @version $Id$
 * @author daisuke
 * @see <a href="http://bit.ly/qQtt9T">削除フラグのはなし</a>
 */
@NoRepositoryBean
public interface LogicalDeleteMirageRepository<E extends Identifiable> extends MirageRepository<E, Long> {
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>IDに負数を与えた場合は何もしない。</p>
	 */
	@Override
	void delete(Long id);
	
	/**
	 * TODO for daisuke
	 * 
	 * @param id
	 * @return
	 * @since 1.0
	 */
	E findOneIncludeLogicalDeleted(Long id);
	
	/**
	 * Deletes a given entity.
	 *
	 * @param entity entity to delete
	 * @since 1.0
	 */
	void physicalDelete(E entity);
	
	/**
	 * Deletes the given entities.
	 *
	 * @param entities entities to delete
	 * @since 1.0
	 */
	void physicalDelete(Iterable<? extends E> entities);
	
	/**
	 * Deletes the entity with the given id.
	 * 
	 * @param id ID of entity to delete
	 * @since 1.0
	 */
	void physicalDelete(Long id);
	
	/**
	 * Deletes all entities managed by the repository.
	 * 
	 * @since 1.0
	 */
	void physicalDeleteAll();
	
	/**
	 * エンティティのバッチ物理削除を行う。
	 * 
	 * @param entities 削除するエンティティ
	 * @throws DataIntegrityViolationException 整合性違反が発生した場合
	 * @since 1.0
	 */
	void physicalDeleteInBatch(Iterable<E> entities);
	
	/**
	 * 論理削除したエンティティを復活させる。
	 * 
	 * <p>IDに正数を与えた場合は何もしない。</p>
	 * 
	 * @param id エンティティID（負数）
	 * @since 1.0
	 */
	void revert(Long id);
}
