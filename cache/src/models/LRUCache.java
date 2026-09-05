package models;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LRUCache<K,V> implements Cache<K,V> {

  private final int capacity;
  private final Map<K, Node<K,V>> map;

  private final Node<K,V> head;
  private final Node<K,V> tail;

  public LRUCache(int capacity) {
    this.capacity = capacity;
    this.map = new HashMap<>();
    this.head = new Node<>(null, null);
    this.tail = new Node<>(null, null);
    this.head.next = tail;
    this.tail.prev = head;
  }

  @Override
  public Optional<V> get(K key) {

    if (map.containsKey(key)){
      Node<K,V> node = map.get(key);
      moveToFront(node);
      return Optional.of(node.value);
    }

    return Optional.empty();
  }

  @Override
  public void put(K key, V value) {
    if (map.containsKey(key)){
      Node<K,V> node = map.get(key);
      node.value = value;
      moveToFront(node);
    } else {
      Node<K,V> node = new Node<>(key, value);
      map.put(key, node);
      addToFront(node);

      if (map.size() > capacity){
        evictFromCache();
      }
    }
  }

  private void evictFromCache() {
    Node<K,V> node = tail.prev;
    remove(node);
    map.remove(node.key);
  }

  private void addToFront(Node<K,V> node) {
    node.prev = head;
    node.next = head.next;

    head.next.prev = node;
    head.next = node;
  }

  private void remove(Node<K,V> node) {
    node.prev.next = node.next;
    node.next.prev = node.prev;
  }

  private void moveToFront(Node<K,V> node) {
    remove(node);
    addToFront(node);
  }
}
