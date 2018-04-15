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
package jp.xet.springframework.data.mirage.repository;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;

import jp.xet.sparwings.spring.data.repository.BatchReadableRepository;
import jp.xet.sparwings.spring.data.repository.BatchWritableRepository;
import jp.xet.sparwings.spring.data.repository.ScannableRepository;

/**
 * TODO
 * 
 * @param <E> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @since 0.1
 * @version $Id: MirageRepository.java 161 2011-10-21 10:08:21Z daisuke $
 * @author daisuke
 */
@Deprecated
@NoRepositoryBean
public interface MirageRepository<E, ID extends Serializable>
		extends ScannableRepository<E, ID>, BatchReadableRepository<E, ID>, BatchWritableRepository<E, ID> {
}
