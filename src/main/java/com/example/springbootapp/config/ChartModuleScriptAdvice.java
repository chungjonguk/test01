package com.example.springbootapp.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 차트 모듈 페이지에서 필요한 스크립트 플래그를 URI 기준으로 설정합니다.
 */
@ControllerAdvice
public class ChartModuleScriptAdvice {

    @ModelAttribute
    public void chartModuleScripts(HttpServletRequest request, Model model) {
        if (request == null) {
            return;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return;
        }
        if (uri.contains("/modules/charts/echarts")) {
            model.addAttribute("loadEchartsExamples", true);
        }
        if (uri.contains("/modules/charts/d3js")) {
            model.addAttribute("loadD3", true);
        }
        if (uri.contains("/modules/charts/chartjs")) {
            model.addAttribute("loadChartJs", true);
        }
    }
}
