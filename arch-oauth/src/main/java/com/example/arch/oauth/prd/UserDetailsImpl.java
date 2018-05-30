package com.example.arch.oauth.prd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.arch.oauth.entity.AccountInfo;

/**
 * 真实的UserDetail实现
 */
public class UserDetailsImpl implements UserDetails, CredentialsContainer {
    private static final long serialVersionUID = 1970376243925526803L;
    private AccountInfo user;

    public UserDetailsImpl(AccountInfo user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        // for (SysRole role : user.get) {
        // authorityList.add(new SimpleGrantedAuthority(role.getRoleCode()));
        // }
        authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorityList;
    }

    @Override
    public String getPassword() {
        return "{my}" + user.password;
    }

    @Override
    public String getUsername() {
        return user.phone;
    }

    // 下面的标识，可根据实际AccountInfo标志位来，比如remove_at不为空，表示账户已失效
    @Override
    public boolean isAccountNonExpired() {
        // 账户未失效：true
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 账户未锁定：true
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 证书未过期：true
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 账户有效：true
        return true;
    }

    @Override
    public void eraseCredentials() {
        if (user != null) {
            user.password = null;
        }
    }

    @Override
    public String toString() {
        // return String.format("{\"phone\":\"%s\", \"password\":\"%s\"}", getUsername(), getPassword());
        return super.toString();
    }
}
