package com.example.arch.oauth.entity;

/**
 * 系统的账户信息表，可以从upms系统中复制过来
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
    /*其它字段省略*/
}
