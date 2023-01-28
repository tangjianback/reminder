package com.example.demo.object;

import java.util.List;

public class User {
    int u_id;
    String u_name = null;
    String u_pwd = null;
    String mail = null;
    List<Item> u_lru_list = null;
    List<Quick> quicks = null;

    int language = 0; // default english

    public User(int u_id, String u_name, String u_pwd, String mail, List<Item> u_lru_list, List<Quick> quicks, int language) {
        this.u_id = u_id;
        this.u_name = u_name;
        this.u_pwd = u_pwd;
        this.mail = mail;
        this.u_lru_list = u_lru_list;
        this.quicks = quicks;
        this.language = language;
    }

    public int getU_id() {
        return u_id;
    }

    public void setU_id(int u_id) {
        this.u_id = u_id;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getU_name() {
        return u_name;
    }

    public void setU_name(String u_name) {
        this.u_name = u_name;
    }

    public String getU_pwd() {
        return u_pwd;
    }

    public void setU_pwd(String u_pwd) {
        this.u_pwd = u_pwd;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public List<Item> getU_lru_list() {
        return u_lru_list;
    }

    public void setU_lru_list(List<Item> u_lru_list) {
        this.u_lru_list = u_lru_list;
    }

    public List<Quick> getQuicks() {
        return quicks;
    }

    public void setQuicks(List<Quick> quicks) {
        this.quicks = quicks;
    }

    @Override
    public String toString() {
        return "User{" +
                "u_id=" + u_id +
                ", u_name='" + u_name + '\'' +
                ", u_pwd='" + u_pwd + '\'' +
                ", mail='" + mail + '\'' +
                ", u_lru_list=" + u_lru_list +
                ", quicks=" + quicks +
                ", language=" + language +
                '}';
    }
}
