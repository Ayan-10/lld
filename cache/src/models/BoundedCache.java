package models;

import eviction.EvictionPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BoundedCache<K,V> implements Cache<K,V>{
  private final int capacity;
  private final Map<K,V> map;
  private final EvictionPolicy<K> evictionPolicy;

  public BoundedCache(int capacity, EvictionPolicy<K> evictionPolicy) {
    this.capacity = capacity;
    this.map = new HashMap<>();
    this.evictionPolicy = evictionPolicy;
  }

  @Override
  public Optional<V> get(K key) {
    if (map.containsKey(key)){
      evictionPolicy.recordAccess(key);
      return Optional.of(map.get(key));
    }

    return Optional.empty();
  }

  @Override
  public void put(K key, V value) {
    if (map.containsKey(key)){
      map.put(key, value);
      evictionPolicy.recordAccess(key);
    } else {
      if (map.size() >= capacity) {
        K victim = evictionPolicy.evictCandidate();

        map.remove(victim);
        evictionPolicy.remove(victim);
      }

      map.put(key, value);
      evictionPolicy.recordInsert(key);
    }
  }
}
