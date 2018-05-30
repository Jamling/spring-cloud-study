package com.example.biz.upms.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.biz.upms.entity.AccountInfo;

@Mapper
public interface AccountMapper {
    @Select("select t.phone, t.uid, t.head, t.email from qw_member t where t.phone = #{phone} and t.password = #{password}")
    AccountInfo findByPhonePassword(@Param("phone") String phone, @Param("password") String password);
}
