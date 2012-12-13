/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2011/11/13
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
package org.springframework.data.mirage.repository.example;

import java.util.List;

import org.springframework.data.mirage.repository.LogicalDeleteJdbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * {@link Entity}用リポジトリインターフェイス。
 * 
 * @since 1.0
 * @version $Id$
 * @author daisuke
 */
@Repository
public interface EntityRepository extends LogicalDeleteJdbcRepository<Entity> {
	
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
}
