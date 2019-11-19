package com.cornell.multitenantcache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SpringBootTest
class MultitenantCacheApplicationTests {

	@Test
	void cacheInBorrowedAuxillarySpaceWhenAvailableTest() throws IOException {

		MultitenantCachConfig config = MultitenantCachConfig.builder()
											  .client("A", 4)
											  .client("B", 2)
											  .client("C", 3)
											  .build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(Duration.ofSeconds(1), config);

		assertTrue(cache.write("A", "key1A", "value1A"));
		assertTrue(cache.write("A", "key2A", "value2A"));
		assertTrue(cache.write("A", "key3A", "value3A"));
		assertTrue(cache.write("A", "key4A", "value4A"));
		assertTrue(cache.write("A", "key5A", "value5A"));
		assertEquals(5, cache.cacheState.get("A").getActiveCount());
		assertEquals(2, cache.cacheState.get("C").getAvailableCount());
		assertEquals(2, cache.cacheState.get("B").getAvailableCount());

	}

	@Test
	void cacheReadTest() throws IOException {

		MultitenantCachConfig config = MultitenantCachConfig.builder()
											   .client("A", 4)
											   .client("B", 2)
											   .client("C", 3)
											   .build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(Duration.ofSeconds(1), config);

		assertTrue(cache.write("A", "key1A", "value1A"));
		assertSame("value1A", cache.read("A", "key1A").get());

		assertFalse(cache.read("A", "notPresentKey").isPresent());
	}

	@Test
	void cacheIsStolenWhenNotAvailableTest() throws InterruptedException {

		MultitenantCachConfig config = MultitenantCachConfig.builder()
				.client("A", 2)
				.client("B", 2)
				.client("C", 2)
				.build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(Duration.ofSeconds(1), config);
		assertTrue(cache.write("B", "key1A", "value1A"));
		assertTrue(cache.write("B", "key2A", "value2A"));
		TimeUnit.SECONDS.sleep(1);
		assertTrue(cache.write("C", "key3A", "value2A"));
		assertTrue(cache.write("C", "key4A", "value2A"));
		TimeUnit.SECONDS.sleep(1);
		assertTrue(cache.write("A", "key5A", "value3A"));
		assertTrue(cache.write("A", "key6A", "value4A"));
		assertTrue(cache.write("A", "key7A", "value5A"));
		assertEquals(3, cache.cacheState.get("A").getActiveCount());
		assertEquals(1, cache.cacheState.get("B").getActiveCount());
		assertEquals(2, cache.cacheState.get("C").getActiveCount());
		assertEquals(0, cache.cacheState.get("A").getAvailableCount());
		assertEquals(0, cache.cacheState.get("B").getAvailableCount());
		assertEquals(0, cache.cacheState.get("C").getAvailableCount());
	}

	@Test
	void cacheReclaim() throws InterruptedException {

		MultitenantCachConfig config = MultitenantCachConfig.builder()
				.client("A", 2)
				.client("B", 2)
				.client("C", 2)
				.build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(Duration.ofSeconds(1), config);
		assertTrue(cache.write("B", "key1A", "value1A"));
		assertTrue(cache.write("B", "key2A", "value2A"));
		TimeUnit.SECONDS.sleep(1);
		assertTrue(cache.write("C", "key3A", "value2A"));
		assertTrue(cache.write("C", "key4A", "value2A"));
		TimeUnit.SECONDS.sleep(1);
		assertTrue(cache.write("A", "key5A", "value3A"));
		assertTrue(cache.write("A", "key6A", "value4A"));
		assertTrue(cache.write("A", "key7A", "value5A"));

		assertFalse(cache.read("B", "key1A").isPresent());
		assertTrue(cache.read("B", "key2A").isPresent());
		assertTrue(cache.read("C", "key3A").isPresent());
		assertTrue(cache.read("C", "key4A").isPresent());
		assertTrue(cache.read("A", "key5A").isPresent());
		assertTrue(cache.read("A", "key6A").isPresent());
		assertTrue(cache.read("A", "key7A").isPresent());
		assertTrue(cache.write("B", "key3A", "value2A"));

		assertEquals(2, cache.cacheState.get("A").getActiveCount());
		assertEquals(2, cache.cacheState.get("B").getActiveCount());
		assertEquals(2, cache.cacheState.get("C").getActiveCount());
		assertEquals(0, cache.cacheState.get("A").getAvailableCount());
		assertEquals(0, cache.cacheState.get("B").getAvailableCount());
		assertEquals(0, cache.cacheState.get("C").getAvailableCount());

	}

	@Test
	void cacheEvictionIfBorrowStealingAndReclaimFails() throws InterruptedException {
		MultitenantCachConfig config = MultitenantCachConfig.builder()
				.client("A", 2)
				.client("B", 2)
				.client("C", 2)
				.build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(Duration.ofSeconds(10), config);
		assertTrue(cache.write("A", "key1A", "value1A"));
		assertTrue(cache.write("A", "key2A", "value2A"));
		assertTrue(cache.write("B", "key3A", "value3A"));
		assertTrue(cache.write("B", "key4A", "value4A"));
		assertTrue(cache.write("C", "key5A", "value5A"));
		assertTrue(cache.write("C", "key6A", "value6A"));

		assertEquals("value1A", cache.read("A", "key1A").get());
		assertEquals("value2A", cache.read("A", "key2A").get());
		assertEquals("value3A", cache.read("B", "key3A").get());
		assertEquals("value4A", cache.read("B", "key4A").get());
		assertEquals("value5A", cache.read("C", "key5A").get());
		assertEquals("value6A", cache.read("C", "key6A").get());

		assertTrue(cache.write("A", "key7A", "value7A"));
		assertFalse(cache.read("A", "key1A").isPresent());
		assertTrue(cache.read("A", "key2A").isPresent());
		assertTrue(cache.read("A", "key7A").isPresent());
	}

}
