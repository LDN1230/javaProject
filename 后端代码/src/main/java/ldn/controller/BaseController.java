package ldn.controller;

import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";
    //定义exceptionhandle解决未被controller层吸收的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(HttpServletRequest request, Exception ex)
    {
        Map<String, Object> responseData = new HashMap<>();
        if(ex instanceof BussinessException) {
            BussinessException bussinessException = (BussinessException) ex;
            responseData.put("errCode", ((BussinessException) ex).getErrCode());
            responseData.put("errMsg", ((BussinessException) ex).getErrMsg());
        }
        else
        {
            responseData.put("errCode", EmBussinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", EmBussinessError.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(responseData, "fail");
    }
}
