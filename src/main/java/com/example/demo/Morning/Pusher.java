package com.example.demo.Morning;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author cVzhanshi
 * @create 2022-08-04 21:09
 */
public class Pusher {

    public static void main(String[] args) {
        push("oDP1_6QzjppAUi3RS_GdKgD2_C4w");
    }
    private static String appId = "wx4812172b9ff71b25";
    private static String secret = "49c97692933178d4f3f4215fa879cefd";

    private static String templateid = "LDbfo2EAcYcqAOz-aT6pnitMeZXnyzvkSuAMgEm9dp4";

    public static void push(String userid){
        //获取数据
        String today_word = null;
        try {
            String liuqing_words = ResourceUtils.getURL("classpath:").getPath() +"static/words.txt";
            // read
            BufferedReader reader;
            List<String> temp_list = null;
            try {
                reader = new BufferedReader(new FileReader(liuqing_words));
                String line = reader.readLine();
                temp_list = new LinkedList<String>();
                while (line != null) {
                    if(!line.trim().equals(""))
                        temp_list.add(line.trim());
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //write
            try {
                BufferedWriter fw = new BufferedWriter(new FileWriter(liuqing_words));
                for (int i = 1; i < temp_list.size(); i++) {
                    fw.write(temp_list.get(i));
                    fw.newLine();
                 }
                fw.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            if(temp_list.isEmpty())
            {
                System.out.println("no words anymore...");
                today_word = "love forever";
            }
            else
                today_word  = temp_list.get(0).trim();
        } catch (FileNotFoundException e) {
            System.out.println("error when fetch the words");
            today_word = "love forever";
            //throw new RuntimeException(e);
        }
        //today_word = "唐剑是我们最爱的那个人他是我们的朋友，就像人生中最好的兄弟议案大法师代发地方啊的就像人生中最好的兄弟议案大法师代发地方啊的";
        String today_word_1 = "";
        String today_word_2 = "";
        String today_word_3 = "";
        if(today_word.length()<=20)
        {
            today_word_1 = today_word;
        }
        else if(today_word.length() <=40)
        {
            today_word_1 = today_word.substring(0,20);
            today_word_2 = today_word.substring(20,today_word.length());
        }
        else {
            today_word_1 = today_word.substring(0,20);
            today_word_2 = today_word.substring(20,40);
            today_word_3 = today_word.substring(40,today_word.length());
        }

        //1，配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(appId);
        wxStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        //2,推送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(userid)
                .templateId(templateid)
                .build();
        //3,如果是正式版发送模版消息，这里需要配置你的信息
        Weather weather = WeatherUtils.getWeather();
        Map<String, String> map = CaiHongPiUtils.getEnsentence();

        templateMessage.addData(new WxMpTemplateData("tianqi",weather.getText_now(),"#00FFFF"));
        templateMessage.addData(new WxMpTemplateData("low",weather.getLow() + "","#173177"));
        templateMessage.addData(new WxMpTemplateData("temp",weather.getTemp() + "","#EE212D"));
        templateMessage.addData(new WxMpTemplateData("high",weather.getHigh()+ "","#FF6347" ));
        templateMessage.addData(new WxMpTemplateData("windclass",weather.getWind_class()+ "","#42B857" ));
        templateMessage.addData(new WxMpTemplateData("lianai",JiNianRiUtils.getLianAi()+"","#FF1493"));
        templateMessage.addData(new WxMpTemplateData("shengri1",JiNianRiUtils.getBirthday_Jo()+"","#FFA500"));
        templateMessage.addData(new WxMpTemplateData("shengri2",JiNianRiUtils.getBirthday_Hui()+"","#FFA500"));
        templateMessage.addData(new WxMpTemplateData("en",today_word_1,"#C71585"));
        templateMessage.addData(new WxMpTemplateData("en1",today_word_2,"#C71585"));
        templateMessage.addData(new WxMpTemplateData("en2",today_word_3,"#C71585"));
        String beizhu = "";
        if(JiNianRiUtils.getLianAi() % 365 == 0){
            beizhu = "今天是恋爱" + (JiNianRiUtils.getLianAi() / 365) + "周年纪念日！";
        }
        if(JiNianRiUtils.getBirthday_Jo()  == 0){
            beizhu = "今天是剑哥生日，生日快乐呀！";
        }
        if(JiNianRiUtils.getBirthday_Hui()  == 0){
            beizhu = "今天是柳青生日，生日快乐呀！";
        }
        templateMessage.addData(new WxMpTemplateData("beizhu",beizhu,"#FF0000"));

        try {
            System.out.println(templateMessage.toJson());
            System.out.println(wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage));
        } catch (Exception e) {
            System.out.println("推送失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    public static String readFile(String fileName){
        FileReader reader = null;
        StringBuffer sb = new StringBuffer();
        int len = -1;
        char[] data = new char[1024 * 8];
        try {
            reader = new FileReader(fileName);
            while ((len = reader.read(data)) != -1){
                sb.append(data,0,len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(reader!=null){
                    reader.close();
                    reader=null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
