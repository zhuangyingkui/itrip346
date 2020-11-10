package cn.itrip.controller;

import cn.itrip.dao.itripHotel.ItripHotelMapper;
import cn.itrip.dao.itripUser.ItripUserMapper;
import cn.itrip.pojo.ItripUser;
import cn.itrip.pojo.ItripUserVO;
import com.alibaba.fastjson.JSONArray;
import cz.mallat.uasparser.UserAgentInfo;
import io.swagger.annotations.Api;
import itrip.common.*;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
@Api(value = "appinfo",description = "用户模块")

@Controller
public class Holtelcontroller {
    @Resource
    ItripUserMapper userdao;
    @Resource
    JredisApi jredisAip;
    @Resource
    ItripUserMapper dao1;
    @RequestMapping("/api/registerbyphone")
    @ResponseBody
    public Dto insert(@RequestBody ItripUserVO itripUserVO, HttpServletRequest request) throws Exception {
       ItripUser itripUser=new ItripUser();
        itripUser.setUserCode(itripUserVO.getUserCode());
        itripUser.setUserPassword(MD5.getMd5(itripUserVO.getUserPassword(),32));
         itripUser.setUserName(itripUserVO.getUserName());
        itripUser.setActivated(0);
 //发送短信

        dao1.insertItripUser(itripUser);


       return  DtoUtil.returnDataSuccess("发送成功");

    }
    public void sentsms(String phone,String sms){


        
    }

    @RequestMapping("/api/dologin")
    @ResponseBody
    public Dto dologin(String name, String password ,HttpServletRequest request) throws Exception {
        String ss=MD5.getMd5(password,32);
        System.out.println(ss);
        ItripUser user= userdao.dologin(name, MD5.getMd5(password,32));
        if(user!=null){

             String token=generateToken(request.getHeader("User-Agent"),user);
              String value= JSONArray.toJSONString(user);
            if(jredisAip.getRedis(token)==null) {
                jredisAip.SetRedis(token, value, 60 * 60 * 2);
            }
            ItripTokenVO jjj=new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis()+7200,Calendar.getInstance().getTimeInMillis());


            return DtoUtil.returnDataSuccess(jjj);

        }
        return  DtoUtil.returnSuccess("登陆失败");
    }
    public String generateToken(String agent, ItripUser user) {
        // TODO Auto-generated method stub
        try {
            UserAgentInfo userAgentInfo = UserAgentUtil.getUasParser().parse(
                    agent);
            StringBuilder sb = new StringBuilder();
            sb.append("token:");//统一前缀
            if (userAgentInfo.getDeviceType().equals(UserAgentInfo.UNKNOWN)) {
                if (UserAgentUtil.CheckAgent(agent)) {
                    sb.append("MOBILE-");
                } else {
                    sb.append("PC-");
                }
            } else if (userAgentInfo.getDeviceType()
                    .equals("Personal computer")) {
                sb.append("PC-");
            } else
                sb.append("MOBILE-");
//			sb.append(user.getUserCode() + "-");
            sb.append(MD5.getMd5(user.getUserCode(),32) + "-");//加密用户名称
            sb.append(user.getId() + "-");
            sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                    + "-");
            sb.append(MD5.getMd5(agent, 6));// 识别客户端的简化实现——6位MD5码

            return sb.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Resource
   ItripHotelMapper dao;
   @RequestMapping("/index")
   @ResponseBody
   public Object getlist() {

    try {
        return dao.getItripHotelById(new Long("1"));
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
}
