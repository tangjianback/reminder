package com.example.demo.object;

import java.util.Objects;

public class Item implements Comparable<Item>{
    int Id;
    String title;
    String content;
    String file;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    int uid;
    int category;

    public Item(int id, String title, String content, String file, int uid, int category, int publics, String position, int level) {
        Id = id;
        this.title = title;
        this.content = content;
        this.file = file;
        this.uid = uid;
        this.category = category;
        this.publics = publics;
        this.position = position;
        this.level = level;
    }

    int publics;
    String position;
    int level;

    public int getPublics() {
        return publics;
    }

    public void setPublics(int publics) {
        this.publics = publics;
    }



    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

//    public Item(int id, String title, String content, String file, int uid, int category, int publics) {
//        Id = id;
//        this.title = title;
//        this.content = content;
//        this.file = file;
//        this.uid = uid;
//        this.category = category;
//        this.publics = publics;
//    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
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

    @Override
    public String toString() {
        return "Item{" +
                "Id=" + Id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", file='" + file + '\'' +
                ", uid=" + uid +
                ", category=" + category +
                ", publics=" + publics +
                ", position='" + position + '\'' +
                ", level=" + level +
                '}';
    }

    public void setFile(String file) {
        this.file = file;
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
