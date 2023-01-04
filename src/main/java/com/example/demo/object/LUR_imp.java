package com.example.demo.object;

import com.example.demo.Dao.Dao;

import java.util.LinkedList;
import java.util.List;

public class LUR_imp {
    LinkedList<Integer> recent_viewed_list = new LinkedList<Integer>();
    final int  list_size_limit = 10;
    public List<Item> get_recent_viewed_items()
    {
        LinkedList<Item> res =new LinkedList<Item>();
        for(int id: recent_viewed_list)
        {
            Item temp_item  =  Dao.query_by_id(new Item(id,"","",""));
            if(temp_item!= null)
            {
                res.add(temp_item);
            }
        }
        return res;
    }
    public void lru_add(int id)
    {
        int id_index = lru_find_index_by_id(id);
        // if not in the list
        if(id_index == -1) {
            recent_viewed_list.addFirst(id);
            if (recent_viewed_list.size() > list_size_limit)
                recent_viewed_list.removeLast();
        }
        else {
            recent_viewed_list.remove(id_index);
            recent_viewed_list.addFirst(id);
        }
    }
    public int lru_find_index_by_id(int id)
    {
        for(int i = 0;i<recent_viewed_list.size();i++) {
            if(id == recent_viewed_list.get(i))
                return i;
        }
        return -1;
    }
    public void lru_del(int id)
    {
      int id_index = lru_find_index_by_id(id);
      if(id_index >=0)
          recent_viewed_list.remove(id_index);
    }
}
