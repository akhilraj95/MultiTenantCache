package com.cornell.multitenantcache;

import lombok.*;


import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.*;

import static java.util.logging.Level.INFO;

public class MultitenantCache<K extends Serializable, D extends Serializable> implements Cache<K, D> {

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    HashMap<String, LRUMap<K,D>> cache;
    Map<String, CacheState> cacheState;
    Duration isolationGurantee;

    // Meta
    private int chunkCount;

    public MultitenantCache(Duration isolationGurantee,
                            MultitenantCachConfig config) {

        setupLogger();
        this.isolationGurantee = isolationGurantee;

        Map<String, Integer> clientCacheCount = config.getClientCacheCount();
        cacheState = new HashMap<>();
        cache = new HashMap<>();

        for (String clientID : clientCacheCount.keySet()) {
            int totalCacheSize = clientCacheCount.get(clientID);
            cacheState.put(clientID, new CacheState(0, totalCacheSize, totalCacheSize));
            cache.put(clientID, new LRUMap<>());
            chunkCount += totalCacheSize;
        }
    }

    @Override
    public boolean isPresent(String clientId, K key) {
        return cache.get(clientId).containsKey(key);
    }

    public Optional<D> read(String clientId, K key) {
        logger.log(INFO, "CLIENT_CACHE_STATE," + getCacheState(cacheState));
        if (isPresent(clientId,key)) {
            logger.log(INFO, "LOG,READ,HIT," + key);
            return Optional.of(cache.get(clientId).get(key));
        }
        logger.log(INFO, "LOG,READ,MISSED," + key);
        return Optional.empty();
    }

    public boolean write(String clientId, K key, D data) {

        CacheState state = this.cacheState.get(clientId);
        if(isPresent(clientId, key)) return true;

        logger.log(INFO, "CLIENT_CACHE_STATE," + getCacheState(cacheState));
        if (state.getAvailableCount() > 0) {

            logger.log(INFO, "LOG,WRITE,AVAILABLE," + key);
            cache.get(clientId).put(key, data);
            state.incrementActiveCount();
            state.decrementAvailableCount();
            return true;

        } else {

            boolean cached = borrowAuxiliarySpace(clientId, key, data);
            if(cached) {
                logger.log(INFO, "LOG,WRITE,BORROWED," + key);
                return true;
            }

            cached = stealAuxiliarySpace(clientId, key, data);
            if(cached) {
                logger.log(INFO, "LOG,WRITE,STOLEN," + key);
                return true;
            }

            cached = reclaimAndCache(clientId, key, data);
            if(cached) {
                logger.log(INFO, "LOG,WRITE,RECLAIMED," + key);
                return true;
            }

            cached = cacheAfterEviction(clientId, key, data);
            if(cached) {
                logger.log(INFO, "LOG,WRITE,STOLEN," + key);
                return true;
            }

            logger.log(INFO, "LOG,WRITE,FAIL," + key);
            return false;
        }
    }

    private boolean reclaimAndCache(String clientId, K key, D data) {

        Map<String, Integer> borrowerList = cacheState.get(clientId).getBorrowerList();
        for(String borrower: borrowerList.keySet()) {
            if(borrowerList.get(borrower) > 0) {
                cache.get(borrower).removeFirst();
                cache.get(clientId).put(key, data);
                cacheState.get(borrower).decrementActiveCount();
                cacheState.get(clientId).incrementActiveCount();
                return true;
            }
        }
        return false;
    }

    private boolean borrowAuxiliarySpace(String clientId, K key, D data) {

        int maxAvailable = 0;
        String selectedBorrower = clientId;
        for(String borrowableClient: cache.keySet()) {
            if(borrowableClient != clientId) {
                int availableCount = cacheState.get(borrowableClient).getAvailableCount();
                if(availableCount > maxAvailable) {
                    maxAvailable = availableCount;
                    selectedBorrower = borrowableClient;
                }
            }
         }

        if (maxAvailable == 0) return false;

        cacheState.get(clientId).incrementActiveCount();
        cacheState.get(selectedBorrower).decrementAvailableCount();
        cacheState.get(selectedBorrower).addBorrower(clientId);
        cache.get(clientId).put(key,data);
        return true;
    }

    // TODO: 18/11/19 Make Thread Safe
    private boolean stealAuxiliarySpace(String clientId, K key, D data) {

        Instant now = Instant.now();
        Instant oldestTimestamp = cache.get(clientId).getOldestLastAccessTime();
        String selectedClient = null;
        for(String stealableClient: cache.keySet()) {
            if(stealableClient != clientId) {
                Instant oldestLastAccessTime = cache.get(stealableClient).getOldestLastAccessTime();
                if(Duration.between(oldestLastAccessTime, now).compareTo(isolationGurantee) > 0
                        && oldestLastAccessTime.compareTo(oldestTimestamp) < 0) {
                    oldestTimestamp = oldestLastAccessTime;
                    selectedClient = stealableClient;
                }
            }
        }
        if(selectedClient != null && steal(selectedClient, clientId, key, data)) {
            return true;
        }
        return false;
    }

    // TODO: 18/11/19 Make Thread Safe
    private boolean steal(String stealableClient, String clientId, K key, D data) {

        cache.get(stealableClient).removeFirst();
        cache.get(clientId).put(key, data);

        cacheState.get(stealableClient).decrementActiveCount();
        cacheState.get(stealableClient).addBorrower(clientId);
        cacheState.get(clientId).incrementActiveCount();
        return true;
    }

    private boolean cacheAfterEviction(String clientId, K key, D data) {
        cache.get(clientId).removeFirst();
        cache.get(clientId).put(key,data);
        return true;
    }


    @Getter
    @RequiredArgsConstructor()
    class CacheState {
        @NonNull private int activeCount;
        @NonNull private int availableCount;
        @NonNull private int totalCount;
        private Map<String, Integer> borrowerList = new HashMap<>();

        public void decrementAvailableCount() {
            this.availableCount -= 1;
        }

        public void incrementAvailableCount() {
            this.availableCount += 1;
        }

        public void decrementActiveCount() {
            this.activeCount -= 1;
        }

        public void incrementActiveCount() {
            this.activeCount += 1;
        }

        //// TODO: 18/11/19 Synchronize
        public void addBorrower(String borrowerClientID) {
            borrowerList.put(borrowerClientID, borrowerList.getOrDefault(borrowerClientID,0) + 1);
        }

        //// TODO: 18/11/19 Synchronize
        public void removeBorrower(String borrowerClientID) {
            if(!borrowerList.containsKey(borrowerClientID) || borrowerList.get(borrowerClientID).equals(0)) {
                throw new RuntimeException("Removing borrower that does not exits");
            }
            borrowerList.put(borrowerClientID, borrowerList.get(borrowerClientID) - 1);
        }

        public String toString() {
            return String.valueOf(activeCount)
                    + "," + String.valueOf(availableCount)
                    + "," + String.valueOf(totalCount);
        }
    }

    private void setupLogger() {
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("./logfile.log");
            fileHandler.setFormatter(new SimpleFormatter() {
                                         private static final String format = "%s,%s\n";

                                         @Override
                                         public synchronized String format(LogRecord lr) {
                                             return String.format(format,
                                                     Instant.now().toEpochMilli(),
                                                     lr.getMessage()
                                             );
                                         }
                                     });
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Logging to file failed");
        }
    }

    private String getCacheState(Map<String, CacheState> cacheState) {
        String result = "";
        for(String id: cacheState.keySet()) {
            result = result + cacheState.get(id).getAvailableCount() + ","
                            + cacheState.get(id).getActiveCount() + ","
                            + cacheState.get(id).getTotalCount()+ ",";
        }
        return result;
    }

}
