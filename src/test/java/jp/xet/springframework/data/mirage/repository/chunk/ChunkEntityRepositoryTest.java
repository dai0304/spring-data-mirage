package jp.xet.springframework.data.mirage.repository.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ws2ten1.chunks.Chunk;
import org.ws2ten1.chunks.ChunkRequest;
import org.ws2ten1.chunks.Chunkable;

import jp.xet.springframework.data.mirage.repository.TestConfiguration;

/**
 * Test for {@link ChunkEntityRepository}.
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Transactional
@SuppressWarnings("javadoc")
public class ChunkEntityRepositoryTest {
	
	@Autowired
	ChunkEntityRepository repo;
	
	
	@Test
	@Rollback
	public void testChunkingAsc() {
		for (int i = 0; i < 100; i++) {
			repo.create(new ChunkEntity(String.format("foo%03d", i), UUID.randomUUID().toString()));
		}
		
		List<ChunkEntity> resultAsc = new ArrayList<>(100);
		
		Chunkable requestAsc = new ChunkRequest(8);
		do {
			Chunk<ChunkEntity> chunk = repo.findAll(requestAsc);
			resultAsc.addAll(chunk.getContent());
			String paginationToken = chunk.getPaginationToken();
			log.info("token: {}", paginationToken);
			requestAsc = chunk.hasNext() ? chunk.nextChunkable() : null;
		} while (requestAsc != null);
		
		resultAsc.forEach(e -> log.info("{}", e));
	}
	
	@Test
	@Rollback
	public void testChunkingAscBack() {
		for (int i = 0; i < 100; i++) {
			repo.create(new ChunkEntity(String.format("foo%03d", i), UUID.randomUUID().toString()));
		}
		
		Chunkable requestAsc = new ChunkRequest(8);
		Chunk<ChunkEntity> chunk1 = repo.findAll(requestAsc);
		Chunk<ChunkEntity> chunk2 = repo.findAll(chunk1.nextChunkable());
		
		Chunk<ChunkEntity> chunk1again = repo.findAll(chunk2.prevChunkable());
		
		log.info("1");
		chunk1.forEach(e -> log.info("{}", e));
		log.info("2");
		chunk2.forEach(e -> log.info("{}", e));
		log.info("1again");
		chunk1again.forEach(e -> log.info("{}", e));
	}
	
	@Test
	@Rollback
	public void testChunkingDesc() {
		for (int i = 0; i < 100; i++) {
			repo.create(new ChunkEntity(String.format("foo%03d", i), UUID.randomUUID().toString()));
		}
		
		List<ChunkEntity> resultDesc = new ArrayList<>(100);
		
		Chunkable requestDesc = new ChunkRequest(8, Direction.DESC);
		do {
			Chunk<ChunkEntity> chunk = repo.findAll(requestDesc);
			resultDesc.addAll(chunk.getContent());
			String paginationToken = chunk.getPaginationToken();
			log.info("token: {}", paginationToken);
			requestDesc = chunk.hasNext() ? chunk.nextChunkable() : null;
		} while (requestDesc != null);
		
		resultDesc.forEach(e -> log.info("{}", e));
	}
}
