package com.example.biz.upms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.biz.upms.dao.AccountMapper;
import com.example.biz.upms.entity.AccountInfo;

@Service
public class AccountService {
    @Autowired
    private AccountMapper accountMapper;

    public AccountInfo loginByPhone(String phone, String password) {
        return accountMapper.findByPhonePassword(phone, password);
    }

}
