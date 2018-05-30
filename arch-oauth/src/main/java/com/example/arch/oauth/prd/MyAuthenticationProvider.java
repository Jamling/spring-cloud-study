package com.example.arch.oauth.prd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("prd")
public class MyAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsServiceImpl userService;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName().trim();
        String password = ((String)authentication.getCredentials()).trim();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new BadCredentialsException("Login failed! Please try again.");
        }

        UserDetails user;
        try {
            user = userService.loadUserByUsernamePassword(username, encoder.encode(password));
        } catch (Exception e) {
            throw new BadCredentialsException("Please enter a valid username and password.");
        }

        // 不需要了
//        if (!encoder.matches(password, user.getPassword().trim())) {
//            throw new BadCredentialsException("Please enter a valid username and password.");
//        }

        if (!user.isEnabled()) {
            throw new DisabledException("Please enter a valid username and password.");
        }

        if (!user.isAccountNonLocked()) {
            throw new LockedException("Account locked. ");
        }

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<GrantedAuthority> permlist = new ArrayList<GrantedAuthority>(authorities);

        return new UsernamePasswordAuthenticationToken(user, password, permlist);
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
