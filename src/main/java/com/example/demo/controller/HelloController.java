package com.example.demo.controller;
import com.example.demo.Dao.Dao;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class HelloController implements ApplicationContextAware {
    private ApplicationContext context = null
            ;
    Service gloabal_service = new Service();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/");
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

            model.addAttribute("add_or_update","更新");
            model.addAttribute("url","update_op");
            model.addAttribute("title",title);
            model.addAttribute("content",content);
            // if it has a corresponding file
            if(file.strip()!="")
                model.addAttribute("file",file.split("_")[1]);
            else
                model.addAttribute("file","No File");
            model.addAttribute("id",id);
            return "add";
        }
    }
    @RequestMapping(value = "/add_op")
    public String add_op(Model model, HttpServletRequest request, MultipartFile uploadFile) {
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
            gloabal_service.set_message(model,"title already exists","index","返回主页","red");
            return "message";
        }
        int store_res = gloabal_service.store(uploadFile,title,content);
        String message;
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
        File f = new File(querry_item.getFile());
        if(f.exists())
            f.delete();
        // if delete success
        if(gloabal_service.delete_item(new Item(Integer.parseInt(del_id.strip()),"","","")))
            gloabal_service.set_message(model,"delete "+del_id+"success...","index","返回主页","darkgoldenrod");
        else
            gloabal_service.set_message(model,"delete "+del_id+"failed...","index","返回主页","red");
        return "message";
    }
    @RequestMapping(value = "/update_op")
    public String update_op(Model model, HttpServletRequest request,MultipartFile uploadFile) {
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
        int update_res = gloabal_service.update(uploadFile,title,content,Integer.parseInt(id.strip()));
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
                message = "修改成功(未上传文件)";
                break;
            default:
                message = "修改成果(已上传文件)";
                break;
        }

        // store successful
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

        model.addAttribute("title",query_item.getTitle()+"");
        model.addAttribute("content",query_item.getContent()+"");
        String file_name = query_item.getFile();
        if(file_name == "")
        {
            model.addAttribute("file","");
            model.addAttribute("file_short","");
        }
        else
        {
            model.addAttribute("file",file_name);
            model.addAttribute("file_short",file_name.split("_")[1]);
        }
        model.addAttribute("id",query_item.getId()+"");
        return "item";
    }

    @RequestMapping(path = "/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(HttpServletRequest request) throws IOException {
        String file_name = request.getParameter("file").strip();
        // if the no valid file name
        if(file_name==null || file_name.strip().equals(""))
        {
            return null;
        }
        // file does not exist
        File file= new File(file_name);
        if(!file.exists())
        {
            return null;
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+file_name.split("_")[1]);
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
