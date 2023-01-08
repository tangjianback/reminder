package com.example.demo.object;

public class Quick_item {
    String content=null;
    String id=null;
    String uuid=null;

    public Quick_item(String content, String id, String uuid) {
        this.content = content;
        this.id = id;
        this.uuid = uuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "Quick_item{" +
                "content='" + content + '\'' +
                ", id='" + id + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
