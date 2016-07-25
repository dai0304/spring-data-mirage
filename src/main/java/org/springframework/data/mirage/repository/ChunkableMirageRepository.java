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

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.NoRepositoryBean;

import jp.xet.sparwings.spring.data.chunk.Chunk;
import jp.xet.sparwings.spring.data.chunk.Chunkable;

/**
 * Repository interface to retrieve chunk of entities.
 * 
 * @param <E> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @since #version#
 * @author daisuke
 */
@NoRepositoryBean
public interface ChunkableMirageRepository<E, ID extends Serializable>extends ReadableMirageRepository<E, ID> {
	
	/**
	 * Returns a {@link Chunk} of entities meeting the chunking restriction provided in the {@code Chunkable} object.
	 * 
	 * @param chunkable chunking information
	 * @return a chunk of entities
	 * @throws DataAccessException データアクセスエラーが発生した場合
	 * @throws NullPointerException 引数に{@code null}を与えた場合
	 * @since #version#
	 */
	Chunk<E> findAll(Chunkable chunkable);
	
}
