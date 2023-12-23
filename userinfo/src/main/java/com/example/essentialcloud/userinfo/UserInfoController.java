package com.example.essentialcloud.userinfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class UserInfoController {
    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping(value = "/userinfo")
    public String getUserInfo(@RequestParam String authenticationId) throws JsonProcessingException {
        return userInfoService.retrieveUserInfoByAuthenticationId(authenticationId);
    }

}
