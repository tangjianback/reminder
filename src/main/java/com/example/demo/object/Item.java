package com.example.demo.object;

import java.util.Objects;

public class Item implements Comparable<Item>{
    int Id;
    String title;
    String content;
    String file;

    public Item(int id, String title, String content, String file) {
        Id = id;
        this.title = title;
        this.content = content;
        this.file = file;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Id == item.Id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Item{" +
                "Id=" + Id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", file='" + file + '\'' +
                '}';
    }

    @Override
    public int compareTo(Item o) {
        if(this.getId()>o.getId())
            return 1;
        if(this.getId() < o.getId())
            return -1;
        return 0;
    }
}
