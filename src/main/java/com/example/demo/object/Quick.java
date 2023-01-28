package com.example.demo.object;

public class Quick {
    int id;
    String title;
    int relevant_item_id;
    int uid;

    public Quick(int id, String title, int relevant_item_id, int uid) {
        this.id = id;
        this.title = title;
        this.relevant_item_id = relevant_item_id;
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRelevant_item_id() {
        return relevant_item_id;
    }

    public void setRelevant_item_id(int relevant_item_id) {
        this.relevant_item_id = relevant_item_id;
    }

    @Override
    public String toString() {
        return "Quick{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", relevant_item_id=" + relevant_item_id +
                ", uid=" + uid +
                '}';
    }
}
