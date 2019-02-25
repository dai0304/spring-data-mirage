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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;
import com.miragesql.miragesql.SqlManager;

import jp.xet.sparwings.spring.data.repository.ScannableRepository;
import jp.xet.springframework.data.mirage.repository.support.MirageRepositoryFactory;

import jp.xet.springframework.data.mirage.repository.example.User;
import jp.xet.springframework.data.mirage.repository.example.UserRepository;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Transactional
@SuppressWarnings("javadoc")
public class DefaultMirageRepositoryTest {
	
	@Autowired
	SqlManager sqlManager;
	
	
	// ScannableRepository
	
	@Test
	public void testCount() {
		// setup
		RepositoryFactorySupport factory = new MirageRepositoryFactory(sqlManager);
		UserRepository repos = factory.getRepository(UserRepository.class);
		
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		
		// exercise
		long actual = repos.count();
		
		// verify
		assertThat("count", actual, is(3L));
	}
	
	@Test
	public void testFindAll() {
		// setup
		RepositoryFactorySupport factory = new MirageRepositoryFactory(sqlManager);
		UserRepository repos = factory.getRepository(UserRepository.class);
		
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		
		// exercise
		Iterable<User> all = repos.findAll();
		
		// verify
		assertThat("findAll size", Iterables.size(all), is(3));
		assertThat("findAll", all, hasItem(new User("foo", null)));
		assertThat("findAll", all, hasItem(new User("bar", null)));
		assertThat("findAll", all, hasItem(new User("baz", null)));
	}
	
	@Test
	public void testFindAllSort() {
		// setup
		RepositoryFactorySupport factory = new MirageRepositoryFactory(sqlManager);
		UserRepository repos = factory.getRepository(UserRepository.class);
		
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		
		// exercise
		Iterable<User> all = repos.findAll(Sort.by(Order.asc("username")));
		
		// verify
		assertThat("findAllSort size", Iterables.size(all), is(3));
		Iterator<User> itr = all.iterator();
		assertThat("findAllSort 1", itr.next(), is(new User("bar", null)));
		assertThat("findAllSort 2", itr.next(), is(new User("baz", null)));
		assertThat("findAllSort 3", itr.next(), is(new User("foo", null)));
	}
	
	@Test
	public void testFindAllIterable() {
		// setup
		RepositoryFactorySupport factory = new MirageRepositoryFactory(sqlManager);
		UserRepository repos = factory.getRepository(UserRepository.class);
		
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		
		// exercise
		Iterable<User> all = repos.findAll(Arrays.asList("foo", "baz"));
		
		// verify
		assertThat("findAll size", Iterables.size(all), is(2));
		assertThat("findAll", all, hasItem(new User("foo", null)));
		assertThat("findAll", all, not(contains(new User("bar", null))));
		assertThat("findAll", all, hasItem(new User("baz", null)));
	}
}
