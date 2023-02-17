package com.example.demo.controller;
import com.example.demo.ConnectionFactory;
import com.example.demo.object.File_item;
import com.example.demo.object.Item;
import com.example.demo.object.Quick;
import com.example.demo.object.User;
import com.example.demo.service.Service;
import com.example.demo.service.Service_user;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class HelloController implements ApplicationContextAware {
    private ApplicationContext context = null;
    public static boolean need_close = false;
    Service gloabal_service = new Service();
    Service_user global_service_user = new Service_user();

    @RequestMapping(value = "/ajax_delete_op")
    public ResponseEntity<String> ajax_delete_op(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer) se.getAttribute("user");
        if(current_user_id == null)
        {
            return new ResponseEntity<>("no session!", HttpStatus.OK);
        }

        String delete_str = request.getParameter("delete_uuids");
        if(delete_str == null || delete_str.strip().equals(""))
        {
            return new ResponseEntity<>("no delete needed", HttpStatus.OK);
        }
        String [] del_id = delete_str.split(";");

        // update the users
        global_service_user.delete_quick_in_user(current_user_id,del_id);

        // delete the item in quick sql
        for(String need_del_id : del_id)
        {
            if(!need_del_id.strip().equals("")){
                global_service_user.delete_quick_by_id(Integer.parseInt(need_del_id.strip()));
            }
        }
        return  new ResponseEntity<>("okk", HttpStatus.OK);
    }
    @RequestMapping(value = "/index")
    public String index(Model model,HttpServletRequest request) {
        //get the user session
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer) se.getAttribute("user");

//        User current_user = global_service_user.get_user_by_mail_or_id("tangjians@icloud.com");
//        se.setAttribute("user",current_user.getU_id());
//        model.addAttribute("user",current_user);
//        return "index";


        //need to login first if no session
        if(current_user_id == null)
        {
            String name = request.getParameter("direct_mail");
            String pwd = request.getParameter("direct_pwd");
            if(name == null || pwd ==  null)
                return "users/login";
            else
            {
                User current_user = global_service_user.get_user_by_mail_or_id(name);
                if(current_user == null || !current_user.getU_pwd().equals(pwd))
                    return "users/login";
                else
                {
                    se.setAttribute("user",current_user.getU_id());
                    model.addAttribute("user",current_user);
                    return "index";
                }
            }
        }
        else
        {
            User current_user = global_service_user.get_user_by_mail_or_id(current_user_id+"");
            model.addAttribute("user",current_user);
            return "index";
        }

    }
    @RequestMapping(value = "/")
    public String index_default(Model model,HttpServletRequest request) {
        return "forward:/index";
    }

    @RequestMapping(value = "/searchByPage")
    public String queryBypage(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }
        String page_index = request.getParameter("page_index");
        int page_index_int = Integer.parseInt(page_index);
        User current_user = global_service_user.get_user_by_mail_or_id(current_user_id+"");

        current_user.setU_lru_list(gloabal_service.get_item_by_page(page_index_int, gloabal_service.page_size, current_user_id));
        int total_count  = gloabal_service.get_item_total(current_user_id);

        int current_page = page_index_int;
        int previous_page = page_index_int-1 <0? 0:page_index_int-1;
        int next_page = ( (total_count -1)/Service.page_size <= page_index_int)?(total_count -1)/Service.page_size :page_index_int+1;

        model.addAttribute("page", true);
        model.addAttribute("previous_page",previous_page);
        model.addAttribute("current_page",current_page);
        model.addAttribute("next_page",next_page);
        model.addAttribute("user",current_user);
        return "index";
    }

    @RequestMapping(value = "/search")
    public String search(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }
        String para = request.getParameter("search_words");
        para = para.strip();
        User current_user = global_service_user.get_user_by_mail_or_id(current_user_id+"");

        // input no empty
        if(!para.strip().equals("")) {
            String[] para_array = para.split(" ");
            current_user.setU_lru_list(gloabal_service.query_key_word(para_array, current_user_id));
        }
        model.addAttribute("user",current_user);
        model.addAttribute("page", false);
        return "index";
    }

    @RequestMapping(value = "/add")
    public String add(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }
        User current_user = global_service_user.get_user_by_mail_or_id(current_user_id+"");
        model.addAttribute("user",current_user );
        return "add";
    }
    @RequestMapping(value = "/update")
    public String update(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }

        String del_id = request.getParameter("id");
        // if the no id or id is not int
        if(del_id == null )
        {
            gloabal_service.set_message(model,"no id specified","index","返回主页","red");
            return "message";
        }
        // get item of del_id
        Item querry_item = null;
        try{
            querry_item = gloabal_service.querry_by_id(Integer.parseInt(del_id.strip()));
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
            gloabal_service.set_message(model,"id is not parseable","index","返回主页","red");
            return "message";
        }
        if(querry_item == null)
        {
            gloabal_service.set_message(model,"no such id in database","index","返回主页","red");
            return "message";
        }
        else
        {
            String title = querry_item.getTitle();
            String content = querry_item.getContent();
            String file = querry_item.getFile();
            String id = querry_item.getId()+"";
            String type = querry_item.getCategory()+"";

            model.addAttribute("title",title);
            model.addAttribute("content",content);
            model.addAttribute("id",id);
            model.addAttribute("type",type);

            // if it has a corresponding file
            String file_name = querry_item.getFile().strip();
            List<File_item> file_lists =  new LinkedList<File_item>();
            for(String item_string: file_name.split(";"))
            {
                if(!item_string.strip().equals(""))
                    file_lists.add(new File_item(item_string.strip(),item_string.strip().split("_")[1]));
            }
            model.addAttribute("file_list",file_lists );
            model.addAttribute("user", global_service_user.get_user_by_mail_or_id(current_user_id+""));
            return "update";
        }
    }
    @RequestMapping(value = "/add_op")
    public String add_op(Model model, HttpServletRequest request, @RequestParam("uploadFile") MultipartFile[] files) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }

        if(files == null)
        {
            System.out.println("null?");
            return "message";
        }
        String title = request.getParameter("search_title").strip();
        String content = request.getParameter("search_content").strip();
        String category_id = request.getParameter("type").strip();
        String public_item = request.getParameter("public");
        if(public_item == null)
            public_item = "0";
        // it no parameter
        if(title==null || content == null || category_id== null)
        {
            gloabal_service.set_message(model,"title or content  miss","index","返回主页","red");
            return "message";
        }
        // if the title is empty
        if(title.strip().equals(""))
        {
            gloabal_service.set_message(model,"输入的标题不能为空","index","返回主页","red");
            return "message";
        }
        // if it contains the same title
        int query_id =  gloabal_service.get_id_by_title(title, current_user_id);
        if(query_id > 0)
        {
            gloabal_service.set_message(model,"标题已经存在","index","返回主页","red");
            return "message";
        }
        //store multiple files
        int store_res = gloabal_service.store(files,title,content,current_user_id,Integer.parseInt(category_id),public_item);
        String message = null;
        switch (store_res){
            case -3:
                message = "store file fail";
                break;
            case -2:
                message = "sql execute fail";
                break;
            case -1:
                message = "dir creating fail";
                break;
            case 0:
                message = "添加成功(不带附件)";
                break;
            default:
                message = "添加成果(含有附件)";
                break;
        }
        // store successful
        if(store_res >=0)
        {
            int id = gloabal_service.get_id_by_title(title,current_user_id);
            model.addAttribute("id",id);
            gloabal_service.set_message(model,message,"detail","查看","darkgoldenrod");
            // add lru to user
            global_service_user.lru_to_front(id,current_user_id);
        }
        else
            gloabal_service.set_message(model,message,"index","返回主页","red");
        return "message";
    }
    @RequestMapping(value = "/delete_op")
    public String del_op(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }

        String del_id = request.getParameter("id");
        if(del_id==null)
        {
            gloabal_service.set_message(model,"no id element","index","返回主页","red");
            return "message";
        }
        Item querry_item = null;
        try{
            querry_item =gloabal_service.querry_by_id(Integer.parseInt(del_id.strip()));
        }catch (NumberFormatException e)
        {
            e.printStackTrace();
            gloabal_service.set_message(model,"id is not a integer","index","返回主页","red");
            return "message";
        }

        // delete the  corresponding file
        if(!gloabal_service.delete_files_from_string(querry_item.getFile().strip()))
            System.out.println("warning: some file is not in the dir");

        //update user first
        global_service_user.lru_delte(Integer.parseInt(del_id),current_user_id);
        //delete the item
        if(gloabal_service.delete_item_by_id(Integer.parseInt(del_id.strip())))
        {
            gloabal_service.set_message(model,"delete "+del_id+"success...","index","返回主页","darkgoldenrod");
        }
        else
            gloabal_service.set_message(model,"delete "+del_id+"failed...","index","返回主页","red");
        return "message";
    }
    @RequestMapping(value = "/update_op")
    public String update_op(Model model, HttpServletRequest request,@RequestParam("uploadFiles") MultipartFile[] files) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }

        String title = request.getParameter("search_title").strip();
        String content = request.getParameter("search_content").strip();
        String id = request.getParameter("id");
        String cate_id = request.getParameter("type");
        // if no enough parameters
        if(title == null || content == null || id ==null)
        {
            gloabal_service.set_message(model,"parameter is null","index","返回主页","red");
            return "message";
        }
        // if title is empty or the id
        if(title.strip().equals(""))
        {
            gloabal_service.set_message(model,"标题不能为空","index","返回主页","red");
            return "message";
        }
        // if id is not valid
        int id_int;
        try {
            id_int = Integer.parseInt(id.strip());
        }catch (NumberFormatException e)
        {
            e.printStackTrace();
            gloabal_service.set_message(model,"id is not parseable","index","返回主页","red");
            return "message";
        }
        Item query_item = gloabal_service.querry_by_id(id_int);
        // if id is not in the database
        if(query_item== null)
        {
            gloabal_service.set_message(model,"id is not in the database","index","返回主页","red");
            return "message";
        }
        // check the updated title is already in the database
        int query_title_id =  gloabal_service.get_id_by_title(title,current_user_id);
        if(query_title_id > 0 && query_title_id != id_int )
        {
            gloabal_service.set_message(model,"the title is already in the database","index","返回主页","red");
        }

        String message;
        int update_res = gloabal_service.update(files,title,content,Integer.parseInt(id.strip()),request.getParameterValues("del_files"),current_user_id, Integer.parseInt(cate_id));
        switch (update_res){
            case -3:
                message = "store file fail";
                break;
            case -2:
                message = "sql execute fail";
                break;
            case -1:
                message = "dir creating fail";
                break;
            case 0:
                message = "修改成功(文件列表不变)";
                break;
            case 1:
                message = "修改成功(增加文件)";
                break;
            case 2:
                message = "修改成功(删除文件)";
                break;
            default:
                message = "修改成功(增加，删除文件)";
                break;
        }
        // update successful
        if(update_res >=0)
        {
            model.addAttribute("id",id);
            gloabal_service.set_message(model,message,"detail","查看","darkgoldenrod");
            // add lru to user
            global_service_user.lru_to_front(id_int,current_user_id);
        }
        else{
            gloabal_service.set_message(model,message,"index","返回主页","red");
        }
        return "message";
    }

    @RequestMapping(value = "/detail")
    public String detail(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return  "users/login";
        }

        String para = request.getParameter("detail");
        if(para == null || para.equals("")){
            gloabal_service.set_message(model,"no id is available", "index","回到主页","red");
            return "message";
        }
        int id_int;
        try{
            id_int  = Integer.parseInt(para);
        }
        catch (NumberFormatException e )
        {
            e.printStackTrace();
            gloabal_service.set_message(model,"id is un parseable", "index","回到主页","red");
            return "message";
        }
        Item query_item = gloabal_service.querry_by_id(id_int);
        if(query_item == null)
        {
            gloabal_service.set_message(model,"id is not in database", "index","回到主页","red");
            return "message";
        }

        model.addAttribute("id",query_item.getId()+"");
        model.addAttribute("title",query_item.getTitle()+"");
        model.addAttribute("content",query_item.getContent()+"");
        String file_name = query_item.getFile().strip();
        List<File_item> file_lists =  new LinkedList<File_item>();
        for(String item_string: file_name.split(";"))
        {
            if(!item_string.strip().equals(""))
            {
                String []file_split_names = item_string.split("_",2);
                if(file_split_names.length == 2)
                    file_lists.add(new File_item(item_string.strip(),file_split_names[1]));
                else
                    System.out.println("invalid file name");
            }
        }
        model.addAttribute("file_list",file_lists );
        model.addAttribute("publics",query_item.getPublics());
        model.addAttribute("user",global_service_user.get_user_by_mail_or_id(current_user_id+""));
        // update the user
        global_service_user.lru_to_front(id_int,current_user_id);
        // photo or normal
        int type = query_item.getCategory();
        if(type == 0)
            return "item";
        else
        {
            for(File_item temp :file_lists)
            {
                if( !temp.getFile().endsWith(".jpg") && !temp.getFile().endsWith(".png"))
                {
                    gloabal_service.set_message(model,"contains no photo in the dataset", "index","回到主页","red");
                    return "message";
                }
            }
            return "photo";
        }
    }

    @RequestMapping(path = "/downloadAll", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadAll(HttpServletRequest request) throws IOException {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            new ResponseEntity<>("you need to login first", HttpStatus.OK);
        }

        String id = request.getParameter("id").strip();
        // if id is null or empty
        if(id== null)
        {
            System.out.println("id is null");
            return null;
        }
        int id_int ;
        try{
            id_int = Integer.parseInt(id.strip());
        }catch (NumberFormatException e)
        {
            e.printStackTrace();
            return null;
        }
        // no id in database
        Item res = gloabal_service.querry_by_id(id_int);
        if(res == null)
        {
            System.out.println("no id in database");
            return  null;
        }
        String files_string = res.getFile();
        if(files_string.equals(""))
        {
            System.out.println("current item has no file");
            return null;
        }
        // get files list
        String[] files_string_list = files_string.split(";");
        List<String> file_list = new LinkedList<String>();
        for(String i : files_string_list)
        {
            if(!i.strip().equals(""))
                file_list.add(i.strip());
        }

        File file = null;
        // get the zip file
        try {
            file= gloabal_service.zip_multiple_fiels(file_list);
        }catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("no file exist for");
            return null;
        }
        if(!file.exists())
        {
            System.out.println("no zipped file exist");
            return null;
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+file.getName());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @RequestMapping(path = "/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(Model model, HttpServletRequest request) throws IOException {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            new ResponseEntity<>("you need to login first", HttpStatus.OK);
        }

        String file_name = request.getParameter("file").strip();
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

    @RequestMapping(value = "/quick_add")
    public String quick_add(Model model, HttpServletRequest request)
    {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");
        if(current_user_id == null)
        {
            return "users/login";
        }

        String add_type = request.getParameter("add_type");
        if(add_type ==  null || add_type.strip().equals(""))
        {
            gloabal_service.set_message(model,"no add_type","index","回到主页","red");
            return "message";
        }
        // bonding
        if(add_type.strip().equals("0"))
        {
            // id exist?
            String id = request.getParameter("id");
            if(id== null || id.strip().equals(""))
            {
                gloabal_service.set_message(model,"no valid id","index","回到主页","red");
                return "message";
            }
            // id valid?
            int id_int;
            try{
                id_int = Integer.parseInt(id.strip());
            }catch (NumberFormatException e)
            {
                e.printStackTrace();
                gloabal_service.set_message(model,"id is invalid","index","回到主页","red");
                return "message";
            }
            // id is in database?
            Item query_item=  gloabal_service.querry_by_id(id_int);
            if(query_item == null)
            {
                gloabal_service.set_message(model,"id is not in database","index","回到主页","red");
                return "message";
            }
            // add to model
            model.addAttribute("title",query_item.getTitle());
            model.addAttribute("id",query_item.getId()+"");
            model.addAttribute("user",global_service_user.get_user_by_mail_or_id(current_user_id+""));
            return "quickadd";
        }
        // no bonding add
        else
        {
            model.addAttribute("title","");
            model.addAttribute("id","-1");
            model.addAttribute("user",global_service_user.get_user_by_mail_or_id(current_user_id+""));
            return "quickadd";
        }
    }
    @RequestMapping(value = "/quick_add_op")
    public String quick_add_op(Model model, HttpServletRequest request)
    {
        //get the the user session
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer)se.getAttribute("user");

        //need to login first if no session
        if(current_user_id == null)
        {
            return "users/login";
        }

        String bonding_id = request.getParameter("id");
        String add_title = request.getParameter("title");
        if(add_title ==  null || add_title.strip().equals(""))
        {
            gloabal_service.set_message(model,"title is empty","index","回到主页","red");
            return "message";
        }
        Quick new_quick  = null;
        // bonding
        if(bonding_id !=null && !bonding_id.strip().equals(""))
        {
            new_quick = new Quick(0,add_title, Integer.parseInt(bonding_id.strip()),current_user_id);
        }
        else
            new_quick = new Quick(0,add_title, -1,current_user_id);
        //add the quick to sql first
        global_service_user.add_quick(new_quick);
        // update the user
        global_service_user.add_quick_in_user(current_user_id,new_quick);
        return "forward:/index";
    }

    @RequestMapping(value = "/close")
    public String close(Model model, HttpServletRequest request) {
        ConnectionFactory.CleanConection();
        model.addAttribute("message","clean....");
        return "message";
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
