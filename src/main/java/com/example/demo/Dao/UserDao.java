package com.example.demo.Dao;

import com.example.demo.ConnectionFactory;
import com.example.demo.controller.HelloController;
import com.example.demo.object.Item;
import com.example.demo.object.Quick;
import com.example.demo.object.User;
import com.mysql.jdbc.CommunicationsException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class UserDao {
    public static void flush_connection()
    {

    }

    public static int get_quick_id_by_title(String title, int uid) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        int res_id = -1;
        LinkedList<Integer> res = new LinkedList<Integer>();
        String sql = "select id from quick where title = '"+ title.replaceAll("'","''")+"' and uid = "+uid;
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            // 通过字段检索
            res_id = rs.getInt("id");
        }
        rs.close();
        stmt.close();
        conn.close();
        return res_id;
    }

    public static boolean delete_quick_by_id(int  id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        int res = 0;
        String sql = "DELETE FROM  quick WHERE  id = "+id;
        res = stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
        return (res == 1?true:false );
    }

    public static boolean add_quick(Quick out_quick) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        out_quick.setTitle(out_quick.getTitle().replaceAll("'","''"));
        int quick_relevant_id = out_quick.getRelevant_item_id();
        int uid = out_quick.getUid();
        int res = 0;
        String sql = "INSERT INTO quick(title,item_id,uid)  VALUES('" +out_quick.getTitle() +"',"+quick_relevant_id+","+uid+")";
        res = stmt.executeUpdate(sql);
        stmt.close();
        conn.close();

        return (res == 1?true:false );
    }



    public static boolean update_user(User out_user) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        int res = 0;
        out_user.setMail(out_user.getMail().replaceAll("'","''"));
        out_user.setU_name(out_user.getU_name().replaceAll("'","''"));
        out_user.setU_pwd(out_user.getU_pwd().replaceAll("'","''"));
        String lur_string = UserDao.list_to_lru(out_user.getU_lru_list());
        String quick_string = UserDao.list_to_quick(out_user.getQuicks());

        String sql = "UPDATE user SET name = '"
                +out_user.getU_name() +"', pwd = '"
                +out_user.getU_pwd()+"', lru = '"
                +lur_string+"', mail = '"
                +out_user.getMail()+"', quick = '"
                +quick_string+"', language=" +out_user.getLanguage()+ " WHERE id = "+out_user.getU_id();

        res = stmt.executeUpdate(sql);
        stmt.close();
        conn.close();

        return (res == 1?true:false );
    }

    public static boolean add_user(User out_user) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        out_user.setMail(out_user.getMail().replaceAll("'","''"));
        out_user.setU_name(out_user.getU_name().replaceAll("'","''"));
        out_user.setU_pwd(out_user.getU_pwd().replaceAll("'","''"));
        String lur_string = UserDao.list_to_lru(out_user.getU_lru_list());
        String quick_string = UserDao.list_to_quick(out_user.getQuicks());


        int res = 0;
        String sql = "INSERT INTO user(name,pwd,lru,mail,quick,language)  VALUES('" +out_user.getU_name()
                + "','"+out_user.getU_pwd()
                +"','"+lur_string
                +"','"+out_user.getMail()
                +"','"+quick_string
                +"',"+out_user.getLanguage()+")";

        res = stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
        return (res == 1?true:false );

    }


    public static User get_user_by_mail_or_id(String mail_or_id) throws SQLException
    {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();
        User res_user = null;

        String sql = null;
        if(mail_or_id.contains("@"))
        {
            sql = "select * from user where mail = '"+mail_or_id.strip()+"'";
        }
        else
        {
            sql = "select * from user where id = "+mail_or_id.strip();
        }
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            int id  = rs.getInt("id");
            String name = rs.getString("name");
            String pwd = rs.getString("pwd");
            String mail = rs.getString("mail");
            int language = rs.getInt("language");

            String lru = rs.getString("lru");
            lru = lru == null? "":lru;
            String quick = rs.getString("quick");
            quick = quick == null? "":quick;
            res_user = new User(id,name,pwd,mail,lru_to_item_list(lru.strip()),quick_str_to_quick_list(quick.strip()),language);
        }
        rs.close();
        stmt.close();
        conn.close();
        return res_user;
    }

    public static String list_to_lru(List<Item> temp_list)
    {
        String res ="";
        for(Item i: temp_list)
        {
            res +=i.getId()+";";
        }
        return res;
    }

    public static List<Item> lru_to_item_list(String lru_str)
    {
        List<Item> res = new LinkedList<Item>();
        if(lru_str == null)
        {
            System.out.println("sql error, no lru in the database");
        }
        else
        {
            for(String item_id: lru_str.split(";"))
            {
                if(item_id.strip().equals(""))
                    continue;
                int item_id_int;
                try {
                    item_id_int = Integer.parseInt(item_id);
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                    continue;
                }
                Item temp = null;
                try {
                    temp = Dao.query_by_id(item_id_int);
                } catch (SQLException e) {
                    try {
                        temp = Dao.query_by_id(item_id_int);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                if(temp!= null)
                    res.add(temp);
                else
                    System.out.println("some lru id not in the database id is "+item_id);
            }
        }
        return  res;
    }

    public static String list_to_quick(List<Quick> temp_list)
    {
        String res ="";
        for(Quick i: temp_list)
        {
            res +=i.getId()+";";
        }
        return res;
    }

    public static List<Quick> quick_str_to_quick_list(String quick_str) throws SQLException
    {
        List<Quick> res = new LinkedList<Quick>();
        if(quick_str == null)
        {
            System.out.println("sql error, no quick_str in the database");
        }
        else
        {
            for(String item_id: quick_str.split(";"))
            {
                if(item_id.strip().equals(""))
                    continue;
                int item_id_int;
                try {
                    item_id_int = Integer.parseInt(item_id);
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                    continue;
                }
                Quick temp = null;
                temp = UserDao.get_quick_by_id(item_id_int);
                if(temp!= null)
                    res.add(temp);
                else
                    System.out.println("some quick id not in the database id is "+item_id);
            }
        }

        return  res;
    }


    public  static Quick get_quick_by_id(int id) throws SQLException {
        Connection conn = ConnectionFactory.get_connection();
        Statement  stmt = conn.createStatement();

        Quick res_item = null;
        String sql = "select * from quick where id = "+ id;
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            // 通过字段检索
            int res_id = rs.getInt("id");
            String title = rs.getString("title");
            int relevant_item = rs.getInt("item_id");
            int uid = rs.getInt("uid");
            // 输出数据
            res_item = new Quick(res_id,title,relevant_item,uid);
        }
        rs.close();
        stmt.close();
        conn.close();
        return res_item;
    }
}
