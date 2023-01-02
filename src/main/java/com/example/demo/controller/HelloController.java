package com.example.demo.controller;
import com.example.demo.Dao.Dao;
import com.example.demo.object.Item;
import com.example.demo.service.Service;
import jakarta.servlet.http.HttpServletRequest;
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
public class HelloController {
    Service gloabal_service = new Service();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/");
    @RequestMapping(value = "/index")
    public String index(Model model) {
        model.addAttribute("test","nihao");
        return "index";
    }
    @RequestMapping(value = "/")
    public String index_default(Model model) {
        model.addAttribute("test","nihao");
        return "index";
    }
    @RequestMapping(value = "/search")
    public String search(Model model, HttpServletRequest request) {
        String para = request.getParameter("search_words");
        para = para.strip();
        String [] para_array = para.split(" ");
        List<Item> item_list = gloabal_service.query_key_word(para_array);
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
        // get item of del_id
        Item querry_item = gloabal_service.querry_by_id(new Item(Integer.parseInt(del_id.strip()),"","",""));
        if(querry_item == null)
        {
            model.addAttribute("message","no record for this id");
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
            if(file.strip()!="")
                model.addAttribute("file",file.split("_")[1]);
            else
                model.addAttribute("file","no file");
            model.addAttribute("id",id);
            return "add";
        }
    }
    @RequestMapping(value = "/add_op")
    public String add_op(Model model, HttpServletRequest request, MultipartFile uploadFile) {
        String title = request.getParameter("search_title").strip();
        String content = request.getParameter("search_content").strip();
        String message;
        int store_res = gloabal_service.store(uploadFile,title,content);
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
                message = "no file upload";
                break;
            default:
                message = "add complete";
                break;
        }
        model.addAttribute("message",message);
        // store successful
        if(store_res >=0)
        {
            model.addAttribute("url","detail");
            int id = gloabal_service.get_id_by_title(title);
            if(id>0)
            {
                model.addAttribute("id",id);
                model.addAttribute("button_name","查看");
            }

            else
                model.addAttribute("message","reload item error");
        }
        return "message";
    }
    @RequestMapping(value = "/delete_op")
    public String del_op(Model model, HttpServletRequest request) {
        String del_id = request.getParameter("id");
        Item querry_item =gloabal_service.querry_by_id(new Item(Integer.parseInt(del_id.strip()),"","",""));
        File f = new File(querry_item.getFile());
        if(f.exists())
            f.delete();

        if(gloabal_service.delete_item(new Item(Integer.parseInt(del_id.strip()),"","","")))
            model.addAttribute("message","delete "+ del_id+"success...");
        else
            model.addAttribute("message","delete "+ del_id+"failed...");
        model.addAttribute("url","index");
        model.addAttribute("button_name","返回主页");
        return "message";
    }
    @RequestMapping(value = "/update_op")
    public String update_op(Model model, HttpServletRequest request,MultipartFile uploadFile) {
        String title = request.getParameter("search_title").strip();
        String content = request.getParameter("search_content").strip();
        String id = request.getParameter("id");
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
                message = "no file upload";
                break;
            default:
                message = "add complete";
                break;
        }
        model.addAttribute("message",message);
        // store successful
        if(update_res >=0)
        {
            model.addAttribute("url","detail");
            model.addAttribute("id",id);
            model.addAttribute("button_name","查看");
        }
        else{
                model.addAttribute("message","reload item error");
        }
        return "message";
    }

    @RequestMapping(value = "/detail")
    public String detail(Model model, HttpServletRequest request) {
        String para = request.getParameter("detail");
        int id = Integer.parseInt(para);
        Item querry_item = gloabal_service.querry_by_id(new Item(id,"","","" ));
        model.addAttribute("title",querry_item.getTitle()+"");
        model.addAttribute("content",querry_item.getContent()+"");
        String file_name = querry_item.getFile();
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
        model.addAttribute("id",querry_item.getId()+"");
        return "item";
    }

    @RequestMapping(path = "/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(HttpServletRequest request) throws IOException {

        String file_name = request.getParameter("file").strip();
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
    public String close(Model model, HttpServletRequest request) {
        Dao.close_connection();
        System.out.println("clear resource");
        return "debug";
    }
}
