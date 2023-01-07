package com.example.demo.service;

import com.example.demo.Dao.Dao;
import com.example.demo.object.Item;
import com.example.demo.object.Item_query;
import com.example.demo.object.LUR_imp;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Service {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/");
    LUR_imp lru_imp = new LUR_imp();
    public List<Integer> get_id_by_title(String title)
    {
        List<Integer> res = Dao.get_id_by_title(title);
        if(!res.isEmpty())
            lru_imp.lru_add(res.get(0));
        return res;
    }
    public void set_message(Model model, String message, String url, String button_value,String message_color)
    {
        model.addAttribute("message",message);
        model.addAttribute("color",message_color);
        model.addAttribute("url",url);
        model.addAttribute("button_name",button_value);
        return;
    }
    public Item querry_by_id(Item i){
        lru_imp.lru_add(i.getId());
        Item res =  Dao.query_by_id(i);
        return res;
    }
    public boolean alter_item(Item i)
    {
        lru_imp.lru_add(i.getId());
        return Dao.alter(i);
    }
    public boolean delete_item(Item i)
    {
        lru_imp.lru_del(i.getId());
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
    public int store(MultipartFile[] uploadFiles, String title, String content)
    {
        String format = sdf.format(new Date());
        File folder = new File("uploadFile/" + format);

        // if parent dir not exists, then create it, if fails return -1
        if(!folder.isDirectory() && folder.mkdirs() == false)
        {
            return -1;
        }
        // store uploaded file and store in mysql
        List<String> stored_fles = new LinkedList<String>();
        for(MultipartFile temp_file: uploadFiles)
        {
            // if the file is not valid skip it
            if(temp_file.isEmpty() || temp_file.getSize()== 0 || temp_file.getName().strip().equals(""))
                continue;
            String newName = UUID.randomUUID().toString() +"_"+ temp_file.getOriginalFilename();
            try {
                File new_file = new File(folder.getAbsolutePath()+"/"+newName);
                temp_file.transferTo(new_file);
                stored_fles.add("uploadFile/" + format+"/"+newName);
            } catch (IOException e) {
                // if store failed then delete all files stored already
                e.printStackTrace();
                for(String delete_name: stored_fles)
                {
                    File temp_file_delete = new File(delete_name);
                    if(temp_file_delete.exists())
                        temp_file_delete.delete();
                }
                return -3;
            }
        }
        // sql executing
        if(this.add_item(new Item(1,title.strip(),content.strip(),this.files_to_string(stored_fles))))
        {
            if(stored_fles.isEmpty())
                return 0;
            else
                return 1;
        }
        else
        {
            for(String delete_name: stored_fles)
            {
                File temp_file_delete = new File(delete_name);
                if(temp_file_delete.exists())
                    temp_file_delete.delete();
            }
            return -2;
        }

    }
    //    finished :1; nofile:0; dircreate_file:-1; sqlerror:-2; storefail:-3;
    public int update(MultipartFile[] uploadFiles, String title, String content, int ID, String[] del_files)
    {
        // prepare the new files string
        Item query_item  = this.querry_by_id(new Item(ID,"","",""));
        if(query_item==null)
            return -2;
        List<String> old_string_files = this.get_files_by_string(query_item.getFile());

        //it del_files is okk ,prepare sql string array and delete files
        if(del_files!=null && del_files.length>0)
        {
            // sql array prepare
            for(String s:del_files)
                old_string_files.remove(s);

            //delete the requires files
            for(String s: del_files)
            {
                File temp_file = new File(s.strip());
                if(temp_file.exists())
                    temp_file.delete();
                else
                    System.out.println("warning no file in the dir "+s);
            }
        }

        //store the files
        String format = sdf.format(new Date());
        File folder = new File("uploadFile/" + format);

        // store files and adding it the list
        // if parent dir not exists, then create it, if fails return -1
        if(!folder.isDirectory() && folder.mkdirs() == false)
        {
            return -1;
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
                old_string_files.add("uploadFile/" + format+"/"+newName);
                add_file_flag = true;
            } catch (IOException e) {
                // if store failed
                e.printStackTrace();
                return -3;
            }
        }
        // execute sql
        // sql successful
        if(this.alter_item(new Item(ID,title.strip(),content.strip(),this.files_to_string(old_string_files))))
        {
            // add and delte
            if(add_file_flag && del_files.length!= 0)
                return 3;
            // no file update
            if(!add_file_flag && del_files.length==0)
                return 0;
            // only add file
            if(add_file_flag && del_files.length==0)
            {
                return 1;
            }
            // only del file
            return 2;
        }
        else
        {
            return -2;
        }
    }

    public List<Item> get_lru_list()
    {
        return this.lru_imp.get_recent_viewed_items();
    }

    public File zip_multiple_fiels(List<String> srcFiles) throws IOException {
        File folder = new File("uploadFile");
        // if dir is
        if(!folder.isDirectory())
        {
            if(folder.mkdirs()==false){
                System.out.println("create folder failed");
                return null;
            }
        }
        // if the compressed.zip exist
        File target_zip_file = new File("uploadFile/compressed.zip");
        if(target_zip_file.exists())
            target_zip_file.delete();
        final FileOutputStream fos = new FileOutputStream(target_zip_file);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (String srcFile : srcFiles) {
            File fileToZip = new File(srcFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
        return target_zip_file;
    }
    public boolean delete_files_from_string(String total_files) {
        boolean flag = true;
        for(String s: total_files.split(";"))
        {
            if(!s.equals(""))
            {
                File temp_file= new File(s.strip());
                if(temp_file.exists())
                    temp_file.delete();
                else
                    flag = false;
            }
        }
        return flag;
    }
    public List<String> get_files_by_string(String s )
    {
        List<String> s_list = new LinkedList<String>();
        for(String i: s.split(";"))
        {
            if(!i.strip().equals(""))
                s_list.add(i);
        }
        return  s_list;
    }
    public String files_to_string(List<String> temp_lists)
    {
        String res="";
        for(String i: temp_lists)
        {
            res+=i+";";
        }
        return  res;
    }
}
