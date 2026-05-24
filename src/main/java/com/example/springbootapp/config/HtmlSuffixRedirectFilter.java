package com.example.springbootapp.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
/**
 * {@code /app/.../*.html} 등 Falcon 정적 링크를 확장자 없는 MVC 경로로 리다이렉트합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HtmlSuffixRedirectFilter extends OncePerRequestFilter {
    private static final String[] PREFIXES = {"/app/", "/pages/", "/modules/"};
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.endsWith(".html") && startsWithAny(uri)) {
            String target = uri.substring(0, uri.length() - 5);
            String query = request.getQueryString();
            response.sendRedirect(target + (query != null ? "?" + query : ""));
            return;
        }
        filterChain.doFilter(request, response);
    }
    private static boolean startsWithAny(String uri) {
        for (String prefix : PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
