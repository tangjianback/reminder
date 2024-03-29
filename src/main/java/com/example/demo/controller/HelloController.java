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

    @RequestMapping(value = "/ajax_folder_by_path")
    public ResponseEntity<String> ajax_folder_by_path(Model model, HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer) se.getAttribute("user");
        if(current_user_id == null)
        {
            return new ResponseEntity<>("no session!", HttpStatus.OK);
        }
        String path = request.getParameter("path");
        StringBuffer status = new StringBuffer("SUCC");
        String folders = gloabal_service.get_folder_by_path(path,current_user_id,status);
        if(!status.toString().equals("SUCC"))
        {
            return  new ResponseEntity<>(status.toString(), HttpStatus.EXPECTATION_FAILED);
        }
        return  new ResponseEntity<>(folders, HttpStatus.OK);
    }

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
    @RequestMapping(value = "/love")
    public String love(Model model,HttpServletRequest request) {
        //get the user session
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer) se.getAttribute("user");

        //need to login first if no session
        if(current_user_id == null)
        {
                return "users/login";
        }
        else
        {
            return "love";
        }
    }

    @RequestMapping(value = "/index")
    public String index(Model model,HttpServletRequest request) {
        HttpSession se = request.getSession();
        Integer current_user_id = (Integer) se.getAttribute("user");

        //need to login first if no session
        if(current_user_id == null)
        {
                return "users/login";
        }
        else
        {
            User current_user = global_service_user.get_user_by_mail_or_id(current_user_id+"");
            if(current_user == null)
            {
                System.out.println("the cookied user id is invalid");
                gloabal_service.set_message(model,"服务器出现了一个不太重要的问题","index","返回主页","red");
                se.removeAttribute("user");
                return "message";
            }
            model.addAttribute("user",current_user);
            String select = request.getParameter("select");
            if(select == null)
                select = "1";
            int index_type = Integer.parseInt(select);

            String page_index = request.getParameter("page_index");
            if(page_index == null)
                page_index = "0";
            int page_index_int = Integer.parseInt(page_index);
            int total_count  = 0;
            int previous_page = 0;
            int next_page = 0;
            StringBuffer status = new StringBuffer("SUCC");

            switch (index_type) {
                case 1: // lru
                    model.addAttribute("result_list", current_user.getU_lru_list());
                    model.addAttribute("init_select",1);
                    model.addAttribute("page", false);
                    break;
                case 2: // cate
                    String [] s= request.getParameterValues("selects");
                    String out_path = "/";
                    int click_index = Integer.parseInt(request.getParameter("click_index"));
                    if (s!= null)
                        for(String i: s) {
                            if (!i.strip().equals("") && !i.strip().equals("CURRENT") && click_index>=0)
                            {
                                out_path += i + "/";
                                click_index--;
                            }
                            else
                                break;
                        }

                    List<Item> res_list = gloabal_service.get_item_by_path(out_path,current_user_id,status);
                    if( !status.toString().equals("SUCC") )
                    {
                        gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                        return "message";
                    }

                    total_count  = res_list.size();
                    previous_page = Math.max(page_index_int - 1, 0);
                    next_page = ( (total_count -1)/Service.page_size <= page_index_int)?(total_count -1)/Service.page_size :page_index_int+1;

                    model.addAttribute("page", true);
                    model.addAttribute("previous_page",previous_page);
                    model.addAttribute("current_page",page_index_int);
                    model.addAttribute("next_page",next_page);
                    model.addAttribute("result_list", res_list.subList(page_index_int*gloabal_service.page_size,Math.min((page_index_int+1)*gloabal_service.page_size,res_list.size())));

                    status.replace(0,status.length(),"SUCC");
                    List<List<String>> temp_list_list = gloabal_service.get_complete_folders_by_path(out_path,current_user_id,status);
                    if(!status.toString().equals("SUCC") )
                    {
                        gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                        return "message";
                    }
                    model.addAttribute("init_folders",temp_list_list);

                    model.addAttribute("selects", s==null?new LinkedList<String>():Arrays.stream(s).toList());
                    model.addAttribute("click_index", request.getParameter("click_index"));
                    model.addAttribute("init_select",2);
                    break;
                case 3: // all
                    status.replace(0,status.length(),"SUCC");
                    total_count  = gloabal_service.get_item_total(current_user_id,status);
                    if(!status.toString().equals("SUCC"))
                    {
                        gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                        return "message";
                    }
                    previous_page = Math.max(page_index_int - 1, 0);
                    next_page = ( (total_count -1)/Service.page_size <= page_index_int)?(total_count -1)/Service.page_size :page_index_int+1;

                    model.addAttribute("page", true);
                    model.addAttribute("previous_page",previous_page);
                    model.addAttribute("current_page",page_index_int);
                    model.addAttribute("next_page",next_page);

                    status.replace(0,status.length(),"SUCC");
                    List<Item> temp_item_list = gloabal_service.get_item_by_page(page_index_int, gloabal_service.page_size, current_user_id,status);
                    if(!status.toString().equals("SUCC"))
                    {
                        gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                        return "message";
                    }
                    model.addAttribute("result_list", temp_item_list);
                    model.addAttribute("init_select",3);
                    break;
                default: // search
                    String para = request.getParameter("search_words");
                    if(para == null)
                        para = "";
                    para = para.strip();
                    String[] para_array = para.split(" ");

                    status.replace(0,status.length(),"SUCC");
                    List<Item> current_list = gloabal_service.query_key_word(para_array, current_user_id,status);
                    if(!status.toString().equals("SUCC"))
                    {
                        gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                        return "message";
                    }
                    total_count  = current_list.size();
                    previous_page = Math.max(page_index_int - 1, 0);
                    next_page = ( (total_count -1)/Service.page_size <= page_index_int)?(total_count -1)/Service.page_size :page_index_int+1;

                    model.addAttribute("page", true);
                    model.addAttribute("previous_page",previous_page);
                    model.addAttribute("current_page",page_index_int);
                    model.addAttribute("next_page",next_page);
                    model.addAttribute("result_list", current_list.subList(page_index_int*gloabal_service.page_size,Math.min((page_index_int+1)*gloabal_service.page_size,current_list.size())));
                    model.addAttribute("key_words",para.strip());
                    model.addAttribute("init_select",4);
            };
            return "index";
        }

    }

    @RequestMapping(value = "/")
    public String index_default(Model model,HttpServletRequest request) {
        return "forward:/index";
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
        List<String>  ini_dirs = new LinkedList<String>();

        StringBuffer status = new StringBuffer("SUCC");
        String temp_string  = gloabal_service.get_folder_by_path("/",current_user_id,status);
        if(!status.toString().equals("SUCC") )
        {
            gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
            return "message";
        }
        for(String temp_s : temp_string.split(";"))
        {
            if(temp_s.strip()!="")
                ini_dirs.add(temp_s);
        }
        model.addAttribute("ini_folders",ini_dirs);
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
            StringBuffer status = new StringBuffer("SUCC");
            querry_item = gloabal_service.querry_by_id(Integer.parseInt(del_id.strip()),status);
            if(!status.toString().equals("SUCC"))
            {
                gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                return "message";
            }
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
            String path = querry_item.getPosition();

            model.addAttribute("title",title);
            model.addAttribute("content",content);
            model.addAttribute("id",id);
            model.addAttribute("type",type);
            model.addAttribute("ini_path",path);


            StringBuffer status = new StringBuffer("SUCC");
            List<List<String>> temp_list_list = gloabal_service.get_complete_folders_by_path(path,current_user_id,status);
            if(!status.toString().equals("SUCC"))
            {
                gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                return "message";
            }
            model.addAttribute("init_folders",temp_list_list);

            // if it has a corresponding file
            String file_name = querry_item.getFile().strip();
            List<File_item> file_lists =  new LinkedList<File_item>();
            for(String item_string: file_name.split(";"))
            {
                if(!item_string.strip().equals(""))
                {
                    String []file_split_names = item_string.split("_",2);
                    file_lists.add(new File_item(item_string.strip(),file_split_names[1]));
                }
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
        String path = request.getParameter("path");

        if(public_item == null)
            public_item = "0";

        //if path invalid
        if(path.indexOf("//") !=-1|| !path.startsWith("/"))
        {
            gloabal_service.set_message(model,"invalid path: "+path,"index","返回主页","red");
            return "message";
        }
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

        StringBuffer status = new StringBuffer("SUCC");
        // if it contains the same title
        int query_id =  gloabal_service.get_id_by_title(title, current_user_id,status);
        if(!status.toString().equals("SUCC"))
        {
            gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
            return "message";
        }

        if(query_id > 0)
        {
            gloabal_service.set_message(model,"标题已经存在","index","返回主页","red");
            return "message";
        }

        //store multiple files
        int store_res = gloabal_service.store(files,title,content,current_user_id,Integer.parseInt(category_id),public_item,path.strip());
        String message = null;
        switch (store_res){
            case -4:
                message = "contains invalid file for photo item";
                break;
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
            status.replace(0,status.length(),"SUCC");
            int id = gloabal_service.get_id_by_title(title,current_user_id,status);
            if(!status.toString().equals("SUCC"))
            {
                gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                return "message";
            }

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
            StringBuffer status = new StringBuffer("SUCC");
            querry_item =gloabal_service.querry_by_id(Integer.parseInt(del_id.strip()),status);
            if(!status.toString().equals("SUCC"))
            {
                gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
                return "message";
            }
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
        StringBuffer status = new StringBuffer("SUCC");
        gloabal_service.delete_item_by_id(Integer.parseInt(del_id.strip()),status);
        if(!status.toString().equals("SUCC"))
        {
            gloabal_service.set_message(model,"delete "+del_id+"failed...","index","返回主页","red");
        }
        else
        {
            gloabal_service.set_message(model,"delete "+del_id+"success...","index","返回主页","darkgoldenrod");
        }
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
        String path = request.getParameter("path").strip();

        //if path invalid
        if(path.indexOf("//")!=-1 || !path.startsWith("/"))
        {
            gloabal_service.set_message(model,"invalid path: "+path,"index","返回主页","red");
            return "message";
        }

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
        StringBuffer status = new StringBuffer("SUCC");
        Item query_item = gloabal_service.querry_by_id(id_int,status);
        if(!status.toString().equals("SUCC"))
        {
            gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
            return "message";
        }

        String old_path = query_item.getPosition();
        // if id is not in the database
        if(query_item== null)
        {
            gloabal_service.set_message(model,"id is not in the database","index","返回主页","red");
            return "message";
        }
        // check the updated title is already in the database

        status.replace(0,status.length(),"SUCC");
        int query_title_id =  gloabal_service.get_id_by_title(title,current_user_id,status);
        if(!status.toString().equals("SUCC"))
        {
            gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
            return "message";
        }

        if(query_title_id > 0 && query_title_id != id_int )
        {
            gloabal_service.set_message(model,"the title is already in the database","index","返回主页","red");
        }

        String message;
        int update_res = gloabal_service.update(files,title,content,Integer.parseInt(id.strip()),request.getParameterValues("del_files"),current_user_id, Integer.parseInt(cate_id),path);
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
                message = "修改成功(文件列表不变,";
                break;
            case 1:
                message = "修改成功(增加文件,";
                break;
            case 2:
                message = "修改成功(删除文件,";
                break;
            default:
                message = "修改成功(增加&删除文件,";
                break;
        }
        // update successful
        if(update_res >=0)
        {
            if(old_path.equals(path))
                message+="笔记路径不变)";
            else
                message+="笔记路径改变)";
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
        StringBuffer status = new StringBuffer("SUCC");
        Item query_item = gloabal_service.querry_by_id(id_int,status);
        if(!status.toString().equals("SUCC"))
        {
            gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
            return "message";
        }

        model.addAttribute("id",query_item.getId()+"");
        model.addAttribute("title",query_item.getTitle()+"");
        model.addAttribute("content",query_item.getContent()+"");
        model.addAttribute("path",query_item.getPosition()+"");
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
        // update lru order of the user items/quick
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

        StringBuffer status = new StringBuffer("SUCC");
        // no id in database
        Item res = gloabal_service.querry_by_id(id_int,status);
        if(!status.toString().equals("SUCC"))
        {
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
            StringBuffer status = new StringBuffer("SUCC");
            // id is in database?
            Item query_item=  gloabal_service.querry_by_id(id_int,status);
            if(!status.toString().equals("SUCC"))
            {
                gloabal_service.set_message(model,status.toString(),"index","返回主页","red");
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
