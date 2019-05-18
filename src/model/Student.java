package model;


import annotation.MinLengthRule;
import annotation.NotNullRule;
import annotation.RegularRule;

public class Student {

    @NotNullRule(message = "id不能为空")
    private Integer id;

    @MinLengthRule(minLength = 1,message = "姓名不能为空")
    private String name;

    @NotNullRule(message = "手机号不能为空")
    @RegularRule(expression = "[0-9]{11}", message = "手机格式错误")
    private String telephone;

    private String birthday;

    public int getId() {
        return id;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

}
