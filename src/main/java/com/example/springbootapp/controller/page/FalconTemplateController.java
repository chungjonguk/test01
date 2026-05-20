package com.example.springbootapp.controller.page;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * 사이드바 modules 경로 중 ModulesController에 개별 매핑이 없는 템플릿을 렌더링합니다.
 */
@Controller
public class FalconTemplateController {

    private final ResourceLoader resourceLoader;

    public FalconTemplateController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/modules/widgets")
    public String redirectModulesWidgets() {
        return "redirect:/widgets";
    }

    @GetMapping("/modules/**")
    public String renderModulePage(HttpServletRequest request, Model model) {
        return renderTemplate(request, model, "modules");
    }

    private String renderTemplate(HttpServletRequest request, Model model, String prefix) {
        String uri = request.getRequestURI();
        if (uri.endsWith(".html")) {
            String target = uri.substring(0, uri.length() - 5);
            String query = request.getQueryString();
            return "redirect:" + target + (query != null ? "?" + query : "");
        }

        String viewName = uri.startsWith("/") ? uri.substring(1) : uri;
        if (!viewName.startsWith(prefix + "/") && !viewName.equals(prefix)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!templateExists(viewName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!model.containsAttribute("title")) {
            model.addAttribute("title", humanizeTitle(viewName));
        }

        return viewName;
    }

    private boolean templateExists(String viewName) {
        Resource resource = resourceLoader.getResource("classpath:/templates/" + viewName + ".html");
        return resource.exists();
    }

    private static String humanizeTitle(String viewName) {
        int slash = viewName.lastIndexOf('/');
        String leaf = slash >= 0 ? viewName.substring(slash + 1) : viewName;
        String spaced = leaf.replace('-', ' ').replace('_', ' ');
        if (spaced.isEmpty()) {
            return "Falcon";
        }
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }
}
