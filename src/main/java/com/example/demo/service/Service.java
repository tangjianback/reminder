package com.example.demo.service;

import com.example.demo.Dao.Dao;
import com.example.demo.object.Item;
import com.example.demo.object.Item_query;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Service {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/");

    public static String uploadfile_dir;
    public static int page_size = 10;

    static {
        try {
            uploadfile_dir = ResourceUtils.getURL("classpath:").getPath() +"static/uploadFile/";
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Item> get_item_by_page(int page_index, int page_size, int u_id)
    {
        try {
            return Dao.get_items_by_page(page_index,page_size,u_id);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                return  Dao.get_items_by_page(page_index,page_size,u_id);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public int get_item_total(int u_id)
    {
        try {
            return Dao.get_item_total(u_id);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                return Dao.get_item_total(u_id);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    public int get_id_by_title(String title,int uid)
    {
        try {
            return Dao.get_id_by_title(title,uid);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                return Dao.get_id_by_title(title,uid);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public void set_message(Model model, String message, String url, String button_value,String message_color)
    {
        model.addAttribute("message",message);
        model.addAttribute("color",message_color);
        model.addAttribute("url",url);
        model.addAttribute("button_name",button_value);
        return;
    }
    public Item querry_by_id(int id){
        Item res = null;
        try {
            res = Dao.query_by_id(id);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                res = Dao.query_by_id(id);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        return res;
    }
    public boolean alter_item(Item i)
    {
        try {
            return Dao.alter(i);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                return Dao.alter(i);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public boolean delete_item_by_id(int id)
    {
        try {
            return Dao.delete_item_by_id(id);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                return Dao.delete_item_by_id(id);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public boolean add_item(Item i)
    {
        try {
            return Dao.insert(i);
        } catch (SQLException e) {
            Dao.flush_connection();
            try {
                return Dao.insert(i);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public LinkedList<Item> query_key_word(String[] keywords,int u_id) {
    {
        TreeMap<Item, Integer> my_map = new TreeMap<Item, Integer>();
        for(String k : keywords)
        {
            if(k.strip().equals(""))
                continue;
            List<Item> temp_item_list = null;
            try {
                temp_item_list  = Dao.query(k,u_id);
            } catch (SQLException e) {
                Dao.flush_connection();
                try {
                    temp_item_list  = Dao.query(k,u_id);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            for (Item temp_item: temp_item_list)
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
    public int store(MultipartFile[] uploadFiles, String title, String content,int uid,int cate_id,String public_item)
    {
        //check the item type
        if(cate_id == 1)
        {
            for(MultipartFile temp_file: uploadFiles)
            {
                if(!temp_file.getOriginalFilename().endsWith(".png") && !temp_file.getOriginalFilename().endsWith(".jpg"))
                {
                    System.out.println(temp_file.getOriginalFilename());
                    return -4;
                }
            }
        }

        String format = sdf.format(new Date());
        File  folder = new File(Service.uploadfile_dir + format);
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
                stored_fles.add(format+newName);
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

        int publics = 0;
        if(public_item!= null && public_item.strip().equals("1"))
        {
            publics = 1;
        }
        // sql executing
        if(this.add_item(new Item(1,title.strip(),content.strip(),this.files_to_string(stored_fles),uid,cate_id,publics)))
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
    public int update(MultipartFile[] uploadFiles, String title, String content, int ID, String[] del_files, int uid,int cate_id)
    {
        // prepare the new files string
        Item query_item  = this.querry_by_id(ID);
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
                File temp_file = new File(Service.uploadfile_dir+"/" +s.strip());
                if(temp_file.exists())
                    temp_file.delete();
                else
                    System.out.println("warning no file in the dir "+s);
            }
        }

        //store the files
        String format = sdf.format(new Date());
        File folder = new File(Service.uploadfile_dir + format);

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
                old_string_files.add(format+newName);
                add_file_flag = true;
            } catch (IOException e) {
                // if store failed
                e.printStackTrace();
                return -3;
            }
        }
        // execute sql
        // sql successful
        if(this.alter_item(new Item(ID,title.strip(),content.strip(),this.files_to_string(old_string_files),uid,cate_id,query_item.getPublics())))
        {
            boolean del_file_flag = (del_files==null || del_files.length== 0)? false:true;
            // add and delte
            if(add_file_flag && del_file_flag)
                return 3;
            // no file update
            if(!add_file_flag && !del_file_flag)
                return 0;
            // only add file
            if(add_file_flag && !del_file_flag)
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


    public File zip_multiple_fiels(List<String> srcFiles) throws IOException {
        File folder = new File(Service.uploadfile_dir);
        // if dir is
        if(!folder.isDirectory())
        {
            if(folder.mkdirs()==false){
                System.out.println("create folder failed");
                return null;
            }
        }
        // if the compressed.zip exist
        File target_zip_file = new File(Service.uploadfile_dir+"/compressed.zip");
        if(target_zip_file.exists())
            target_zip_file.delete();
        final FileOutputStream fos = new FileOutputStream(target_zip_file);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (String srcFile : srcFiles) {
            File fileToZip = new File(Service.uploadfile_dir+srcFile);
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
                File temp_file= new File(Service.uploadfile_dir+s.strip());
                if(temp_file.exists())
                    temp_file.delete();
                else
                {
                    flag = false;
                    System.out.println(temp_file.getAbsolutePath());
                }
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
