package ldn.service;

import ldn.error.BussinessException;
import ldn.service.model.UserModel;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserId(Integer id);

    void register(UserModel userModel) throws BussinessException;

    UserModel validateLogin(String telephone, String password) throws BussinessException;
}
