package com.example.demo.service;

import com.example.demo.Dao.Dao;
import com.example.demo.object.Item;
import com.example.demo.object.Item_query;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Service {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/");
    public int get_id_by_title(String title)
    {
        List<Integer> id_list = Dao.get_id_by_title(title);
        if(id_list.size()==1)
            return id_list.get(0);
        else
        {
            if(id_list.size()==0)
                System.out.println("no such a title");
            else if(id_list.size()>1)
                System.out.println("multiple title");
            return -1;
        }
    }
    public Item querry_by_id(Item i){
        return Dao.query_by_id(i);
    }
    public boolean alter_item(Item i)
    {
        return Dao.alter(i);
    }
    public boolean delete_item(Item i)
    {
        return Dao.delete(i);
    }
    public boolean add_item(Item i)
    {
        return Dao.insert(i);
    }
    public LinkedList<Item> query_key_word(String[] keywords) {
    {
        TreeMap<Item, Integer> my_map = new TreeMap<Item, Integer>();
        for(String k : keywords)
        {
            if(k.strip().equals(""))
                continue;
            for (Item temp_item: Dao.query(k))
            {
                // if it contains the item
                if(!my_map.containsKey(temp_item))
                {
                    my_map.put(temp_item,1);
                }
                else
                {
                    my_map.put(temp_item,my_map.get(temp_item)+1);
                }
            }
        }

        ArrayList<Item_query> array_list = new ArrayList<Item_query>();
        for(Map.Entry<Item,Integer> pair: my_map.entrySet())
        {
            array_list.add(new Item_query(pair.getKey(),pair.getValue()));
        }
        Collections.sort(array_list);
        LinkedList<Item> res = new LinkedList<Item>();
        for(Item_query i: array_list)
        {
            res.add(i.getItem());
        }
        return res;
    }

    }

//    finished 1: no dile but add 0; dircreate_file:-1; sqlerror:-2; storefail:-3;
    public int store(MultipartFile uploadFile, String title, String content)
    {
        String format = sdf.format(new Date());
        File folder = new File(System.getProperty("user.dir")+"/uploadFile/" + format);
        // if the file is empty, return 0
       if(uploadFile.isEmpty())
       {
           if(this.add_item(new Item(1,title.strip(),content.strip(),"")))
               return 0;
           else
           {
               return -2;
           }
       }
        else
        {
            // if parent dir not exists, then create it, if fails return -1
            if(!folder.isDirectory() && folder.mkdirs() == false)
            {
                return -1;
            }
            // store uploaed file and store in mysql
            String newName = UUID.randomUUID().toString() +"_"+ uploadFile.getOriginalFilename();
            try {
                File new_file = new File(folder, newName);
                uploadFile.transferTo(new_file);
                // if insert sql success then return 1
                if(this.add_item(new Item(1,title.strip(),content.strip(),new_file.getAbsolutePath())))
                    return 1;
                else
                {
                    new_file.delete();
                    return -2;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return -3;
            }
        }
    }
    //    finished :1; nofile:0; dircreate_file:-1; sqlerror:-2; storefail:-3;
    public int update(MultipartFile uploadFile, String title, String content, int ID)
    {
        String format = sdf.format(new Date());
        File folder = new File(System.getProperty("user.dir")+"/uploadFile/" + format);
        String old_file_name = this.querry_by_id(new Item(ID,"","","")).getFile();
        // if the file is empty, return 0
        if(uploadFile.isEmpty())
        {
            if(this.alter_item(new Item(ID,title.strip(),content.strip(),old_file_name)))
                return 0;
            else
            {
                return -2;
            }
        }
        // file exists...
        else
        {
            // if parent dir not exists, then create it, if fails return -1
            if(!folder.isDirectory() && folder.mkdirs() == false)
            {
                return -1;
            }
            // store uploaed file and store in mysql
            String newName = UUID.randomUUID().toString() +"_"+ uploadFile.getOriginalFilename();
            try {
                File new_file = new File(folder, newName);
                // store new file
                uploadFile.transferTo(new_file);
                // delete old file
                if(old_file_name.strip()!="")
                {
                    File temp_f = new File(old_file_name);
                    if(temp_f.exists())
                        temp_f.delete();
                }
                // if insert sql success then return 1
                if(this.alter_item(new Item(ID,title.strip(),content.strip(),new_file.getAbsolutePath())))
                    return 1;
                else
                {
                    new_file.delete();
                    return -2;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return -3;
            }
        }
    }
}
