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
package jp.xet.springframework.data.mirage.repository.chunk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ws2ten1.chunkrequests.ChunkRequest;
import org.ws2ten1.chunkrequests.Chunkable;
import org.ws2ten1.chunkrequests.Direction;
import org.ws2ten1.chunks.Chunk;
import org.ws2ten1.chunks.ChunkFactory;

import com.google.common.collect.Ordering;

import jp.xet.springframework.data.mirage.repository.MirageConfiguration;

/**
 * Test for {@link ChunkEntityRepository}.
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MirageConfiguration.class)
@Transactional
@SuppressWarnings("javadoc")
public class ChunkEntityRepositoryTest {
	
	@Autowired
	ChunkEntityRepository repo;
	
	ChunkFactory chunkFactory = new ChunkFactory();
	
	// ChunkableRepository
	
	
	@Test
	@Rollback
	public void testChunkingAsc() {
		// setup
		for (int i = 0; i < 10; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		List<ChunkEntity> resultAsc = new ArrayList<>(10);
		
		Chunkable requestAsc = new ChunkRequest(8);
		do {
			List<ChunkEntity> list = repo.findAll(requestAsc);
			resultAsc.addAll(list);
			
			Chunk<ChunkEntity> chunk = chunkFactory.createChunk(list, requestAsc);
			String paginationToken = chunk.getPaginationToken();
			log.info("token: {}", paginationToken);
			requestAsc = chunk.hasNext() ? chunk.nextChunkable() : null; // NOPMD
		} while (requestAsc != null);
		
		resultAsc.forEach(e -> log.info("{}", e));
		assertThat(resultAsc).hasSize(10);
		assertThat(Ordering.natural().isStrictlyOrdered(resultAsc)).isTrue();
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(resultAsc)).isFalse();
	}
	
	@Test
	@Rollback
	public void testChunkingAsc_Under20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		
		Chunkable requestAsc = new ChunkRequest(8);
		List<ChunkEntity> list1 = repo.findAll(requestAsc);
		Chunk<ChunkEntity> chunk1 = chunkFactory.createChunk(list1, requestAsc);
		assertThat(chunk1.hasContent()).isTrue();
		assertThat(chunk1.isFirst()).isTrue();
		assertThat(chunk1.isLast()).isFalse();
		assertThat(chunk1.hasNext()).isTrue();
		assertThat(chunk1.nextChunkable()).isNotNull();
		assertThat(chunk1.hasPrevious()).isFalse();
		assertThat(chunk1.previousChunkable()).isNull();
		assertThat(chunk1.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk1.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk1.getContent())).isTrue();
		assertThat(chunk1.getContent().get(0)).isEqualTo(new ChunkEntity("000", 0));
		assertThat(chunk1.getContent().get(7)).isEqualTo(new ChunkEntity("007", 7));
		
		List<ChunkEntity> list2 = repo.findAll(chunk1.nextChunkable());
		Chunk<ChunkEntity> chunk2 = chunkFactory.createChunk(list2, chunk1.nextChunkable());
		assertThat(chunk2.hasContent()).isTrue();
		assertThat(chunk2.isFirst()).isFalse();
		assertThat(chunk2.isLast()).isFalse();
		assertThat(chunk2.hasNext()).isTrue();
		assertThat(chunk2.nextChunkable()).isNotNull();
		assertThat(chunk2.hasPrevious()).isTrue();
		assertThat(chunk2.previousChunkable()).isNotNull();
		assertThat(chunk2.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk2.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk2.getContent())).isTrue();
		assertThat(chunk2.getContent().get(0)).isEqualTo(new ChunkEntity("008", 8));
		assertThat(chunk2.getContent().get(7)).isEqualTo(new ChunkEntity("015", 15));
		
		List<ChunkEntity> list3 = repo.findAll(chunk2.nextChunkable());
		Chunk<ChunkEntity> chunk3 = chunkFactory.createChunk(list3, chunk2.nextChunkable());
		assertThat(chunk3.hasContent()).isTrue();
		assertThat(chunk3.isFirst()).isFalse();
		assertThat(chunk3.isLast()).isTrue();
		assertThat(chunk3.hasNext()).isFalse();
		assertThat(chunk3.nextChunkable()).isNull();
		assertThat(chunk3.hasPrevious()).isTrue();
		assertThat(chunk3.previousChunkable()).isNotNull();
		assertThat(chunk3.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk3.getContent()).hasSize(4);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk3.getContent())).isTrue();
		assertThat(chunk3.getContent().get(0)).isEqualTo(new ChunkEntity("016", 16));
		assertThat(chunk3.getContent().get(3)).isEqualTo(new ChunkEntity("019", 19));
		
		List<ChunkEntity> list2again = repo.findAll(chunk3.previousChunkable());
		Chunk<ChunkEntity> chunk2again = chunkFactory.createChunk(list2again, chunk3.previousChunkable());
		assertThat(chunk2again.hasContent()).isTrue();
		assertThat(chunk2again.isFirst()).isFalse();
		assertThat(chunk2again.isLast()).isFalse();
		assertThat(chunk2again.hasNext()).isTrue();
		assertThat(chunk2again.nextChunkable()).isNotNull();
		assertThat(chunk2again.hasPrevious()).isTrue();
		assertThat(chunk2again.previousChunkable()).isNotNull();
		assertThat(chunk2again.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk2again.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk2again.getContent())).isTrue();
		assertThat(chunk2again.getContent().get(0)).isEqualTo(new ChunkEntity("008", 8));
		assertThat(chunk2again.getContent().get(7)).isEqualTo(new ChunkEntity("015", 15));
		
		List<ChunkEntity> list1again = repo.findAll(chunk2again.previousChunkable());
		Chunk<ChunkEntity> chunk1again =
				chunkFactory.createChunk(list1again, chunk2again.previousChunkable());
		assertThat(chunk1again.hasContent()).isTrue();
		assertThat(chunk1again.isFirst()).isFalse();
		assertThat(chunk1again.isLast()).isFalse();
		assertThat(chunk1again.hasNext()).isTrue();
		assertThat(chunk1again.nextChunkable()).isNotNull();
		assertThat(chunk1again.hasPrevious()).isTrue();
		assertThat(chunk1again.previousChunkable()).isNotNull();
		assertThat(chunk1again.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk1again.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk1again.getContent())).isTrue();
		assertThat(chunk1again.getContent().get(0)).isEqualTo(new ChunkEntity("000", 0));
		assertThat(chunk1again.getContent().get(7)).isEqualTo(new ChunkEntity("007", 7));
		
		List<ChunkEntity> list0 = repo.findAll(chunk1again.previousChunkable());
		Chunk<ChunkEntity> chunk0 = chunkFactory.createChunk(list0, chunk1again.previousChunkable());
		assertThat(chunk0.hasContent()).isFalse();
		assertThat(chunk0.isFirst()).isFalse();
		assertThat(chunk0.isLast()).isTrue();
		assertThat(chunk0.hasNext()).isTrue();
		assertThat(chunk0.nextChunkable()).isNotNull();
		assertThat(chunk0.hasPrevious()).isFalse();
		assertThat(chunk0.previousChunkable()).isNull();
		assertThat(chunk0.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk0.getContent()).hasSize(0);
		
		List<ChunkEntity> list1again2 = repo.findAll(chunk0.nextChunkable());
		Chunk<ChunkEntity> chunk1again2 = chunkFactory.createChunk(list1again2, chunk0.nextChunkable());
		assertThat(chunk1again2.hasContent()).isTrue();
		assertThat(chunk1again2.isFirst()).isTrue();
		assertThat(chunk1again2.isLast()).isFalse();
		assertThat(chunk1again2.hasNext()).isTrue();
		assertThat(chunk1again2.nextChunkable()).isNotNull();
		assertThat(chunk1again2.hasPrevious()).isFalse();
		assertThat(chunk1again2.previousChunkable()).isNull();
		assertThat(chunk1again2.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk1again2.getContent()).hasSize(8);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk1again2.getContent())).isTrue();
		assertThat(chunk1again2.getContent().get(0)).isEqualTo(new ChunkEntity("000", 0));
		assertThat(chunk1again2.getContent().get(7)).isEqualTo(new ChunkEntity("007", 7));
	}
	
	@Test
	@Rollback
	public void testChunkingAsc_Exact20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		
		Chunkable requestAsc = new ChunkRequest(20);
		List<ChunkEntity> list1 = repo.findAll(requestAsc);
		Chunk<ChunkEntity> chunk1 = chunkFactory.createChunk(list1, requestAsc);
		assertThat(chunk1.hasContent()).isTrue();
		assertThat(chunk1.isFirst()).isTrue();
		assertThat(chunk1.isLast()).isFalse();
		assertThat(chunk1.hasNext()).isTrue(); // unknown
		assertThat(chunk1.nextChunkable()).isNotNull();
		assertThat(chunk1.hasPrevious()).isFalse();
		assertThat(chunk1.previousChunkable()).isNull();
		assertThat(chunk1.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk1.getContent()).hasSize(20);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk1.getContent())).isTrue();
		assertThat(chunk1.getContent().get(0)).isEqualTo(new ChunkEntity("000", 0));
		assertThat(chunk1.getContent().get(19)).isEqualTo(new ChunkEntity("019", 19));
		
		List<ChunkEntity> list2 = repo.findAll(chunk1.nextChunkable());
		Chunk<ChunkEntity> chunk2 = chunkFactory.createChunk(list2, chunk1.nextChunkable());
		assertThat(chunk2.hasContent()).isFalse();
		assertThat(chunk2.isFirst()).isFalse();
		assertThat(chunk2.isLast()).isTrue();
		assertThat(chunk2.hasNext()).isFalse();
		assertThat(chunk2.nextChunkable()).isNull();
		assertThat(chunk2.hasPrevious()).isTrue();
		assertThat(chunk2.previousChunkable()).isNotNull();
		assertThat(chunk2.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk2.getContent()).isEmpty();
		
		List<ChunkEntity> list1again = repo.findAll(chunk2.previousChunkable());
		Chunk<ChunkEntity> chunk1again = chunkFactory.createChunk(list1again, chunk2.previousChunkable());
		assertThat(chunk1again.hasContent()).isTrue();
		assertThat(chunk1again.isFirst()).isTrue();
		assertThat(chunk1again.isLast()).isFalse();
		assertThat(chunk1again.hasNext()).isTrue();
		assertThat(chunk1again.nextChunkable()).isNotNull();
		assertThat(chunk1again.hasPrevious()).isTrue();
		assertThat(chunk1again.previousChunkable()).isNotNull();
		assertThat(chunk1again.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk1again.getContent()).hasSize(20);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk1again.getContent())).isTrue();
		assertThat(chunk1again.getContent().get(0)).isEqualTo(new ChunkEntity("000", 0));
		assertThat(chunk1again.getContent().get(19)).isEqualTo(new ChunkEntity("019", 19));
	}
	
	@Test
	@Rollback
	public void testChunkingAsc_Over20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		
		Chunkable requestAsc = new ChunkRequest(30);
		List<ChunkEntity> list = repo.findAll(requestAsc);
		Chunk<ChunkEntity> chunk = chunkFactory.createChunk(list, requestAsc);
		assertThat(chunk.hasContent()).isTrue();
		assertThat(chunk.isFirst()).isTrue();
		assertThat(chunk.isLast()).isTrue();
		assertThat(chunk.hasNext()).isFalse();
		assertThat(chunk.nextChunkable()).isNull();
		assertThat(chunk.hasPrevious()).isFalse();
		assertThat(chunk.previousChunkable()).isNull();
		assertThat(chunk.getDirection()).isEqualTo(Direction.ASC);
		assertThat(chunk.getContent()).hasSize(20);
		assertThat(Ordering.natural().isStrictlyOrdered(chunk.getContent())).isTrue();
		assertThat(chunk.getContent().get(0)).isEqualTo(new ChunkEntity("000", 0));
		assertThat(chunk.getContent().get(19)).isEqualTo(new ChunkEntity("019", 19));
	}
	
	@Test
	@Rollback
	public void testChunkingDesc() {
		for (int i = 0; i < 100; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		
		List<ChunkEntity> resultDesc = new ArrayList<>(100);
		
		Chunkable requestDesc = new ChunkRequest(8, Direction.DESC);
		do {
			List<ChunkEntity> list = repo.findAll(requestDesc);
			Chunk<ChunkEntity> chunk = chunkFactory.createChunk(list, requestDesc);
			resultDesc.addAll(chunk.getContent());
			String paginationToken = chunk.getPaginationToken();
			log.info("token: {}", paginationToken);
			requestDesc = chunk.hasNext() ? chunk.nextChunkable() : null; // NOPMD
		} while (requestDesc != null);
		
		resultDesc.forEach(e -> log.info("{}", e));
		assertThat(resultDesc).hasSize(100);
		assertThat(Ordering.natural().isStrictlyOrdered(resultDesc)).isFalse();
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(resultDesc)).isTrue();
	}
	
	@Test
	@Rollback
	public void testChunkingDesc_Under20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		
		Chunkable requestAsc = new ChunkRequest(8, Direction.DESC);
		List<ChunkEntity> list1 = repo.findAll(requestAsc);
		Chunk<ChunkEntity> chunk1 = chunkFactory.createChunk(list1, requestAsc);
		assertThat(chunk1.hasContent()).isTrue();
		assertThat(chunk1.isFirst()).isTrue();
		assertThat(chunk1.isLast()).isFalse();
		assertThat(chunk1.hasNext()).isTrue();
		assertThat(chunk1.nextChunkable()).isNotNull();
		assertThat(chunk1.hasPrevious()).isFalse();
		assertThat(chunk1.previousChunkable()).isNull();
		assertThat(chunk1.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk1.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk1.getContent())).isTrue();
		assertThat(chunk1.getContent().get(0)).isEqualTo(new ChunkEntity("019", 19));
		assertThat(chunk1.getContent().get(7)).isEqualTo(new ChunkEntity("012", 12));
		
		List<ChunkEntity> list2 = repo.findAll(chunk1.nextChunkable());
		Chunk<ChunkEntity> chunk2 = chunkFactory.createChunk(list2, chunk1.nextChunkable());
		assertThat(chunk2.hasContent()).isTrue();
		assertThat(chunk2.isFirst()).isFalse();
		assertThat(chunk2.isLast()).isFalse();
		assertThat(chunk2.hasNext()).isTrue();
		assertThat(chunk2.nextChunkable()).isNotNull();
		assertThat(chunk2.hasPrevious()).isTrue();
		assertThat(chunk2.previousChunkable()).isNotNull();
		assertThat(chunk2.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk2.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk2.getContent())).isTrue();
		assertThat(chunk2.getContent().get(0)).isEqualTo(new ChunkEntity("011", 11));
		assertThat(chunk2.getContent().get(7)).isEqualTo(new ChunkEntity("004", 4));
		
		List<ChunkEntity> list3 = repo.findAll(chunk2.nextChunkable());
		Chunk<ChunkEntity> chunk3 = chunkFactory.createChunk(list3, chunk2.nextChunkable());
		assertThat(chunk3.hasContent()).isTrue();
		assertThat(chunk3.isFirst()).isFalse();
		assertThat(chunk3.isLast()).isTrue();
		assertThat(chunk3.hasNext()).isFalse();
		assertThat(chunk3.nextChunkable()).isNull();
		assertThat(chunk3.hasPrevious()).isTrue();
		assertThat(chunk3.previousChunkable()).isNotNull();
		assertThat(chunk3.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk3.getContent()).hasSize(4);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk3.getContent())).isTrue();
		assertThat(chunk3.getContent().get(0)).isEqualTo(new ChunkEntity("003", 3));
		assertThat(chunk3.getContent().get(3)).isEqualTo(new ChunkEntity("000", 0));
		
		List<ChunkEntity> list2again = repo.findAll(chunk3.previousChunkable());
		Chunk<ChunkEntity> chunk2again = chunkFactory.createChunk(list2again, chunk3.previousChunkable());
		assertThat(chunk2again.hasContent()).isTrue();
		assertThat(chunk2again.isFirst()).isFalse();
		assertThat(chunk2again.isLast()).isFalse();
		assertThat(chunk2again.hasNext()).isTrue();
		assertThat(chunk2again.nextChunkable()).isNotNull();
		assertThat(chunk2again.hasPrevious()).isTrue();
		assertThat(chunk2again.previousChunkable()).isNotNull();
		assertThat(chunk2again.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk2again.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk2again.getContent())).isTrue();
		assertThat(chunk2again.getContent().get(0)).isEqualTo(new ChunkEntity("011", 11));
		assertThat(chunk2again.getContent().get(7)).isEqualTo(new ChunkEntity("004", 4));
		
		List<ChunkEntity> list1again = repo.findAll(chunk2again.previousChunkable());
		Chunk<ChunkEntity> chunk1again =
				chunkFactory.createChunk(list1again, chunk2again.previousChunkable());
		assertThat(chunk1again.hasContent()).isTrue();
		assertThat(chunk1again.isFirst()).isFalse();
		assertThat(chunk1again.isLast()).isFalse();
		assertThat(chunk1again.hasNext()).isTrue();
		assertThat(chunk1again.nextChunkable()).isNotNull();
		assertThat(chunk1again.hasPrevious()).isTrue();
		assertThat(chunk1again.previousChunkable()).isNotNull();
		assertThat(chunk1again.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk1again.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk1again.getContent())).isTrue();
		assertThat(chunk1again.getContent().get(0)).isEqualTo(new ChunkEntity("019", 19));
		assertThat(chunk1again.getContent().get(7)).isEqualTo(new ChunkEntity("012", 12));
		
		List<ChunkEntity> list0 = repo.findAll(chunk1again.previousChunkable());
		Chunk<ChunkEntity> chunk0 = chunkFactory.createChunk(list0, chunk1again.previousChunkable());
		assertThat(chunk0.hasContent()).isFalse();
		assertThat(chunk0.isFirst()).isFalse();
		assertThat(chunk0.isLast()).isTrue();
		assertThat(chunk0.hasNext()).isTrue();
		assertThat(chunk0.nextChunkable()).isNotNull();
		assertThat(chunk0.hasPrevious()).isFalse();
		assertThat(chunk0.previousChunkable()).isNull();
		assertThat(chunk0.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk0.getContent()).hasSize(0);
		
		List<ChunkEntity> list1again2 = repo.findAll(chunk0.nextChunkable());
		Chunk<ChunkEntity> chunk1again2 = chunkFactory.createChunk(list1again2, chunk0.nextChunkable());
		assertThat(chunk1again2.hasContent()).isTrue();
		assertThat(chunk1again2.isFirst()).isTrue();
		assertThat(chunk1again2.isLast()).isFalse();
		assertThat(chunk1again2.hasNext()).isTrue();
		assertThat(chunk1again2.nextChunkable()).isNotNull();
		assertThat(chunk1again2.hasPrevious()).isFalse();
		assertThat(chunk1again2.previousChunkable()).isNull();
		assertThat(chunk1again2.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk1again2.getContent()).hasSize(8);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk1again2.getContent())).isTrue();
		assertThat(chunk1again2.getContent().get(0)).isEqualTo(new ChunkEntity("019", 19));
		assertThat(chunk1again2.getContent().get(7)).isEqualTo(new ChunkEntity("012", 12));
	}
	
	@Test
	@Rollback
	public void testChunkingDesc_Exact20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		
		Chunkable requestAsc = new ChunkRequest(20, Direction.DESC);
		List<ChunkEntity> list1 = repo.findAll(requestAsc);
		Chunk<ChunkEntity> chunk1 = chunkFactory.createChunk(list1, requestAsc);
		assertThat(chunk1.hasContent()).isTrue();
		assertThat(chunk1.isFirst()).isTrue();
		assertThat(chunk1.isLast()).isFalse();
		assertThat(chunk1.hasNext()).isTrue(); // unknown
		assertThat(chunk1.nextChunkable()).isNotNull();
		assertThat(chunk1.hasPrevious()).isFalse();
		assertThat(chunk1.previousChunkable()).isNull();
		assertThat(chunk1.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk1.getContent()).hasSize(20);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk1.getContent())).isTrue();
		assertThat(chunk1.getContent().get(0)).isEqualTo(new ChunkEntity("019", 19));
		assertThat(chunk1.getContent().get(19)).isEqualTo(new ChunkEntity("000", 0));
		
		List<ChunkEntity> list2 = repo.findAll(chunk1.nextChunkable());
		Chunk<ChunkEntity> chunk2 = chunkFactory.createChunk(list2, chunk1.nextChunkable());
		assertThat(chunk2.hasContent()).isFalse();
		assertThat(chunk2.isFirst()).isFalse();
		assertThat(chunk2.isLast()).isTrue();
		assertThat(chunk2.hasNext()).isFalse();
		assertThat(chunk2.nextChunkable()).isNull();
		assertThat(chunk2.hasPrevious()).isTrue();
		assertThat(chunk2.previousChunkable()).isNotNull();
		assertThat(chunk2.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk2.getContent()).isEmpty();
		
		List<ChunkEntity> list1again = repo.findAll(chunk2.previousChunkable());
		Chunk<ChunkEntity> chunk1again = chunkFactory.createChunk(list1again, chunk2.previousChunkable());
		assertThat(chunk1again.hasContent()).isTrue();
		assertThat(chunk1again.isFirst()).isTrue();
		assertThat(chunk1again.isLast()).isFalse();
		assertThat(chunk1again.hasNext()).isTrue();
		assertThat(chunk1again.nextChunkable()).isNotNull();
		assertThat(chunk1again.hasPrevious()).isTrue();
		assertThat(chunk1again.previousChunkable()).isNotNull();
		assertThat(chunk1again.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk1again.getContent()).hasSize(20);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk1again.getContent())).isTrue();
		assertThat(chunk1again.getContent().get(0)).isEqualTo(new ChunkEntity("019", 19));
		assertThat(chunk1again.getContent().get(19)).isEqualTo(new ChunkEntity("000", 0));
	}
	
	@Test
	@Rollback
	public void testChunkingDesc_Over20() {
		for (int i = 0; i < 20; i++) {
			repo.save(new ChunkEntity(String.format("%03d", i), i));
		}
		
		Chunkable requestAsc = new ChunkRequest(30, Direction.DESC);
		List<ChunkEntity> list = repo.findAll(requestAsc);
		Chunk<ChunkEntity> chunk = chunkFactory.createChunk(list, requestAsc);
		assertThat(chunk.hasContent()).isTrue();
		assertThat(chunk.isFirst()).isTrue();
		assertThat(chunk.isLast()).isTrue();
		assertThat(chunk.hasNext()).isFalse();
		assertThat(chunk.nextChunkable()).isNull();
		assertThat(chunk.hasPrevious()).isFalse();
		assertThat(chunk.previousChunkable()).isNull();
		assertThat(chunk.getDirection()).isEqualTo(Direction.DESC);
		assertThat(chunk.getContent()).hasSize(20);
		assertThat(Ordering.natural().reverse().isStrictlyOrdered(chunk.getContent())).isTrue();
		assertThat(chunk.getContent().get(0)).isEqualTo(new ChunkEntity("019", 19));
		assertThat(chunk.getContent().get(19)).isEqualTo(new ChunkEntity("000", 0));
	}
}
