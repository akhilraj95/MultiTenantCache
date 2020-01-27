package com.cornell.multitenantcache;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

class MultitenantCacheApplicationTests {

	@Test
	void cacheInBorrowedAuxillarySpaceWhenAvailableTest() throws IOException {

		MultitenantCachConfig config = MultitenantCachConfig.builder()
											  .client("A", 4)
											  .client("B", 2)
											  .client("C", 3)
											  .build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(config);

		assertTrue(cache.write("A", "key1A", "value1A"));
		assertTrue(cache.write("A", "key2A", "value2A"));
		assertTrue(cache.write("A", "key3A", "value3A"));
		assertTrue(cache.write("A", "key4A", "value4A"));
		assertTrue(cache.write("A", "key5A", "value5A"));
		cache.logCacheState();
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
		MultitenantCache<String, String> cache = new MultitenantCache<>(config);

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
		MultitenantCache<String, String> cache = new MultitenantCache<>(config);
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
		MultitenantCache<String, String> cache = new MultitenantCache<>(config);
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
				.isolationGurantee(Duration.ofSeconds(10))
				.client("A", 2)
				.client("B", 2)
				.client("C", 2)
				.build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(config);
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

	@Test
	void cacheLongTermTest() {

		List<String> keys = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			keys.add("Key" + i);
		}
		MultitenantCachConfig config = MultitenantCachConfig.builder()
				.isolationGurantee(Duration.ofSeconds(10))
				.client("A", 2)
				.client("B", 2)
				.client("C", 2)
				.build();
		MultitenantCache<String, String> cache = new MultitenantCache<>(config);

		Random rand = new Random();

		for (int i = 0; i < 1000; i++) {
			int randInt = rand.nextInt(100000);
			int randkey = rand.nextInt(8);
			if(randInt % 100 < 30) {
				cache.write("A", keys.get(randkey%3), "value1A");
			}
			else if(randInt % 100 < 40) {
				cache.write("B", keys.get(4+(randkey%3)), "value1A");
			}
			else if(randInt % 100 < 50) {
				cache.write("C", keys.get(8+(randkey%3)), "value1A");
			}
			else if(randInt % 100 < 80) {
				cache.write("A", keys.get((randkey%3)), "value1A");
			}
			else if(randInt % 100 < 90) {
				cache.write("B", keys.get(4+(randkey%3)), "value1A");
			}
			else {
				cache.write("C", keys.get(8+(randkey%3)), "value1A");
			}

		}
	}

}
