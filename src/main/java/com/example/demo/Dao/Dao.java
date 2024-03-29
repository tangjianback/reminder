package com.example.demo.Dao;
import com.example.demo.ConnectionFactory;
import com.example.demo.controller.HelloController;
import com.example.demo.object.Item;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class Dao {

    public static List<Item> get_item_by_path(String path, int uid) throws SQLException
    {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        ResultSet rs = null;
        LinkedList<Item> res = new LinkedList<Item>();
        String sql = "select search_id from search_table where path = '"+ path.replaceAll("'","''")+"' and user_id = "+uid +";";
        try{
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                int temp_item_id  = rs.getInt("search_id");
                res.add(Dao.query_by_id(temp_item_id));
            }
        }catch (SQLException e)
        {
            throw e;
        }finally {
            rs.close();
            stmt.close();
            conn.close();
        }
        return res;
    }

    public static List<String> get_folder_path(String path, int level, int uid) throws SQLException
    {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        String res_path = null;
        LinkedList<String> res = new LinkedList<String>();
        String sql = "select path from search_table where path like '"+ path.replaceAll("'","''")+"%"+"' and user_id = "+uid +" and level > "+level;

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                res_path = rs.getString("path");
                res.add(res_path);
            }
        }catch (SQLException e)
        {
            throw e;
        }finally {
            rs.close();
            stmt.close();
            conn.close();
        }
        return res;
    }


    public static int get_id_by_title(String title,int uid) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        int res_id = -1;
        LinkedList<Integer> res = new LinkedList<Integer>();
        String sql = "select search_id from search_table where search_title = '"+ title.replaceAll("'","''")+"' and user_id = "+uid;

        ResultSet rs = null;
        try {
            rs  = stmt.executeQuery(sql);
            if (rs.next()) {
                // 通过字段检索
                res_id = rs.getInt("search_id");
            }
        }catch (SQLException e)
        {
            throw e;
        }finally {
            rs.close();
            stmt.close();
            conn.close();
        }
        return res_id;
    }
    public static int get_item_total(int u_id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        String sql = "select count(*) as total from search_table where user_id = "+ u_id;
        ResultSet rs = null;
        int res_count = 0;
        try {
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                res_count = rs.getInt("total");
            }
        }catch (SQLException e)
        {
            throw  e;
        }finally {
            rs.close();
            stmt.close();
            conn.close();
        }
        return res_count;
    }
    public static List<Item> get_items_by_page(int query_page, int page_size, int u_id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table where user_id = "+ u_id+ " limit "+ query_page*page_size+","+page_size;
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("search_id");
                String title = rs.getString("search_title");
                String content = rs.getString("search_content");
                String file = rs.getString("search_file");
                int user_id = rs.getInt("user_id");
                int cate_id = rs.getInt("category");
                int publics = rs.getInt("public");
                String path = rs.getString("path");
                int level = rs.getInt("level");
                // 输出数据
                res.add(new Item(id,title,content,file,user_id,cate_id,publics,path,level));
            }
        }catch (SQLException e)
        {
            throw e;
        }finally {
            rs.close();
            stmt.close();
            conn.close();
        }
        return res;
    }
    public static List<Item> query(String word, int uid) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        word = word.replaceAll("'","''");
        LinkedList<Item> res = new LinkedList<>();
        String sql = "select * from search_table where (user_id = "+uid+" or public = 1) and search_title like '%"+word+"%'";
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("search_id");
                String title = rs.getString("search_title");
                String content = rs.getString("search_content");
                String file = rs.getString("search_file");
                int user_id= rs.getInt("user_id");
                int cate_id = rs.getInt("category");
                int publics = rs.getInt("public");
                String path = rs.getString("path");
                int level = rs.getInt("level");
                // 输出数据
                res.add(new Item(id,title,content,file,user_id,cate_id,publics,path,level));
            }
        }catch (SQLException e)
        {
            throw  e;
        }finally {
            rs.close();
            stmt.close();
            conn.close();
        }
        return res;
    }

    public static boolean alter(Item item) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        int res = 0;
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        String sql = "UPDATE search_table SET search_title = '"+item.getTitle()+"', search_content = '"+item.getContent()+"', search_file = '"+item.getFile()+"', path = '"+item.getPosition()+"', level="+item.getLevel()+" WHERE search_id = "+item.getId();

        try{
            res = stmt.executeUpdate(sql);
        }catch (SQLException e)
        {
            throw  e;
        }finally {
            stmt.close();
            conn.close();
        }
        return (res == 1?true:false );
    }
    public static boolean delete_item_by_id(int id_out) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        int res = 0;
        String sql = "DELETE FROM  search_table WHERE  search_id = "+id_out;

        try {
            res = stmt.executeUpdate(sql);
        }catch (SQLException e)
        {
            throw  e;
        }finally {
            stmt.close();
            conn.close();
        }
        return (res == 1?true:false );
    }
    public  static boolean insert(Item item) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        item.setTitle(item.getTitle().replaceAll("'","''"));
        item.setContent(item.getContent().replaceAll("'","''"));
        item.setPosition(item.getPosition().replaceAll("'","''"));
        String sql = "INSERT INTO search_table(search_title,search_content,search_file,user_id,category,public,path,level)  VALUES('"+item.getTitle()+ "','"+item.getContent()+"','"+item.getFile()+"',"+item.getUid()+","+item.getCategory()+","+item.getPublics()+",'"+item.getPosition()+"',"+item.getLevel()+")";
        int res = 0;
        try{
            res = stmt.executeUpdate(sql);
        }catch (SQLException e)
        {
            throw  e;
        }finally {
            stmt.close();
            conn.close();
        }
        return (res == 1?true:false );
    }
    public  static Item query_by_id( int out_id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        Item res_item = null;
        String sql = "select * from search_table where search_id = "+ out_id;

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("search_id");
                String title = rs.getString("search_title");
                String content = rs.getString("search_content");
                String file = rs.getString("search_file");
                int user_id = rs.getInt("user_id");
                int cate_id = rs.getInt("category");
                int publics = rs.getInt("public");
                String path = rs.getString("path");
                int level = rs.getInt("level");
                // 输出数据
                res_item = new Item(id,title,content,file,user_id,cate_id,publics,path,level);
            }
        }catch (SQLException e)
        {
            throw e;
        }finally {
            rs.close();
            stmt.close();
            conn.close();
        }
        return res_item;
    }
}
