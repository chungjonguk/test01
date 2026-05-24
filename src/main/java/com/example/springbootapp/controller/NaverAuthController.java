package com.example.springbootapp.controller;
import com.example.springbootapp.auth.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
@Controller
@RequestMapping("/auth/naver")
public class NaverAuthController {
    @Value("${naver.client-id}")
    private String clientId;
    @Value("${naver.client-secret}")
    private String clientSecret;
    @Value("${naver.redirect-uri}")
    private String redirectUri;
    @Value("${naver.authorize-url:https://nid.naver.com/oauth2.0/authorize}")
    private String authorizeUrl;
    @Value("${naver.token-url:https://nid.naver.com/oauth2.0/token}")
    private String tokenUrl;
    @Value("${naver.userinfo-url:https://openapi.naver.com/v1/nid/me}")
    private String userInfoUrl;
    private final SessionAuthService sessionAuthService;
    public NaverAuthController(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }
    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        String safeAuthorizeUrl = requireProperty(authorizeUrl, "naver.authorize-url");
        String safeClientId = requireProperty(clientId, "naver.client-id");
        String safeRedirectUri = requireProperty(redirectUri, "naver.redirect-uri");
        String state = UUID.randomUUID().toString().replace("-", "");
        request.getSession(true).setAttribute("naver_oauth_state", state);
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
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletRequest request,
            Model model
    ) {
        if (error != null && !error.isBlank()) {
            model.addAttribute("stateValid", false);
            model.addAttribute("error", error + (errorDescription != null ? ": " + errorDescription : ""));
            return "pages/authentication/simple/naver-callback";
        }
        Object sessionState = null;
        if (request.getSession(false) != null) {
            sessionState = request.getSession(false).getAttribute("naver_oauth_state");
        }
        boolean stateValid = sessionState != null && sessionState.equals(state);
        model.addAttribute("stateValid", stateValid);
        if (!stateValid) {
            model.addAttribute("error", "state 검증 실패");
            return "pages/authentication/simple/naver-callback";
        }
        if (code == null || code.isBlank()) {
            model.addAttribute("error", "인가 코드 없음");
            return "pages/authentication/simple/naver-callback";
        }
        String safeTokenUrl = requireProperty(tokenUrl, "naver.token-url");
        String safeUserInfoUrl = requireProperty(userInfoUrl, "naver.userinfo-url");
        String safeClientId = requireProperty(clientId, "naver.client-id");
        String safeClientSecret = requireProperty(clientSecret, "naver.client-secret");
        String safeRedirectUri = requireProperty(redirectUri, "naver.redirect-uri");
        String safeState = requireProperty(state, "naver.oauth-state");
        RestTemplate restTemplate = new RestTemplate();
        List<MediaType> acceptJson = Objects.requireNonNull(List.of(MediaType.APPLICATION_JSON));
        String tokenRequestUrl = UriComponentsBuilder.fromHttpUrl(safeTokenUrl)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", safeClientId)
                .queryParam("client_secret", safeClientSecret)
                .queryParam("redirect_uri", safeRedirectUri)
                .queryParam("code", code)
                .queryParam("state", safeState)
                .build()
                .toUriString();
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                tokenRequestUrl,
                Objects.requireNonNull(HttpMethod.GET),
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> tokenMap = tokenResponse.getBody();
        if (tokenMap == null || tokenMap.get("access_token") == null) {
            model.addAttribute("error", "토큰 발급 실패");
            model.addAttribute("tokenResponse", tokenMap);
            return "pages/authentication/simple/naver-callback";
        }
        String accessToken = requireProperty(Objects.requireNonNull(tokenMap.get("access_token")).toString(), "naver.access-token");
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.setAccept(acceptJson);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                safeUserInfoUrl,
                Objects.requireNonNull(HttpMethod.GET),
                userRequest,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> naverBody = userResponse.getBody();
        if (naverBody == null || !"00".equals(String.valueOf(naverBody.get("resultcode")))) {
            model.addAttribute("error", "사용자 정보 조회 실패");
            model.addAttribute("userResponse", naverBody);
            return "pages/authentication/simple/naver-callback";
        }
        HttpSession session = request.getSession(true);
        sessionAuthService.loginFromNaver(session, naverBody, request);
        session.removeAttribute("naver_oauth_state");
        return "redirect:/";
    }
    private static @NonNull String requireProperty(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }
}
