package com.example.demo.service;

import com.example.demo.Dao.Dao;
import com.example.demo.Dao.UserDao;
import com.example.demo.object.Item;
import com.example.demo.object.Quick;
import com.example.demo.object.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class Service_user {

    //add the quick in the user
    public void add_quick_in_user(int uid, Quick out_quick)
    {
        User current_user = this.get_user_by_mail_or_id(uid+"");
        this.set_user_list_nonempty(current_user);
        List<Quick> quick_itemList = current_user.getQuicks();
        int add_quick_id = 0;
        try {
            add_quick_id = UserDao.get_quick_id_by_title(out_quick.getTitle().strip(), uid);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                add_quick_id = UserDao.get_quick_id_by_title(out_quick.getTitle().strip(), uid);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        out_quick.setId(add_quick_id);
        quick_itemList.add(out_quick);
        this.update_user(current_user);
    }
    public void delete_quick_in_user(int uid, String quick_id[])
    {
        User old_user = this.get_user_by_mail_or_id(uid+"");
        this.set_user_list_nonempty(old_user);
        List<Quick> quick_itemList =old_user.getQuicks();
        for(String need_del_id : quick_id)
        {
            if(need_del_id.strip().equals(""))
                continue;
            for(Quick i: quick_itemList)
            {
                if(i.getId()  == Integer.parseInt(need_del_id.strip()))
                {
                    //delete the record in quick sql
                    this.delete_quick_by_id(i.getId());
                    //delete the record in user
                    quick_itemList.remove(i);
                    break;
                }
            }
        }
        this.update_user(old_user);
    }

    // delete quick in the sql
    public boolean delete_quick_by_id(int id)
    {
        try {
            return UserDao.delete_quick_by_id(id);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                return UserDao.delete_quick_by_id(id);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // add quick in the sql
    public boolean add_quick(Quick out_quick)
    {
        try {
            return UserDao.add_quick(out_quick);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                return UserDao.add_quick(out_quick);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public void lru_to_front(int item_id,int uid)
    {
        User old_uesr = null;
        try {
            old_uesr = UserDao.get_user_by_mail_or_id(uid+"");
        }catch (SQLException e)
        {
            e.printStackTrace();
            try {
                old_uesr = UserDao.get_user_by_mail_or_id(uid+"");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        this.set_user_list_nonempty(old_uesr);

        List<Item> old_list = old_uesr.getU_lru_list();

        // if it contains already then delete it
        for(Item i : old_list)
        {
            if(i.getId() == item_id)
            {
                old_list.remove(i);
                break;
            }
        }
        // add in the head
        Item new_head = null;
        try {
            new_head = Dao.query_by_id(item_id);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                new_head = Dao.query_by_id(item_id);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(new_head == null)
        {
            System.out.println("no new head");
            return;
        }
        old_list.add(0,new_head);
        // if it exceeds the limit drop the tail
        if(old_list.size()>10)
            old_list.remove(old_list.size()-1);

        // write into the mysql
        try {
            UserDao.update_user(old_uesr);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                UserDao.update_user(old_uesr);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public void lru_delte(int item_id, int uid)
    {

        User old_uesr = null;
        try {
            old_uesr = UserDao.get_user_by_mail_or_id(uid+"");
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                old_uesr = UserDao.get_user_by_mail_or_id(uid+"");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        this.set_user_list_nonempty(old_uesr);
        List<Item> old_list = old_uesr.getU_lru_list();
        old_list.remove(new Item(item_id,"","","",1,0,0));
        // write into the mysql
        try {
            UserDao.update_user(old_uesr);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                UserDao.update_user(old_uesr);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    public boolean add_user(User user_out)
    {
        try {
            return UserDao.add_user( user_out);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                return UserDao.add_user( user_out);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public  boolean update_user(User out_user)
    {
        try {
            return  UserDao.update_user(out_user);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                return UserDao.update_user(out_user);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public User get_user_by_mail_or_id(String mail_or_id)
    {
        User temp = null;
        try {
            temp = UserDao.get_user_by_mail_or_id(mail_or_id);
        } catch (SQLException e) {
            UserDao.flush_connection();
            try {
                temp = UserDao.get_user_by_mail_or_id(mail_or_id);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        this.set_user_list_nonempty(temp);
        return temp;
    }
    public void set_error_message(Model model, String message)
    {
        model.addAttribute("message",message);
        model.addAttribute("color","red");
        model.addAttribute("url","index");
        model.addAttribute("button_name","返回主页");
        return;
    }
    public void set_valid_message(Model model, String message)
    {
        model.addAttribute("message",message);
        model.addAttribute("color","green");
        model.addAttribute("url","index");
        model.addAttribute("button_name","返回主页");
        return;
    }
    public void set_user_list_nonempty(User current_user)
    {
        if(current_user == null)
            return;
        if(current_user.getU_lru_list() == null)
            current_user.setU_lru_list(new LinkedList<Item>());
        if(current_user.getQuicks() == null)
            current_user.setQuicks(new LinkedList<Quick>());
    }
}
