import eviction.LRUEvictionPolicy;
import factory.CacheFactory;
import models.BoundedCache;
import models.Cache;
import models.LRUCache;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {
  //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
  // to see how IntelliJ IDEA suggests fixing it.
  IO.println(String.format("Hello and welcome!"));

  testLRUCache();
  testBoundedCache();
  testBoundedCacheWithFactory();
}

private static void testLRUCache() {
  System.out.println("=== Testing LRUCache (Hardcoded DLL) ===");
  // Capacity of 2
  Cache<Integer, String> cache = new LRUCache<>(2);

  cache.put(1, "Data1");
  cache.put(2, "Data2");
  System.out.println("Inserted 1 and 2.");

  // Access 1, making it MRU. 2 becomes LRU.
  System.out.println("Get 1 (Expected Optional[Data1]): " + cache.get(1));

  // Insert 3. Capacity is 2, so it should evict the LRU (which is 2).
  cache.put(3, "Data3");
  System.out.println("Inserted 3. (Should evict 2)");

  // Verify 2 is gone and 3 is present
  System.out.println("Get 2 (Expected Optional.empty): " + cache.get(2));
  System.out.println("Get 3 (Expected Optional[Data3]): " + cache.get(3));
  System.out.println();
}

private static void testBoundedCache() {
  System.out.println("=== Testing BoundedCache (Strategy Pattern) ===");
  // Capacity of 2, passing in the LRU policy
  Cache<Integer, String> cache = new BoundedCache<>(2, new LRUEvictionPolicy<>());

  cache.put(1, "Data1");
  cache.put(2, "Data2");
  System.out.println("Inserted 1 and 2.");

  // Access 1, making it MRU. 2 becomes LRU.
  System.out.println("Get 1 (Expected Optional[Data1]): " + cache.get(1));

  // Insert 3. Capacity is 2, so it should ask the policy for a victim (should be 2).
  cache.put(3, "Data3");
  System.out.println("Inserted 3. (Should evict 2)");

  // Verify 2 is gone and 3 is present
  System.out.println("Get 2 (Expected Optional.empty): " + cache.get(2));
  System.out.println("Get 3 (Expected Optional[Data3]): " + cache.get(3));
  System.out.println();
}

private static void testBoundedCacheWithFactory() {
  System.out.println("=== Testing Cache via Factory ===");

  // The client (Main) now only knows about the Cache interface and the Factory.
  // It has no idea if it's getting a hardcoded DLL LRUCache or a BoundedCache with a Strategy.
  Cache<Integer, String> cache = CacheFactory.createCache("LRU", 2);

  cache.put(1, "Data1");
  cache.put(2, "Data2");
  System.out.println("Inserted 1 and 2.");

  // Access 1, making it MRU. 2 becomes LRU.
  System.out.println("Get 1 (Expected Optional[Data1]): " + cache.get(1));

  // Insert 3. Capacity is 2, so it should evict the LRU (which is 2).
  cache.put(3, "Data3");
  System.out.println("Inserted 3. (Should evict 2)");

  // Verify 2 is gone and 3 is present
  System.out.println("Get 2 (Expected Optional.empty): " + cache.get(2));
  System.out.println("Get 3 (Expected Optional[Data3]): " + cache.get(3));
}
