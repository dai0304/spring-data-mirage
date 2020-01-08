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
package jp.xet.springframework.data.mirage.repository.example;

import java.util.List;

import org.springframework.data.repository.query.Param;

import jp.xet.sparwings.spring.data.chunk.Chunk;
import jp.xet.sparwings.spring.data.chunk.Chunkable;
import jp.xet.sparwings.spring.data.repository.ChunkableRepository;
import jp.xet.sparwings.spring.data.repository.PageableRepository;
import jp.xet.sparwings.spring.data.repository.ScannableRepository;
import jp.xet.sparwings.spring.data.repository.UpsertableRepository;
import jp.xet.sparwings.spring.data.repository.WritableRepository;

import jp.xet.springframework.data.mirage.repository.query.StaticParam;

/**
 * Repository interface for {@link Entity}.
 * 
 * @author daisuke
 */
public interface EntityRepository extends ScannableRepository<Entity, Long>, UpsertableRepository<Entity, Long>,
		WritableRepository<Entity, Long>, ChunkableRepository<Entity, Long>, PageableRepository<Entity, Long> {
	
	/**
	 * TODO for daisuke
	 * 
	 * @param str strプロパティ指定
	 * @return the entities found
	 */
	List<Entity> findByStr(@Param("str") String str);
	
	/**
	 * TODO for daisuke
	 * 
	 * @param str strプロパティ指定
	 * @return the entities found
	 */
	List<Entity> findByStrStartsWith(@Param("str") String str);
	
	/**
	 * TODO for daisuke
	 * 
	 * @return the xxx value
	 */
	@StaticParam(key = "str", value = "hoge")
	List<Entity> findXxx();
	
	/**
	 * TODO for wreulicke
	 *
	 * @return the entities found
	 */
	Chunk<Entity> findChunk(Chunkable chunkable);
	
}
