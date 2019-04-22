package ldn.service.UserServiceImpl;

import com.alibaba.druid.util.StringUtils;
import ldn.DataObject.UserDO;
import ldn.DataObject.UserPasswordDO;
import ldn.dao.UserDOMapper;
import ldn.dao.UserPasswordDOMapper;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.service.UserService;
import ldn.service.model.UserModel;
import ldn.validator.ValidationImpl;
import ldn.validator.ValidationResult;
import ldn.DataObject.UserDO;
import ldn.DataObject.UserPasswordDO;
import ldn.dao.UserDOMapper;
import ldn.dao.UserPasswordDOMapper;
import ldn.error.BussinessException;
import ldn.error.EmBussinessError;
import ldn.service.UserService;
import ldn.service.model.UserModel;
import ldn.validator.ValidationImpl;
import ldn.validator.ValidationResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Autowired
    private ValidationImpl validator;
    @Override
    public UserModel getUserId(Integer id) {
        //调用UserDOMapper获取对应的用户DataObeject
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);//获取用户信息
        if(userDO == null){
            return null;
        }

        //通过用户ID获取用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO,userPasswordDO);

    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BussinessException {
        if(userModel ==null)
        {
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//            ||StringUtils.isEmpty(userModel.getTelephone())
//            ||userModel.getAge() == null
//            ||userModel.getGender() == null)
//        {
//            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR);
//        }
        //代替上面注释的内容
        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors())
        {
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR, result.getErrMsg());
        }
        //实现UserModel->DataObject的方法
        UserDO userDO = convertFromUserModel(userModel);
//        userDOMapper.insertSelective(userDO);
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex) {
            throw new BussinessException(EmBussinessError.PARANETDER_VALIDATION_ERROR, "手机号已注册过");
        }
        UserDO userDO1 = userDOMapper.selectByTelephone(userDO.getTelephone());
        userModel.setId(userDO1.getId());//确保password的id 与 user_info的id一致
        UserPasswordDO userPasswordDO = convertPasswordFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);



    }

    @Override
    public UserModel validateLogin(String telephone, String password) throws BussinessException {
        //通过用户手机号获取用户信息
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null)
            throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
//        //通过用户id获取用户密码
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        //DataObject->UserModel
       UserModel userModel = convertFromDataObject(userDO,userPasswordDO);

       //将用户注册的密码与登录的密码进行比较
       if(!StringUtils.equals(password, userModel.getPassword()))
       {//不相等则登录失败
           throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
       }

        //UserModel userModel = null;
        return userModel;


    }

    private UserPasswordDO convertPasswordFromUserModel(UserModel userModel)
    {
        if(userModel == null)
            return null;
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setPassword(userModel.getPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserDO convertFromUserModel(UserModel userModel)
    {
        if(userModel == null)
            return null;
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }
    //融合UserDO和UserPasswordDo的信息，变成UserModel
    //UserModel是service层的模型
    //UserDo和UserPasswordDO是DataObject层
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO)
    {
        if(userDO == null)
            return  null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);//将userDo里面的属性直接复制到userModel
                                                    //userModel需要有setter才能复制成功！！！
        if(userPasswordDO != null)
            userModel.setPassword(userPasswordDO.getPassword());//userModel中密码的设置需要从userPasswordDO获取
        return userModel;
    }
}
