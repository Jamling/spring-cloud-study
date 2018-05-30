package com.example.arch.oauth.prd;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MyPasswordEncoder implements PasswordEncoder {

    private PasswordEncoder delegating = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private String md5(String str) {
        if (str == null) {
            return null;
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return MD5Encoder.encode(md.digest(str.getBytes()));
    }

    @Override
    public String encode(CharSequence rawPassword) {
        // 加密规则为md5(md5(password)+password)
        String str = rawPassword.toString();
        str = md5(str);
        str = str + rawPassword;
        str = md5(str);
        return str;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null && encodedPassword == null) {
            return true;
        }
        String id = extractId(encodedPassword);
        if (id == null) {
            // 顺带修复一下Spring明文密码校验
            return delegating.matches(rawPassword, "{noop}" + encodedPassword);
        } else if ("my".equals(id)) {
            return encode(rawPassword).equals(extractEncodedPassword(encodedPassword));
        }
        return delegating.matches(rawPassword, encodedPassword);
    }

    private static final String PREFIX = "{";
    private static final String SUFFIX = "}";

    private String extractId(String prefixEncodedPassword) {
        if (prefixEncodedPassword == null) {
            return null;
        }
        int start = prefixEncodedPassword.indexOf(PREFIX);
        if (start != 0) {
            return null;
        }
        int end = prefixEncodedPassword.indexOf(SUFFIX, start);
        if (end < 0) {
            return null;
        }
        return prefixEncodedPassword.substring(start + 1, end);
    }

    private String extractEncodedPassword(String prefixEncodedPassword) {
        int start = prefixEncodedPassword.indexOf(SUFFIX);
        return prefixEncodedPassword.substring(start + 1);
    }

    public static void main(String[] args) {
        MyPasswordEncoder encoder = new MyPasswordEncoder();
        System.out.println(encoder.encode("123456"));
    }
}
