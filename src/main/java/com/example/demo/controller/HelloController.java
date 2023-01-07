package com.example.demo.controller;
import com.example.demo.Dao.Dao;
import com.example.demo.object.File_item;
import com.example.demo.object.Item;
import com.example.demo.service.Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class HelloController implements ApplicationContextAware {
    private ApplicationContext context = null
            ;
    Service gloabal_service = new Service();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/");
    @RequestMapping(value = "/test1")
    public String test1(Model model, HttpServletRequest request) {
        return "test";
    }
    @RequestMapping(value = "/test")
    public String test(Model model, HttpServletRequest request) {
        System.out.println(request.getParameter("interest"));
        for(String s: request.getParameterValues("interest"))
        {
            System.out.println(s);
        }
        return "message";
    }
    @RequestMapping(value = "/index")
    public String index(Model model) {
        model.addAttribute("test","nihao");
        List<Item> item_list = gloabal_service.get_lru_list();
        model.addAttribute("item_list",item_list);
        return "index";
    }
    @RequestMapping(value = "/")
    public String index_default(Model model) {
        model.addAttribute("test","nihao");
        List<Item> item_list = gloabal_service.get_lru_list();
        model.addAttribute("item_list",item_list);
        return "index";
    }
    @RequestMapping(value = "/search")
    public String search(Model model, HttpServletRequest request) {
        String para = request.getParameter("search_words");
        para = para.strip();
        List<Item> item_list = null;
        // no input
        if(para.equals(""))
        {
            item_list = gloabal_service.get_lru_list();
        }
        else
        {
            String [] para_array = para.split(" ");
            item_list = gloabal_service.query_key_word(para_array);
        }
        model.addAttribute("item_list",item_list);
        return "index";
    }
    @RequestMapping(value = "/add")
    public String add(Model model, HttpServletRequest request) {
        model.addAttribute("add_or_update","创建");
        model.addAttribute("url","add_op");
        return "add";
    }
    @RequestMapping(value = "/update")
    public String update(Model model, HttpServletRequest request) {
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
            querry_item = gloabal_service.querry_by_id(new Item(Integer.parseInt(del_id.strip()),"","",""));
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

            model.addAttribute("title",title);
            model.addAttribute("content",content);
            model.addAttribute("id",id);

            // if it has a corresponding file
            String file_name = querry_item.getFile().strip();
            List<File_item> file_lists =  new LinkedList<File_item>();
            for(String item_string: file_name.split(";"))
            {
                if(!item_string.strip().equals(""))
                    file_lists.add(new File_item(item_string.strip(),item_string.strip().split("_")[1]));
            }
            model.addAttribute("file_list",file_lists );
            return "update";
        }
    }
    @RequestMapping(value = "/add_op")
    public String add_op(Model model, HttpServletRequest request, @RequestParam("uploadFile") MultipartFile[] files) {
        if(files == null)
        {
            System.out.println("null?");
            return "message";
        }
        String title = request.getParameter("search_title").strip();
        String content = request.getParameter("search_content").strip();
        // it no parameter
        if(title==null || content == null)
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
        List<Integer> temp_list =  gloabal_service.get_id_by_title(title);
        if(!temp_list.isEmpty())
        {
            gloabal_service.set_message(model,"标题已经存在","index","返回主页","red");
            return "message";
        }
        //store multiple files
        int store_res = gloabal_service.store(files,title,content);
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
            int id = gloabal_service.get_id_by_title(title).get(0);
            model.addAttribute("id",id);
            gloabal_service.set_message(model,message,"detail","查看","darkgoldenrod");
        }
        else
            gloabal_service.set_message(model,message,"index","返回主页","red");
        return "message";
    }
    @RequestMapping(value = "/delete_op")
    public String del_op(Model model, HttpServletRequest request) {
        String del_id = request.getParameter("id");
        if(del_id==null)
        {
            gloabal_service.set_message(model,"no id element","index","返回主页","red");
            return "message";
        }
        Item querry_item = null;
        try{
            querry_item =gloabal_service.querry_by_id(new Item(Integer.parseInt(del_id.strip()),"","",""));
        }catch (NumberFormatException e)
        {
            e.printStackTrace();
            gloabal_service.set_message(model,"id is not a integer","index","返回主页","red");
            return "message";
        }

        // delete the  corresponding file
        if(!gloabal_service.delete_files_from_string(querry_item.getFile().strip()))
            System.out.println("warning: some file is not in the dir");

        //delete in the sql
        if(gloabal_service.delete_item(new Item(Integer.parseInt(del_id.strip()),"","","")))
            gloabal_service.set_message(model,"delete "+del_id+"success...","index","返回主页","darkgoldenrod");
        else
            gloabal_service.set_message(model,"delete "+del_id+"failed...","index","返回主页","red");
        return "message";
    }
    @RequestMapping(value = "/update_op")
    public String update_op(Model model, HttpServletRequest request,@RequestParam("uploadFiles") MultipartFile[] files) {
        String title = request.getParameter("search_title").strip();
        String content = request.getParameter("search_content").strip();
        String id = request.getParameter("id");
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
        // if id is not in the database
        if(gloabal_service.querry_by_id(new Item(id_int,"","","")) == null)
        {
            gloabal_service.set_message(model,"id is not in the database","index","返回主页","red");
            return "message";
        }
        String message;
        int update_res = gloabal_service.update(files,title,content,Integer.parseInt(id.strip()),request.getParameterValues("del_files"));
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
        }
        else{
            gloabal_service.set_message(model,message,"index","返回主页","red");
        }
        return "message";
    }

    @RequestMapping(value = "/detail")
    public String detail(Model model, HttpServletRequest request) {
        String para = request.getParameter("detail");
        if(para == null || para.equals("")){
            gloabal_service.set_message(model,"no id is available", "index","回到主页","red");
            return "message";
        }
        int id;
        try{
            id  = Integer.parseInt(para);
        }
        catch (NumberFormatException e )
        {
            e.printStackTrace();
            gloabal_service.set_message(model,"id is un parseable", "index","回到主页","red");
            return "message";
        }
        Item query_item = gloabal_service.querry_by_id(new Item(id,"","","" ));
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
                String []file_split_names = item_string.split("_");
                if(file_split_names.length == 2)
                    file_lists.add(new File_item(item_string.strip(),file_split_names[1]));
                else
                    System.out.println("invalid file name");
            }
        }
        model.addAttribute("file_list",file_lists );
        return "item";
    }

    @RequestMapping(path = "/downloadAll", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadAll(HttpServletRequest request) throws IOException {
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
        Item res = gloabal_service.querry_by_id(new Item(id_int,"","",""));
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

        // get the zip file
        File file= gloabal_service.zip_multiple_fiels(file_list);
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
        String file_name = request.getParameter("file").strip();
        // get the file
        File file= new File(file_name.strip());
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

    @RequestMapping(value = "/close")
    public String close() {
        Dao.close_connection();
        System.out.println("clear resource");
//        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) context;
//        ctx.registerShutdownHook();
//        ctx.close();
        return "debug";
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
