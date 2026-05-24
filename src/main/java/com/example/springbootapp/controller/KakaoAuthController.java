package com.example.springbootapp.controller;
import com.example.springbootapp.auth.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
@Controller
@RequestMapping("/auth/kakao")
public class KakaoAuthController {
    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;
    @Value("${kakao.authorize-url:https://kauth.kakao.com/oauth/authorize}")
    private String authorizeUrl;
    @Value("${kakao.token-url:https://kauth.kakao.com/oauth/token}")
    private String tokenUrl;
    @Value("${kakao.userinfo-url:https://kapi.kakao.com/v2/user/me}")
    private String userInfoUrl;
    @Value("${kakao.client-secret:}")
    private String clientSecret;
    private final SessionAuthService sessionAuthService;
    public KakaoAuthController(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }
    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        String safeAuthorizeUrl = requireProperty(authorizeUrl, "kakao.authorize-url");
        String safeClientId = requireProperty(clientId, "kakao.client-id");
        String safeRedirectUri = requireProperty(redirectUri, "kakao.redirect-uri");
        String state = UUID.randomUUID().toString().replace("-", "");
        request.getSession(true).setAttribute("kakao_oauth_state", state);
        String url = UriComponentsBuilder.fromHttpUrl(safeAuthorizeUrl)
                .queryParam("response_type", "code")
                .queryParam("client_id", safeClientId)
                .queryParam("redirect_uri", safeRedirectUri)
                .queryParam("state", state)
                .build()
                .toUriString();
        return "redirect:" + url;
    }
    @GetMapping("/callback")
    public String callback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletRequest request,
            Model model
    ) {
        Object sessionState = null;
        if (request.getSession(false) != null) {
            sessionState = request.getSession(false).getAttribute("kakao_oauth_state");
        }
        boolean stateValid = sessionState != null && sessionState.equals(state);
        model.addAttribute("stateValid", stateValid);
        if (!stateValid) {
            model.addAttribute("error", "state 검증 실패");
            return "pages/authentication/simple/kakao-callback";
        }
        String safeTokenUrl = requireProperty(tokenUrl, "kakao.token-url");
        String safeUserInfoUrl = requireProperty(userInfoUrl, "kakao.userinfo-url");
        String safeClientId = requireProperty(clientId, "kakao.client-id");
        String safeRedirectUri = requireProperty(redirectUri, "kakao.redirect-uri");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        List<MediaType> acceptJson = Objects.requireNonNull(List.of(MediaType.APPLICATION_JSON));
        tokenHeaders.setAccept(acceptJson);
        MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
        tokenBody.add("grant_type", "authorization_code");
        tokenBody.add("client_id", safeClientId);
        tokenBody.add("redirect_uri", safeRedirectUri);
        tokenBody.add("code", code);
        if (clientSecret != null && !clientSecret.isBlank()) {
            tokenBody.add("client_secret", clientSecret);
        }
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                safeTokenUrl,
                Objects.requireNonNull(HttpMethod.POST),
                tokenRequest,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> tokenMap = tokenResponse.getBody();
        if (tokenMap == null || tokenMap.get("access_token") == null) {
            model.addAttribute("error", "토큰 발급 실패");
            model.addAttribute("tokenResponse", tokenMap);
            return "pages/authentication/simple/kakao-callback";
        }
        Object tokenValue = Objects.requireNonNull(tokenMap.get("access_token"));
        String accessToken = requireProperty(tokenValue.toString(), "kakao.access-token");
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.setAccept(acceptJson);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                safeUserInfoUrl,
                Objects.requireNonNull(HttpMethod.POST),
                userRequest,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> kakaoUser = userResponse.getBody();
        Map<String, Object> responseSummary = new LinkedHashMap<>();
        responseSummary.put("token", tokenMap);
        responseSummary.put("user", kakaoUser);
        HttpSession session = request.getSession(true);
        sessionAuthService.loginFromKakao(session, kakaoUser, request);
        session.removeAttribute("kakao_oauth_state");
        model.addAttribute("code", code);
        model.addAttribute("response", responseSummary);
        return "pages/authentication/simple/kakao-callback";
    }
    private static @NonNull String requireProperty(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }
}
