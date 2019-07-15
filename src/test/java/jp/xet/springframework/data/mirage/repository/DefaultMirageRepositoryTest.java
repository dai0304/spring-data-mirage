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
package jp.xet.springframework.data.mirage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ws2ten1.chunks.Chunk;
import org.ws2ten1.chunks.ChunkRequest;

import com.miragesql.miragesql.SqlManager;

import jp.xet.springframework.data.mirage.repository.appgenerated.User;
import jp.xet.springframework.data.mirage.repository.appgenerated.UserRepository;
import jp.xet.springframework.data.mirage.repository.support.MirageRepositoryFactory;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MirageConfiguration.class)
@Transactional
@SuppressWarnings("javadoc")
public class DefaultMirageRepositoryTest {
	
	@Autowired
	SqlManager sqlManager;
	
	private RepositoryFactorySupport factory;
	
	
	@Before
	public void setUp() throws Exception {
		factory = new MirageRepositoryFactory(sqlManager);
	}
	
	@Test
	public void findAll() {
		// setup
		UserRepository repos = factory.getRepository(UserRepository.class);
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		// exercise
		Iterable<User> actual = repos.findAll();
		// verify
		assertThat(actual).hasSize(3);
		assertThat(actual).contains(new User("foo", "foopass"));
		assertThat(actual).contains(new User("bar", "barpass"));
		assertThat(actual).contains(new User("baz", "bazpass"));
	}
	
	@Test
	public void findAll_Iterable() {
		// setup
		UserRepository repos = factory.getRepository(UserRepository.class);
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		// exercise
		Iterable<User> actual = repos.findAll(Arrays.asList("foo", "baz"));
		// verify
		assertThat(actual).hasSize(2);
		assertThat(actual).contains(new User("foo", "foopass"));
		assertThat(actual).doesNotContain(new User("bar", "barpass"));
		assertThat(actual).contains(new User("baz", "bazpass"));
	}
	
	@Test
	public void findAll_Iterable_Empty() {
		// setup
		UserRepository repos = factory.getRepository(UserRepository.class);
		// exercise
		Iterable<User> actual = repos.findAll(Arrays.asList("foo", "baz"));
		// verify
		assertThat(actual).isEmpty();
	}
	
	@Test
	public void findAll_Chunkable() {
		// setup
		UserRepository repos = factory.getRepository(UserRepository.class);
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		ChunkRequest chunkable = new ChunkRequest(2);
		// exercise
		List<User> actual = repos.findAll(chunkable);
		// verify
		assertThat(actual).hasSize(2);
		assertThat(actual).doesNotContain(new User("foo", "foopass"));
		assertThat(actual).contains(new User("bar", "barpass"));
		assertThat(actual).contains(new User("baz", "bazpass"));
		
		Chunk<User> chunk = repos.getChunkFactory().createChunk(actual, chunkable);
		assertThat(chunk.getPaginationToken()).isNotNull();
	}
	
	@Test
	public void findAll_ChunkableAll() {
		// setup
		UserRepository repos = factory.getRepository(UserRepository.class);
		repos.save(new User("foo", "foopass"));
		repos.save(new User("bar", "barpass"));
		repos.save(new User("baz", "bazpass"));
		ChunkRequest chunkable = new ChunkRequest(5);
		// exercise
		List<User> actual = repos.findAll(chunkable);
		// verify
		assertThat(actual).hasSize(3);
		assertThat(actual).contains(new User("foo", "foopass"));
		assertThat(actual).contains(new User("bar", "barpass"));
		assertThat(actual).contains(new User("baz", "bazpass"));
		
		Chunk<User> chunk = repos.getChunkFactory().createChunk(actual, chunkable);
		assertThat(chunk.getPaginationToken()).isNotNull();
	}
	
	@Test
	public void findAll_ChunkableEmpty_NullPaginationToken() {
		// setup
		UserRepository repos = factory.getRepository(UserRepository.class);
		// exercise
		ChunkRequest chunkable = new ChunkRequest(2);
		List<User> actual = repos.findAll(chunkable);
		// verify
		assertThat(actual).isEmpty();
		Chunk<User> chunk = repos.getChunkFactory().createChunk(actual, chunkable);
		assertThat(chunk.getPaginationToken()).isNull();
	}
}
