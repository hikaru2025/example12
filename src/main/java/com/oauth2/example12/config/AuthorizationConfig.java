package com.oauth2.example12.config;

import com.oauth2.example12.enhancer.UserTokenEnhancer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;

/**
 * @Auther: xm
 * @Date: 2020/12/31/15:25
 * @Description:
 */
@Configuration
@EnableAuthorizationServer
@AllArgsConstructor
public class AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    public PasswordEncoder passwordEncoder;

    public UserDetailsService kiteUserDetailsService;

    private AuthenticationManager authenticationManager;

    private TokenStore redisTokenStore;

    private WebResponseExceptionTranslator webResponseExceptionTranslator;

    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        DefaultTokenServices tokenServices = new DefaultTokenServices();
        //token持久化容器
        tokenServices.setTokenStore(redisTokenStore);
        //客户端信息
        tokenServices.setClientDetailsService(endpoints.getClientDetailsService());
        //自定义token生成
        tokenServices.setTokenEnhancer(tokenEnhancer());
        //access_token 的有效时长 (秒), 默认 12 小时
        tokenServices.setAccessTokenValiditySeconds(2);
        //refresh_token 的有效时长 (秒), 默认 30 天
        tokenServices.setRefreshTokenValiditySeconds(4);
        //是否支持refresh_token，默认false
        tokenServices.setSupportRefreshToken(true);
        //是否复用refresh_token,默认为true(如果为false,则每次请求刷新都会删除旧的refresh_token,创建新的refresh_token)
        tokenServices.setReuseRefreshToken(false);
        /**
         * redis token 方式
         */
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(kiteUserDetailsService)
                .tokenServices(tokenServices)
                .exceptionTranslator(webResponseExceptionTranslator);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("order-client")
                .secret(passwordEncoder.encode("order-secret-8888"))
                .authorizedGrantTypes("refresh_token", "authorization_code", "password")
                .scopes("all")
                .and()
                .withClient("user-client")
                .secret(passwordEncoder.encode("user-secret-8888"))
                .authorizedGrantTypes("refresh_token", "authorization_code", "password")
                .scopes("all");
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients()
                .checkTokenAccess("permitAll()")
                .tokenKeyAccess("isAuthenticated()");
    }

    @Bean
    public TokenEnhancer tokenEnhancer(){
        return new UserTokenEnhancer();
    }

}
