package com.example.demo.controller;

import com.example.demo.object.User;
import com.example.demo.service.Service;
import com.example.demo.service.Service_user;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Controller
public class UsersController {
    Service_user global_user_service = new Service_user();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/");


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

    @RequestMapping(value = "/user/resendmail")
    public String resend_mail(Model model, HttpServletRequest request) {
        return "resendmail";
    }

    @RequestMapping(value = "/user/resendmail_op")
    public String resend_mail_op(Model model, HttpServletRequest request) {
        String mail = request.getParameter("mail");
        if(mail == null)
        {
            global_user_service.set_error_message(model,"no mail parameter");
            return "message";
        }
        mail = mail.strip();
        User quer_user = global_user_service.get_user_by_mail_or_id(mail);
        if(quer_user == null)
        {
            global_user_service.set_error_message(model,"no mail submitted before");
            return "message";
        }
        if(quer_user.getCheck_code() == 0)
        {
            global_user_service.set_error_message(model," mail already registered before");
            return "message";
        }

        try{
            global_user_service.SendEmail(quer_user.getMail(),"http://203.189.0.62:8088/user/register_check?mail="+quer_user.getMail()+"&check_code="+quer_user.getCheck_code(),quer_user.getU_name());
        }catch (Exception e)
        {
            global_user_service.set_error_message(model,"mail send with error, contact somebody!!!");
            return "message";
        }
        return "mail";
    }

    @RequestMapping(value = "/user/register_check")
    public String register_check(Model model, HttpServletRequest request) {
        String check_code = request.getParameter("check_code");
        String mail = request.getParameter("mail");
        if(check_code == null || mail == null)
        {
            global_user_service.set_error_message(model,"no valid  check infomation");
            return "message";
        }
        int check_code_int = Integer.parseInt(check_code);

        User query_user = global_user_service.get_user_by_mail_or_id(mail);
        if(query_user == null || query_user.getCheck_code()!= check_code_int)
        {
            global_user_service.set_error_message(model,"no valid mail");
            return "message";
        }
        if(query_user.getCheck_code()== 0)
        {
            global_user_service.set_valid_message(model,"you already checked the mail, you can directly login");
            return "message";
        }
        query_user.setCheck_code(0);
        global_user_service.update_user(query_user);
        model.addAttribute("mail",query_user.getMail());
        model.addAttribute("pwd",query_user.getU_pwd());

        global_user_service.set_valid_message(model,"Process complete,  you can login now");
        return "message";
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
                User query_user = global_user_service.get_user_by_mail_or_id(mail);
                //if the mail already in our database
                if(query_user!= null)
                {
                    if(query_user.getCheck_code() == 0)
                        global_user_service.set_error_message(model,"mail already registered");
                    else
                        global_user_service.set_error_message(model,"mail already in register process");
                    return "message";
                }
                else
                {
                    int max = 999999; // specify the maximum value
                    Random rand = new Random();
                    int randomNumber = rand.nextInt(2,max); // generate a random integer between 0 (inclusive) and max (exclusive)

                    //default english 0
                    User add_user = new User(1,name,pwd,mail,null,null,0,randomNumber);
                    global_user_service.set_user_list_nonempty(add_user);
                    global_user_service.add_user(add_user);

                    try{
                        global_user_service.SendEmail(add_user.getMail(),"http://203.189.0.62:8088/user/register_check?mail="+add_user.getMail()+"&check_code="+randomNumber,add_user.getU_name());
                    }catch (Exception e)
                    {
                        global_user_service.set_error_message(model,"mail send with error, contact somebody!!!");
                        return "message";
                    }
                }
            }
        }
        return "mail";
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
            if(current_user == null)
            {
                se.removeAttribute("user");
                se.invalidate();
                global_user_service.set_valid_message(model,"mail is not in our system");
                return "message";
            }
            if(current_user.getCheck_code() != 0)
            {
                global_user_service.set_valid_message(model,"Please finish the check process");
                return "message";
            }
            // password mathc
            if(current_user.getU_pwd().equals(psw))
            {
                se.setAttribute("user",current_user.getU_id());
                model.addAttribute("user",current_user);
                model.addAttribute("result_list", current_user.getU_lru_list());
                model.addAttribute("init_select",1);
                return "index";
            }
            else
            {
                global_user_service.set_valid_message(model,"password is wrong for the required mail");
                return "message";
            }
        }
        return "forward:/index";
    }




    @RequestMapping(value = "/test")
    public String test(Model model, HttpServletRequest request,@RequestParam("files") List<MultipartFile> uploadFiles) {

        //store the files
        String format = sdf.format(new Date());
        File folder = new File(Service.uploadfile_dir + format);

        // store files and adding it the list
        // if parent dir not exists, then create it, if fails return -1
        if(!folder.isDirectory() && folder.mkdirs() == false)
        {
            return "message";
        }
        // store uploaded file and store in mysql
        boolean add_file_flag = false;
        for(MultipartFile temp_file: uploadFiles)
        {
            if(temp_file.getName().strip().equals("") || temp_file.getSize()==0 || temp_file.isEmpty())
                continue;
            String newName = UUID.randomUUID().toString() +"_"+ temp_file.getOriginalFilename();
            try {
                File new_file = new File(folder.getAbsolutePath()+"/"+newName);
                temp_file.transferTo(new_file);
                add_file_flag = true;
            } catch (IOException e) {
                // if store failed
                e.printStackTrace();
                return "message";
            }
        }
       return "message";
    }


    @RequestMapping(path = "/test_fetch", method = RequestMethod.GET)
    public ResponseEntity<Resource> test_fetch(Model model, HttpServletRequest request) throws IOException {
        String file_name = "2023/03/d6769e62-b96f-4c65-a0ec-3631f2427249_5a2112cc43dd22689a839b75196348bb.gif.bin";
        // get the file
        File file= new File(Service.uploadfile_dir+file_name.strip());
        if(!file.exists())
        {
            System.out.println("no file named "+file_name+" exist");
            return null;
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+ URLEncoder.encode(file.getName(), "UTF-8"));
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    @RequestMapping(value = "/test1")
    public String test_1(Model model, HttpServletRequest request) {
       return "test";
    }
    @RequestMapping(value = "/test2")
    public String test_2(Model model, HttpServletRequest request) {
        return "test1";
    }
}
