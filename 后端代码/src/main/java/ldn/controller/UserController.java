package ldn.controller;

import com.alibaba.druid.util.StringUtils;
import ldn.controller.ViewObject.UserVO;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.response.CommonReturnType;
import ldn.service.UserService;
import ldn.service.model.UserModel;
import ldn.controller.ViewObject.UserVO;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.response.CommonReturnType;
import ldn.service.UserService;
import ldn.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
//@CrossOrigin
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    @CrossOrigin(allowCredentials = "true", allowedHeaders = "*'")
    public CommonReturnType login(@RequestParam(name="telephone") String telephone,
                                  @RequestParam(name="password") String password ) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(StringUtils.isEmpty(telephone) || StringUtils.isEmpty(password))
        {
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR);
        }

        //用户登录服务，用来校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telephone, this.encodedByMd5(password));
        //将登录凭证加入到用户登录成功的session中
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);
    }

    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    @CrossOrigin(allowCredentials = "true", allowedHeaders = "*'")
    public CommonReturnType register(@RequestParam(name="telephone") String telephone,//用户注册需要的信息
                                     @RequestParam(name="otpCode") String otpCode,
                                    @RequestParam(name="name") String name,
                                     @RequestParam(name="password") String password,
                                     @RequestParam(name="gender") Integer gender,
                                     @RequestParam(name="age") Integer age
    ) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号与对应optCode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if(!StringUtils.equals(otpCode, inSessionOtpCode))
        {
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR, "短信验证码错误");
        }
        //注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender);
        userModel.setTelephone(telephone);
        userModel.setRegisterMode("byphone");
        userModel.setPassword(this.encodedByMd5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String encodedByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }
    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    @CrossOrigin(allowCredentials = "true", allowedHeaders = "*'")
    public CommonReturnType getOtp(@RequestParam(name="telephone") String telephone)
    {
        //需要按照一定规则生成otp验证码
        Random random = new Random();
        int radomInt = random.nextInt(99999);
        radomInt += 10000;
        String otpCode = String.valueOf(radomInt);
        //将otp验证码与对应用户的手机号相关联,使用httpsession绑定他的手机号与otpcode
        httpServletRequest.getSession().setAttribute(telephone,otpCode);
        //将otp验证码通过短信通道发送给用户
        System.out.println("telephone:"+telephone+" otpcode："+otpCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody//用返回值就要加
    public CommonReturnType getUser(@RequestParam(name="id") Integer id)throws BussinessException
    {
        //调用service服务获取对应的用户对象并返回给前端
        UserModel userModel = userService.getUserId(id);

        //若获取的用户信息不存在
        if(userModel == null)
            throw new BussinessException(EmBussinessError.USER_NOOT_EXIST);

        //将核心领域模型用户对象转化为可供UI使用的ViewObject
        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

   private UserVO convertFromModel(UserModel userModel)
   {
       if(userModel == null)
           return  null;
       UserVO userVO = new UserVO();
       BeanUtils.copyProperties(userModel, userVO);
       return userVO;
   }

}
