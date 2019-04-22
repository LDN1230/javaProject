package ldn.service.model;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UserModel {
    private Integer id;
    @NotBlank(message = "用户名不能为空")
    private String name;
    @NotBlank(message = "手机号不能为空")
    private String telephone;

    private String registerMode;

    private String thirdPartyId;
    @NotNull(message = "年龄不能不填写")
    @Min(value = 0, message = "年龄必须大于0岁")
    @Max(value = 150, message = "年龄必须小于150岁")
    private Integer age;
    @NotNull(message = "性别不能不填写")
    private Integer gender;
    @NotBlank(message = "密码不能为空")
    private String password;//不需要加getter方法,要加setter方法

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", telephone='" + telephone + '\'' +
                ", registerMode='" + registerMode + '\'' +
                ", thirdPartyId='" + thirdPartyId + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", password='" + password + '\'' +
                '}';
    }
}
