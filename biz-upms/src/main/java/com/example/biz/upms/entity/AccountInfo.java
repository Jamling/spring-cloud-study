package com.example.biz.upms.entity;

/**
 * 系统的账户信息表
 */
public class AccountInfo implements java.io.Serializable {

    private static final long serialVersionUID = -101233784802109241L;

    /**
     * 手机号码，作为登录名
     */
    public String phone;

    /**
     * 账号密码，使用自定义的加密方式加密
     */
    public String password;
    
    public String head;
    
    public String email;
}
