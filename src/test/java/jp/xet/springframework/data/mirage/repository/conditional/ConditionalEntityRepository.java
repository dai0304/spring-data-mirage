/*
 * Copyright 2019 the original author or authors.
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
package jp.xet.springframework.data.mirage.repository.conditional;

import org.ws2ten1.repositories.ConditionalDeletableRepository;
import org.ws2ten1.repositories.ConditionalUpdatableRepository;
import org.ws2ten1.repositories.CreatableRepository;
import org.ws2ten1.repositories.ReadableRepository;

/**
 * Repository interface for {@link ConditionalEntity}.
 */
public interface ConditionalEntityRepository extends
		CreatableRepository<ConditionalEntity, String>, ReadableRepository<ConditionalEntity, String>,
		ConditionalUpdatableRepository<ConditionalEntity, String, Long>,
		ConditionalDeletableRepository<ConditionalEntity, String, Long> {
}
