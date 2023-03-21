package com.example.demo.service;

import com.example.demo.Dao.Dao;
import com.example.demo.Dao.UserDao;
import com.example.demo.object.Item;
import com.example.demo.object.Quick;
import com.example.demo.object.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
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
            try {
                UserDao.flush_connection();
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
        old_list.remove(new Item(item_id,"","","",1,0,0,"",0));
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
    public void SendEmail(String harvestEmail,String check_url,String user_name) throws Exception {
        final String MyEmail = "jianges2023@163.com";//开启授权码的邮箱
        final String AuthorizationCode = "POHPJFISDTHAKOUX";//授权码
        final String SMTPEmail = "smtp.163.com";// 网易163邮箱的 SMTP 服务器地址

        //创建连接邮件服务器的参数配置
        Properties props = new Properties();// 参数配置
        props.setProperty("mail.smtp.host", SMTPEmail);// 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");// 需要请求认证
        props.setProperty("mail.transport.protocol", "smtp");
        //根据配置创建会话对象和邮件服务器交互
        Session session = Session.getInstance(props);
        session.setDebug(false);// 设置为debug模式, 可以查看详细的发送日志
        //创建邮件
        MimeMessage message = createEmail(session, MyEmail, harvestEmail,user_name,check_url);
        //使用Session获取邮件传输对象
        Transport transport = session.getTransport();
        //使用邮箱账号和密码连接邮件服务器
        transport.connect(MyEmail, AuthorizationCode);
        //发送邮件
        transport.sendMessage(message, message.getAllRecipients());
        //关闭连接
        transport.close();
    }

    public  MimeMessage createEmail(Session session, String sendMail, String receiveMail,String user_name,String check_link) throws Exception {
        //创建一封邮件
        MimeMessage message = new MimeMessage(session);
        //发件人
        message.setFrom(new InternetAddress(sendMail, "Memory Register", "UTF-8"));
        //收件人
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, user_name+"用户", "UTF-8"));
        //邮件主题
        message.setSubject("Memory Register", "UTF-8");
        //邮件正文
        message.setContent("Thanks for using... \n" +
                "Click this link to complete the register process: "+ check_link, "text/html;charset=UTF-8");
        //设置发件时间
        message.setSentDate(new Date());
        //保存设置
        message.saveChanges();
        return message;
    }



}
