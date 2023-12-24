package com.example.essentialcloud.userinfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class UserInfoService {
    private final ObjectMapper objectMapper;
    private final UserInfoRepository userInfoRepository;
    // inject the template as ValueOperations
    @Resource(name="redisTemplate")
    private ValueOperations<String, String> vOps;

    public UserInfoService(ObjectMapper objectMapper, UserInfoRepository userInfoRepository) {
        this.objectMapper = objectMapper;
        this.userInfoRepository = userInfoRepository;
    }

    public String retrieveUserInfoByAuthenticationId(String authenticationId) throws JsonProcessingException {
        String userInfo = vOps.get(authenticationId);
        if ( userInfo == null ) {
            Optional<UserInfo> dbUser = userInfoRepository.findUserInfoByAuthenticationId(authenticationId);
            if ( dbUser.isPresent()) {
                userInfo = objectMapper.writeValueAsString(dbUser.get());
            }
        }
        if ( userInfo == null ) {
            userInfo = "";
        }
        vOps.set(authenticationId, userInfo, Duration.ofMinutes(15));
        return userInfo;
    }
}
