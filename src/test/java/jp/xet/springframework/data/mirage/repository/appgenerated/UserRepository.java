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
package jp.xet.springframework.data.mirage.repository.appgenerated;

import org.polycreo.repositories.BatchCreatableRepository;
import org.polycreo.repositories.BatchDeletableRepository;
import org.polycreo.repositories.BatchReadableRepository;
import org.polycreo.repositories.BatchUpsertableRepository;
import org.polycreo.repositories.ChunkableRepository;
import org.polycreo.repositories.CreatableRepository;
import org.polycreo.repositories.DeletableRepository;
import org.polycreo.repositories.LockableReadableRepository;
import org.polycreo.repositories.PageableRepository;
import org.polycreo.repositories.ScannableRepository;
import org.polycreo.repositories.TruncatableRepository;
import org.polycreo.repositories.UpdatableRepository;
import org.polycreo.repositories.UpsertableRepository;

/**
 * Repository interface for {@link User}.
 */
public interface UserRepository extends ScannableRepository<User, String>,
		CreatableRepository<User, String>, UpsertableRepository<User, String>,
		LockableReadableRepository<User, String>, UpdatableRepository<User, String>,
		DeletableRepository<User, String>, TruncatableRepository<User, String>,
		ChunkableRepository<User, String>, PageableRepository<User, String>,
		BatchReadableRepository<User, String>, BatchCreatableRepository<User, String>,
		BatchUpsertableRepository<User, String>, BatchDeletableRepository<User, String> {
}
