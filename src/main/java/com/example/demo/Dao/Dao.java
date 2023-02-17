package com.example.demo.Dao;
import com.example.demo.ConnectionFactory;
import com.example.demo.controller.HelloController;
import com.example.demo.object.Item;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class Dao {
    public static void flush_connection()
    {

    }
    public static int get_id_by_title(String title,int uid) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        int res_id = -1;
        LinkedList<Integer> res = new LinkedList<Integer>();
        String sql = "select search_id from search_table where search_title = '"+ title.replaceAll("'","''")+"' and user_id = "+uid;

        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            // 通过字段检索
            res_id = rs.getInt("search_id");
        }
        rs.close();
        stmt.close();
        conn.close();
        return res_id;
    }
    public static int get_item_total(int u_id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        LinkedList<Item> res = new LinkedList<>();
        String sql = "select count(*) as total from search_table where user_id = "+ u_id;
        ResultSet rs = stmt.executeQuery(sql);
        int res_count = 0;
        while (rs.next()) {
            // 通过字段检索
            res_count = rs.getInt("total");
        }
        rs.close();
        stmt.close();
        conn.close();
        return res_count;
    }
    public static List<Item> get_items_by_page(int query_page, int page_size, int u_id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table where user_id = "+ u_id+ " limit "+ query_page*page_size+","+page_size;
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("search_id");
            String title = rs.getString("search_title");
            String content = rs.getString("search_content");
            String file = rs.getString("search_file");
            int user_id = rs.getInt("user_id");
            int cate_id = rs.getInt("category");
            int publics = rs.getInt("public");
            // 输出数据
            res.add(new Item(id,title,content,file,user_id,cate_id,publics));
        }
        rs.close();
        stmt.close();
        conn.close();
        return res;
    }
    public static List<Item> query(String word, int uid) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        word = word.replaceAll("'","''");
        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table where (user_id = "+uid+" or public = 1) and search_title like '%"+word+"%'";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("search_id");
            String title = rs.getString("search_title");
            String content = rs.getString("search_content");
            String file = rs.getString("search_file");
            int user_id= rs.getInt("user_id");
            int cate_id = rs.getInt("category");
            int publics = rs.getInt("public");
            // 输出数据
            res.add(new Item(id,title,content,file,user_id,cate_id,publics));
        }
        rs.close();
        stmt.close();
        conn.close();
        return res;
    }

    public static boolean alter(Item item) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        int res = 0;
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        String sql = "UPDATE search_table SET search_title = '"+item.getTitle()+"', search_content = '"+item.getContent()+"', search_file = '"+item.getFile()+"' WHERE search_id = "+item.getId();

        res = stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
        return (res == 1?true:false );
    }
    public static boolean delete_item_by_id(int id_out) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        int res = 0;
        String sql = "DELETE FROM  search_table WHERE  search_id = "+id_out;

        res = stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
        return (res == 1?true:false );
    }
    public  static boolean insert(Item item) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        int res = 0;
        String sql = "INSERT INTO search_table(search_title,search_content,search_file,user_id,category,public)  VALUES('"+item.getTitle()+ "','"+item.getContent()+"','"+item.getFile()+"',"+item.getUid()+","+item.getCategory()+","+item.getPublics()+")";
        res = stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
        return (res == 1?true:false );
    }
    public  static Item query_by_id( int out_id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        Item res_item = null;
        String sql = "select * from search_table where search_id = "+ out_id;

        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("search_id");
            String title = rs.getString("search_title");
            String content = rs.getString("search_content");
            String file = rs.getString("search_file");
            int user_id = rs.getInt("user_id");
            int cate_id = rs.getInt("category");
            int publics = rs.getInt("public");
            // 输出数据
            res_item = new Item(id,title,content,file,user_id,cate_id,publics);
        }
        rs.close();
        stmt.close();
        conn.close();
        return res_item;
    }
}
