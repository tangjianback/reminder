package com.example.demo.controller;

import com.example.demo.object.User;
import com.example.demo.service.Service;
import com.example.demo.service.Service_user;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UsersController {
    Service_user global_user_service = new Service_user();


    @RequestMapping(value = "/user/language")
    public String changelanguage(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user" );
        //if the null
        if(current_user_id == null)
        {
            return "users/login";
        }
        else
        {
            User current_user = global_user_service.get_user_by_mail_or_id(current_user_id+"");
            current_user.setLanguage(current_user.getLanguage()==0?1:0);
            global_user_service.update_user(current_user);
            return "forward:/index";
        }

    }


    @RequestMapping(value = "/user/test")
    public ResponseEntity<String> ajax_delete_op(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        if(se.getAttribute("test")!= null && !se.getAttribute("test").toString().strip().equals(""))
            System.out.println(se.getAttribute("test"));
        else
            se.setAttribute("test","剑哥你好");
        return  new ResponseEntity<>("okk", HttpStatus.OK);
    }

    @RequestMapping(value = "/user/login_out")
    public String login_out(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        if(se != null)
            se.invalidate();
        return "users/login";
    }

    @RequestMapping(value = "/user/register")
    public String register(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user" );
         //if the null
        if(current_user_id == null)
        {
            return "users/register";
        }
        //if is not empty
        else
        {
            return "forward:/index";
        }
    }
    @RequestMapping(value = "/user/register_op")
    public String register_op(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user" );
        //if the null
        if(current_user_id == null)
        {
            String name  = request.getParameter("name").strip();
            String mail  = request.getParameter("mail").strip();
            String pwd = request.getParameter("psd").strip();

            // if invalid register infomation
            if(name == null || mail  == null || pwd == null)
            {
                global_user_service.set_error_message(model,"no valid register information");
                return "message";
            }
            else
            {
                //if the mail already in our database
                if(global_user_service.get_user_by_mail_or_id(mail)!= null)
                {
                    global_user_service.set_error_message(model,"mail already registered");
                    return "message";
                }
                else
                {
                    //default english 0
                    User add_user = new User(1,name,pwd,mail,null,null,0);
                    global_user_service.set_user_list_nonempty(add_user);
                    global_user_service.add_user(add_user);
                    model.addAttribute("mail",add_user.getMail());
                    model.addAttribute("pwd",add_user.getU_pwd());
                }
            }
        }
        return "users/login";
    }
    @RequestMapping(value = "/user/login")
    public String login(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer) se.getAttribute("user" );
        //if the null
        if(current_user_id == null)
        {
            return "users/login";
        }
        //if is not empty
        else
        {
            return "forward:/index";
        }
    }

    @RequestMapping(value = "/user/login_op")
    public String login_op(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer) se.getAttribute("user" );
        //if the nullI
        if(current_user_id == null)
        {
            String name = request.getParameter("mail");
            String psw = request.getParameter("psd");
            if(name == null || psw == null)
            {
                return "users/login";
            }
            User current_user = global_user_service.get_user_by_mail_or_id(name.strip());

            // password mathc
            if(current_user.getU_pwd().equals(psw))
            {
                se.setAttribute("user",current_user.getU_id());
                model.addAttribute("user",current_user);
                return "index";
            }
            else
            {
                return "users/login";
            }
        }
        return "forward:/index";
    }
}
