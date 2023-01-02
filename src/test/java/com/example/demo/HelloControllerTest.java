package com.example.demo;


import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.Dao.Dao;
import com.example.demo.object.Item;
import com.example.demo.service.Service;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.sql.*;
import java.util.TreeMap;
import java.util.TreeSet;

@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest {
    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/search?useUnicode=true&characterEncoding=utf-8&useSSL=false";

    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
    //static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    //static final String DB_URL = "jdbc:mysql://localhost:3306/RUNOOB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "123";

    @Autowired
    private MockMvc mvc;

    @Test
    public void test1() throws Exception
    {
        TreeMap<Item,Integer> my_set = new TreeMap<Item,Integer>();
        TreeSet<Item> my_seet = new TreeSet<Item>();

        //my_set.put(new Item(1,"nihao","",""), 1);
        Item s1 = new Item(1,"nihao","","");
        Item s3 = new Item(3,"nihao","","");
        Item s4 = new Item(4,"nihao","","");
        Item s5 = new Item(5,"nihao","","");

        my_seet.add(s3);
        my_seet.add(s4);
        my_seet.add(s5);

        //my_set.put(s1,1);
        my_set.put(s3,2);
        my_set.put(s4,3);
        my_set.put(s5,4);
        System.out.println(my_set.containsKey(s1));
        System.out.println(my_set.size());
    }

    @Test
    public void getHello() throws Exception {

        Service s = new Service();
        System.out.println("------------------");
        String []words = {"ni","zuo","中国","世界","文明","nini"};
        for(Item i: s.query_key_word(words))
        {
            System.out.println(i);
        }
        Dao.close_connection();
    }
}