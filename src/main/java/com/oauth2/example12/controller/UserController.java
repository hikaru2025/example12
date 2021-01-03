package com.oauth2.example12.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * @Auther: xm
 * @Date: 2020/12/31/17:05
 * @Description:
 */
@RestController
public class UserController {
    @Autowired
    @Qualifier("consumerTokenServices")
    private ConsumerTokenServices consumerTokenServices;

    @GetMapping("/get")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Object get(Authentication authentication){
         authentication = SecurityContextHolder.getContext().getAuthentication();
//        authentication.getCredentials();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)authentication.getDetails();
        String token = details.getTokenValue();
        return token;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        resource.setClientId("order-client");
        resource.setClientSecret("order-secret-8888");
        resource.setGrantType("password");
        resource.setAccessTokenUri("http://127.0.0.1:8080/oauth/token");
        resource.setUsername(username);
        resource.setPassword(password);
        resource.setScope(Arrays.asList("all"));
        OAuth2RestTemplate template = new OAuth2RestTemplate(resource);
        ResourceOwnerPasswordAccessTokenProvider provider = new ResourceOwnerPasswordAccessTokenProvider();
        template.setAccessTokenProvider(provider);

        OAuth2AccessToken accessToken = template.getAccessToken();
        if (null != accessToken) {
            return JSON.toJSONString(accessToken);
        }
        return "";
    }

    @GetMapping("/kkk")
    public String logout(Authentication authentication) {

        if (authentication != null) {
            OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)authentication.getDetails();
            String token = details.getTokenValue();
            consumerTokenServices.revokeToken(token);
            return "成功退出";
        }
        return "";
    }
}
