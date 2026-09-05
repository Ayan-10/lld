package factory;

import eviction.LRUEvictionPolicy;
import models.BoundedCache;
import models.Cache;

public class CacheFactory {
  public static <K, V> Cache<K, V> createCache(String type, int capacity) {

    if ("LRU".equalsIgnoreCase(type)) {
      return new BoundedCache<>(capacity, new LRUEvictionPolicy<>());
    }

    throw new IllegalArgumentException("Unsupported cache type: " + type);
  }
}
