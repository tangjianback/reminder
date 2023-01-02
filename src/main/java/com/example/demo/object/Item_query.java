package com.example.demo.object;

import java.util.Objects;

public class Item_query implements Comparable<Item_query>{
    Item item;
    int cache_in_count;

    public Item_query(Item item, int cache_in_count) {
        this.item = item;
        this.cache_in_count = cache_in_count;
    }

    public Item getItem() {
        return item;
    }

    public int getCache_in_count() {
        return cache_in_count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item_query that = (Item_query) o;
        return cache_in_count == that.cache_in_count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cache_in_count);
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setCache_in_count(int cache_in_count) {
        this.cache_in_count = cache_in_count;
    }

    @Override
    public String toString() {
        return "Item_query{" +
                "item=" + item +
                ", cache_in_count=" + cache_in_count +
                '}';
    }

    @Override
    public int compareTo(Item_query o) {
        if(this.cache_in_count < o.getCache_in_count())
            return 1;
        if(this.cache_in_count > o.getCache_in_count())
            return -1;
        return 0;
    }
}
