package com.example.demo.Dao;
import com.example.demo.ConnectionFactory;
import com.example.demo.controller.HelloController;
import com.example.demo.object.Item;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class Dao {
    static  Connection conn = null;
    static Statement stmt = null;
    static {
        try {
            conn = ConnectionFactory.get_connection();
            stmt = conn.createStatement();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }
    }
    public static void flush_connection()
    {
        try
        {
            stmt.close();
            conn.close();
            conn = ConnectionFactory.get_connection();
            stmt = conn.createStatement();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static int get_id_by_title(String title,int uid) throws SQLException {
        LinkedList<Integer> res = new LinkedList<Integer>();
        String sql = "select search_id from search_table where search_title = '"+ title.replaceAll("'","''")+"' and user_id = "+uid;

        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("search_id");
            return id;
        }
        rs.close();
        return -1;
    }
    public static List<Item> query_all(int u_id) throws SQLException {
        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table where user_id = "+ u_id;
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("search_id");
            String title = rs.getString("search_title");
            String content = rs.getString("search_content");
            String file = rs.getString("search_file");
            int user_id = rs.getInt("user_id");
            // 输出数据
            res.add(new Item(id,title,content,file,user_id));
        }
        rs.close();
        return res;
    }
    public static List<Item> query(String word, int uid) throws SQLException {
        word = word.replaceAll("'","''");
        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table where user_id = "+uid+" and search_title like '%"+word+"%'";

        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("search_id");
            String title = rs.getString("search_title");
            String content = rs.getString("search_content");
            String file = rs.getString("search_file");
            int user_id= rs.getInt("user_id");
            // 输出数据
            res.add(new Item(id,title,content,file,user_id));
        }
        rs.close();
        return res;
    }

    public static boolean alter(Item item) throws SQLException {
        int res = 0;
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        String sql = "UPDATE search_table SET search_title = '"+item.getTitle()+"', search_content = '"+item.getContent()+"', search_file = '"+item.getFile()+"' WHERE search_id = "+item.getId();

        res = stmt.executeUpdate(sql);

        return (res == 1?true:false );
    }
    public static boolean delete_item_by_id(int id_out) throws SQLException {
        int res = 0;
        String sql = "DELETE FROM  search_table WHERE  search_id = "+id_out;

        res = stmt.executeUpdate(sql);

        return (res == 1?true:false );
    }
    public  static boolean insert(Item item) throws SQLException {
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        int res = 0;
        String sql = "INSERT INTO search_table(search_title,search_content,search_file,user_id)  VALUES('"+item.getTitle()+ "','"+item.getContent()+"','"+item.getFile()+"',"+item.getUid()+")";

        res = stmt.executeUpdate(sql);
        return (res == 1?true:false );
    }
    public  static Item query_by_id( int out_id) throws SQLException {
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
            // 输出数据
            res_item = new Item(id,title,content,file,user_id);
        }
        rs.close();
        return res_item;
    }
}
