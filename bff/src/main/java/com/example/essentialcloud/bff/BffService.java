package com.example.essentialcloud.bff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Service
@Slf4j
public class BffService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> vOps;

    public BffService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Boolean getUserInfo(String authenticationId) {
        String userInfo = vOps.get(authenticationId);
        if (userInfo == null) {
            userInfo = webClient.get()
                    .uri(builder -> builder
                            .scheme("http")
                            .host("localhost")
                            .port(8100)
                            .path("/api/v1/userinfo")
                            .queryParam("authenticationId", authenticationId)
                            .build())
                    .attributes(clientRegistrationId("auth0-login"))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
        if (userInfo != null) {
            try {
                return objectMapper.readTree(userInfo).findValue("transferAdmin").asBoolean();
            } catch (JsonProcessingException e) {
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }
}
