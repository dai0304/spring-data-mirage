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
package jp.xet.springframework.data.mirage.repository.query;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import jp.xet.springframework.data.mirage.repository.MirageConfiguration;
import jp.xet.springframework.data.mirage.repository.preprocess.PreProcessEntityRepository;
import jp.xet.springframework.data.mirage.repository.preprocess.PreProcessEntityRepositoryTest.PreProcessConfiguration;

/**
 * Test for {@link PreProcessEntityRepository}.
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
	MirageConfiguration.class,
	PreProcessConfiguration.class
})
@Transactional
@SuppressWarnings("javadoc")
public class QueryEntityRepositoryTest {
	
	@Autowired
	QueryEntityRepository repo;
	
	
	@Test
	@Rollback
	public void testQuery() {
		// exercise
		repo.save(new QueryEntity("foo", 1));
		// verify
		assertThat(repo.foo("foo"))
			.isPresent();
	}
}
