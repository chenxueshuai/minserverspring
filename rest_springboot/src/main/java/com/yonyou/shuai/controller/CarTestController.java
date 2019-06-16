package com.yonyou.shuai.controller;

import com.yonyou.shuai.service.imp.AgentServer;
import com.yonyou.shuai.service.imp.WeiXinServer;
import com.yonyou.shuai.vo.Car;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by shuai on 7/22/2018.
 */

@RestController
public class CarTestController {
    public static String session_key = "";
    private final AgentServer agentServer;
    private final WeiXinServer weiXinServer;
    @Autowired
    public CarTestController(AgentServer agentServer, WeiXinServer weiXinServer){
        this.agentServer = agentServer;
        this.weiXinServer = weiXinServer;
    }
    @GetMapping("/car/{id}")
    public Car getCar(@PathVariable String id, @RequestParam(required = false) String price){
        Car car = new Car();
        car.setId(id);
        car.setPrice(price);
        return car;
    }
    @GetMapping("/cas/login")
    public String login(@RequestParam(required = false) String casUser, @RequestParam(required = false) String casPass,@RequestParam String code){
        JSONObject resultson = new JSONObject();
        // 获取openid
        String openIdAndSession = weiXinServer.getOpenId(code);
        // 验证cas登陆
      try{
        session_key = new JSONObject(openIdAndSession).getString("session_key");
          String openId = new JSONObject(openIdAndSession).getString("openid");
           String result = agentServer.loginCas(casUser,casPass,openId);
        // 绑定openid
          // 1. 将 session_key和第三方的sessionid  存储在session信息中, 并将sessionid   2. 将这些id 全部给客户端, 服务端不做保存,   finally 选择方案  是否需要https的Agent呢?
        // 回写openid  sessionid  cas返回的用户信息
            resultson.put("openId",openId);
            resultson.put("result",result);
        }catch (JSONException e){

        }
        return resultson.toString();
    }
    @PostMapping("/dec/query")
    public String decQueryList(@RequestBody(required = false) String decQueryParams){
        String result = agentServer.queryDecList(decQueryParams.toString());
        return result;
    }
}
