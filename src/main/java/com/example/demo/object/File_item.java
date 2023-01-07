package com.example.demo.object;

public class File_item {
    String file;
    String short_name;

    public File_item(String file, String short_name) {
        this.file = file;
        this.short_name = short_name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    @Override
    public String toString() {
        return "File_item{" +
                "file='" + file + '\'' +
                ", short_name='" + short_name + '\'' +
                '}';
    }
}
