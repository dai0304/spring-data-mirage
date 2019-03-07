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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import jp.xet.springframework.data.mirage.repository.TestConfiguration;

/**
 * Test for {@link UserRepository}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Transactional
@Slf4j
@SuppressWarnings("javadoc")
public class UserRepositoryTest {
	
	@Autowired
	UserRepository repo;
	
	
	@Test
	public void test() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		repo.save(new User("foo", "foopass"));
		assertThat(repo.count(), is(1L));
		repo.save(new User("bar", "barpass"));
		assertThat(repo.count(), is(2L));
		
		User foundFoo = repo.findById("foo").orElse(null);
		assertThat(foundFoo.getPassword(), is("foopass"));
	}
	
	@Test(expected = DuplicateKeyException.class)
	public void test_fail_to_create() {
		assertThat(repo, is(notNullValue()));
		assertThat(repo.count(), is(0L));
		User foo = repo.create(new User("foo", "foopass"));
		log.info("{}", foo);
		assertThat(repo.count(), is(1L));
		foo.setPassword("barpass");
		repo.create(foo);
	}
}
