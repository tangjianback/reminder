package com.example.demo.Dao;
import com.example.demo.object.Item;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class Dao {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

//    static final String DB_URL = "jdbc:mysql://localhost:3306/search?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    static final String DB_URL = "jdbc:mysql://172.17.0.2:3306/search?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    static final String USER = "tangjian";
    static final String PASS = "123";
    static  Connection conn = null;
    static Statement stmt = null;
    static {
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
    }
    public static LinkedList<Integer> get_id_by_title(String title)
    {
        LinkedList<Integer> res = new LinkedList<Integer>();
        String sql = "select * from search_table where search_title = '"+ title.replaceAll("'","''")+"'";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("search_id");
                res.add(id);
            }
            rs.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
        return res;
    }
    public static List<Item> query_all() {
        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("search_id");
                String title = rs.getString("search_title");
                String content = rs.getString("search_content");
                String file = rs.getString("search_file");
                // 输出数据
                res.add(new Item(id,title,content,file));
            }
            rs.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
        return res;
    }
    public static List<Item> query(String word) {
        word = word.replaceAll("'","''");
        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table where search_title like '%"+word+"%'";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("search_id");
                String title = rs.getString("search_title");
                String content = rs.getString("search_content");
                String file = rs.getString("search_file");
                // 输出数据
                res.add(new Item(id,title,content,file));
            }
            rs.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
        return res;
    }

    public static boolean alter(Item item)
    {
        int res = 0;
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        String sql = "UPDATE search_table SET search_title = '"+item.getTitle()+"', search_content = '"+item.getContent()+"', search_file = '"+item.getFile()+"' WHERE search_id = "+item.getId();
        try {
            res = stmt.executeUpdate(sql);
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
        return (res == 1?true:false );
    }
    public static boolean delete(Item item)
    {
        int res = 0;
        String sql = "DELETE FROM  search_table WHERE  search_id = "+item.getId();
        try {
            res = stmt.executeUpdate(sql);
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
        return (res == 1?true:false );
    }
    public  static boolean insert(Item item)
    {
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        int res = 0;
        String sql = "INSERT INTO search_table(search_title,search_content,search_file)  VALUES('"+item.getTitle()+ "','"+item.getContent()+"','"+item.getFile()+"')";
        try {
            res = stmt.executeUpdate(sql);
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
        return (res == 1?true:false );
    }
    public  static Item query_by_id(Item item)
    {
        Item res_item = null;
        String sql = "select * from search_table where search_id = "+ item.getId();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("search_id");
                String title = rs.getString("search_title");
                String content = rs.getString("search_content");
                String file = rs.getString("search_file");
                // 输出数据
                res_item = new Item(id,title,content,file);
            }
            rs.close();
        } catch (SQLException se) {
        // 处理 JDBC 错误
        se.printStackTrace();
         } catch (Exception e) {
        // 处理 Class.forName 错误
        e.printStackTrace();
        }
        return res_item;
    }
    public  static void close_connection()
    {
        // 关闭资源
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException se2) {
        }// 什么都不做
        try {
            if (conn != null) conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
