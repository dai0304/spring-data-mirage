/*
 * Copyright 2011 Daisuke Miyamoto.
 * Created on 2012/12/09
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link UserRepository}.
 * 
 * @author daisuke
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:/test-context.xml")
@Transactional
@SuppressWarnings("javadoc")
public class UserRepositoryTest {
	
	@Autowired
	UserRepository repos;
	
	
	@Test
	public void test() {
		assertThat(repos, is(notNullValue()));
		assertThat(repos.count(), is(0L));
		repos.save(new User("foo", "foopass"));
		assertThat(repos.count(), is(1L));
		repos.save(new User("bar", "barpass"));
		assertThat(repos.count(), is(2L));
		
		User foundFoo = repos.findOne("foo");
		assertThat(foundFoo.getPassword(), is("foopass"));
	}
}
