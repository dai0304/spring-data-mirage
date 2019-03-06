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
package jp.xet.springframework.data.mirage.repository.preprocess;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import jp.xet.springframework.data.mirage.repository.TestConfiguration;

/**
 * Test for {@link PreProcessEntityRepository}.
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Transactional
@SuppressWarnings("javadoc")
public class PreProcessEntityRepositoryTest {
	
	@Autowired
	PreProcessEntityRepository repo;
	
	
	@Test
	@Rollback
	public void testPreProcess() {
		repo.create(new PreProcessEntity("foo", UUID.randomUUID().toString(), 0));
		PreProcessEntity found = repo.findById("foo").orElse(null);
		log.info("{}", found);
		assertThat(found.getLastUpdated()).isGreaterThan(0);
	}
}
