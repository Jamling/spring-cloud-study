package com.example.arch.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    AuthenticationManager authenticationManager;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // @formatter:off
        clients.inMemory()
            .withClient("client1")
                .authorities("client", "USER")
                .authorizedGrantTypes(
                    "authorization_code", "implicit", "client_credentials",  "password", "refresh_token")
                .secret("{noop}123456")
                .scopes("read")
                .redirectUris("http://localhost:1111/callback", "http://localhost:1111/callback2")
                .autoApprove(true)
                .and()
            .withClient("client2")
                .authorities("client", "ADMIN")
                .authorizedGrantTypes(IAuthConstants.AUTH_CLIENT)
                .secret("{my}5f1d7a84db00d2fce00b31a7fc73224f") // password为123456
                .scopes("write")
                .autoApprove(true)
                ;
        // @formatter:on

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // @formatter:off
        endpoints
            .authenticationManager(authenticationManager)
            // 允许GET，不然{"error":"method_not_allowed","error_description":"Request method &#39;GET&#39; not supported"}
            .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST)
            ;
        // @formatter:on
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.allowFormAuthenticationForClients();
    }

}
