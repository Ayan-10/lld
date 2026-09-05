package eviction;

public interface EvictionPolicy<K> {
  // Called on every get/update; the policy updates its usage bookkeeping.
  void recordAccess(K key);

  // Called on every new insert.
  void recordInsert(K key);

  // Return the key to evict next (e.g., LRU: least-recently-used).
  K evictCandidate();

  // Drop a key the policy is tracking (on explicit removal).
  void remove(K key);
}