package com.yonyou.shuai.service.imp;

import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shuai on 12/5/2018.
 */

@Repository
public class WeiXinServer {
    public static final String appid = "wx45ba5f223d38d20b";
    public static final String secret = "051cc72d4f1162c9c7e1c44a780a0ba9";
    public String getOpenId(String code){
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL("https://api.weixin.qq.com/sns/jscode2session?appid="+appid+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code");
            //得到connection对象。
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //连接
            connection.connect();
            //得到响应码
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                //得到响应流
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine())!=null){
                    builder.append(line);
                }
                reader.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
