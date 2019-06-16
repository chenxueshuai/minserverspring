package com.yonyou.shuai.service.imp;

import com.yonyou.util.AgentUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;
import sun.management.Agent;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by shuai on 12/4/2018.
 */
@Repository
public class AgentServer {
    public static final String AGENTHOST = "219.142.41.78";
    public static final String AGENTPORT = "18810";
    public static final String AGENTURL = "http://219.142.41.78:18810/umserver/core";
    public static final String WEIXINFLAG = "WEIXIN";
    public String loginCas(String user, String pass,String openid){
        // 创建url资源
        JSONObject obj = new JSONObject();
        try{
            obj.put("user",user);
            obj.put("pass",pass);
            obj.put("userId",user);
        }catch (JSONException e){

        }

        String result = "";
        result = getAgentResponse("HGMAServer","login","com.yonyou.uap.haiguan.ApplyController",obj,"weixin"+openid);
        return result;
    }
    // 报关单查询
    public String queryDecList(String queryParam){
        String result = "";
        try{
            JSONObject  obj = new JSONObject(queryParam);
            String openid = obj.getString("openId");
            result = getAgentResponse("hgmobdecapp","getDecStatusMessageQuery","com.mobspgov.www.HgDecMessageSp",obj,"weixin"+openid);
        }catch (JSONException e){
        }
        return result;
    }
    private String getAgentResponse(String appid, String actionname, String viewid,JSONObject obj, String openid){
        String result = "";
        try{
            String  data =   AgentUtil.getParamsMA(appid,actionname,viewid,obj,WEIXINFLAG+openid);
            String agentObj = "tp=none&data="+data;
            System.err.println(agentObj);
            URL url = new URL(AGENTURL);
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置维持长连接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(agentObj.length()));
            // 开始连接请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write(agentObj.getBytes());
            out.flush();
            // 请求返回的状态
            System.err.println("conn.getResponseCode()  : "+conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
                byte[] data1 = new byte[in.available()];
                in.read(data1);
                result = new String(data1);
                System.err.println(result);
            } else {

            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return result;
    }
}
