package eviction;

import java.util.LinkedHashSet;

public class LRUEvictionPolicy<K> implements EvictionPolicy<K> {

  private final LinkedHashSet<K> keys = new LinkedHashSet<>();

  @Override
  public void recordAccess(K key) {
    if (keys.remove(key)) {
      keys.add(key); // moves to MRU position
    }
  }

  @Override
  public void recordInsert(K key) {
    keys.add(key);
  }

  @Override
  public K evictCandidate() {
    // The first element is the oldest (LRU)
    if (keys.isEmpty()) return null;
    return keys.iterator().next();
  }

  @Override
  public void remove(K key) {
    keys.remove(key);
  }
}