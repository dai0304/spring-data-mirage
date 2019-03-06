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
package jp.xet.springframework.data.mirage.repository.idgenerated;

import java.util.List;

import org.springframework.data.repository.query.Param;

import org.ws2ten1.chunks.Chunk;
import org.ws2ten1.repositories.ChunkableRepository;
import org.ws2ten1.repositories.CrudRepository;
import org.ws2ten1.repositories.PageableRepository;
import org.ws2ten1.repositories.ScannableRepository;
import org.ws2ten1.repositories.UpsertableRepository;

import jp.xet.springframework.data.mirage.repository.query.StaticParam;

/**
 * Repository interface for {@link IdGeneratedEntity}.
 */
public interface IdGeneratedEntityRepository
		extends ScannableRepository<IdGeneratedEntity, Long>, UpsertableRepository<IdGeneratedEntity, Long>,
		CrudRepository<IdGeneratedEntity, Long>, ChunkableRepository<IdGeneratedEntity, Long>,
		PageableRepository<IdGeneratedEntity, Long> {
	
	/**
	 * TODO for daisuke
	 *
	 * @param str strプロパティ指定
	 * @return the entities found
	 */
	List<IdGeneratedEntity> findByStr(@Param("str") String str);
	
	/**
	 * TODO for daisuke
	 *
	 * @param str strプロパティ指定
	 * @return the entities found
	 */
	List<IdGeneratedEntity> findByStrStartsWith(@Param("str") String str);
	
	/**
	 * TODO for daisuke
	 *
	 * @return the xxx value
	 */
	@StaticParam(key = "str", value = "hoge")
	@StaticParam(key = "str2", value = "hoge2")
	List<IdGeneratedEntity> findXxx();
	
	/**
	 * TODO for wreulicke
	 *
	 * @return the entities found
	 */
	Chunk<IdGeneratedEntity> findChunk();
	
}