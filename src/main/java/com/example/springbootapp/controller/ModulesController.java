package com.example.springbootapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModulesController {

    // Modules - Forms
    @GetMapping("/modules/forms/basic/form-control")
    public String modulesFormsBasicFormControl(Model model) {
        model.addAttribute("title", "폼 컨트롤");
        return "modules/forms/basic/form-control";
    }

    @GetMapping("/modules/forms/basic/input-group")
    public String modulesFormsBasicInputGroup(Model model) {
        model.addAttribute("title", "입력 그룹");
        return "modules/forms/basic/input-group";
    }

    @GetMapping("/modules/forms/basic/select")
    public String modulesFormsBasicSelect(Model model) {
        model.addAttribute("title", "선택");
        return "modules/forms/basic/select";
    }

    @GetMapping("/modules/forms/basic/checks")
    public String modulesFormsBasicChecks(Model model) {
        model.addAttribute("title", "체크");
        return "modules/forms/basic/checks";
    }

    @GetMapping("/modules/forms/basic/range")
    public String modulesFormsBasicRange(Model model) {
        model.addAttribute("title", "범위");
        return "modules/forms/basic/range";
    }

    @GetMapping("/modules/forms/basic/layout")
    public String modulesFormsBasicLayout(Model model) {
        model.addAttribute("title", "레이아웃");
        return "modules/forms/basic/layout";
    }

    @GetMapping("/modules/forms/advance/advance-select")
    public String modulesFormsAdvanceSelect(Model model) {
        model.addAttribute("title", "고급 선택");
        return "modules/forms/advance/advance-select";
    }

    @GetMapping("/modules/forms/advance/date-picker")
    public String modulesFormsAdvanceDatePicker(Model model) {
        model.addAttribute("title", "날짜 선택");
        return "modules/forms/advance/date-picker";
    }

    @GetMapping("/modules/forms/advance/editor")
    public String modulesFormsAdvanceEditor(Model model) {
        model.addAttribute("title", "에디터");
        return "modules/forms/advance/editor";
    }

    @GetMapping("/modules/forms/advance/emoji-button")
    public String modulesFormsAdvanceEmojiButton(Model model) {
        model.addAttribute("title", "이모지 버튼");
        return "modules/forms/advance/emoji-button";
    }

    @GetMapping("/modules/forms/advance/file-uploader")
    public String modulesFormsAdvanceFileUploader(Model model) {
        model.addAttribute("title", "파일 업로드");
        model.addAttribute("loadDropzone", true);
        return "modules/forms/advance/file-uploader";
    }

    @GetMapping("/modules/forms/advance/rating")
    public String modulesFormsAdvanceRating(Model model) {
        model.addAttribute("title", "평점");
        return "modules/forms/advance/rating";
    }

    @GetMapping("/modules/forms/floating-labels")
    public String modulesFormsFloatingLabels(Model model) {
        model.addAttribute("title", "플로팅 라벨");
        return "modules/forms/floating-labels";
    }

    @GetMapping("/modules/forms/wizard")
    public String modulesFormsWizard(Model model) {
        model.addAttribute("title", "마법사");
        return "modules/forms/wizard";
    }

    @GetMapping("/modules/forms/validation")
    public String modulesFormsValidation(Model model) {
        model.addAttribute("title", "유효성 검사");
        return "modules/forms/validation";
    }

    // Modules - Tables
    @GetMapping("/modules/tables/basic-tables")
    public String modulesTablesBasicTables(Model model) {
        model.addAttribute("title", "기본 테이블");
        return "modules/tables/basic-tables";
    }

    @GetMapping("/modules/tables/advance-tables")
    public String modulesTablesAdvanceTables(Model model) {
        model.addAttribute("title", "고급 테이블");
        return "modules/tables/advance-tables";
    }

    @GetMapping("/modules/tables/bulk-select")
    public String modulesTablesBulkSelect(Model model) {
        model.addAttribute("title", "일괄 선택");
        return "modules/tables/bulk-select";
    }

    // Modules - Charts
    @GetMapping("/modules/charts/chartjs")
    public String modulesChartsChartjs(Model model) {
        model.addAttribute("title", "Chart.js");
        return "modules/charts/chartjs";
    }

    @GetMapping("/modules/charts/d3js")
    public String modulesChartsD3js(Model model) {
        model.addAttribute("title", "D3.js");
        return "modules/charts/d3js";
    }

    @GetMapping("/modules/charts/echarts/line-charts")
    public String modulesChartsEchartsLineCharts(Model model) {
        model.addAttribute("title", "선형 차트");
        return "modules/charts/echarts/line-charts";
    }

    @GetMapping("/modules/charts/echarts/bar-charts")
    public String modulesChartsEchartsBarCharts(Model model) {
        model.addAttribute("title", "막대 차트");
        return "modules/charts/echarts/bar-charts";
    }

    @GetMapping("/modules/charts/echarts/candlestick-charts")
    public String modulesChartsEchartsCandlestickCharts(Model model) {
        model.addAttribute("title", "캔들스틱 차트");
        return "modules/charts/echarts/candlestick-charts";
    }

    @GetMapping("/modules/charts/echarts/geo-map")
    public String modulesChartsEchartsGeoMap(Model model) {
        model.addAttribute("title", "지도");
        return "modules/charts/echarts/geo-map";
    }

    @GetMapping("/modules/charts/echarts/scatter-charts")
    public String modulesChartsEchartsScatterCharts(Model model) {
        model.addAttribute("title", "산점도 차트");
        return "modules/charts/echarts/scatter-charts";
    }

    @GetMapping("/modules/charts/echarts/pie-charts")
    public String modulesChartsEchartsPieCharts(Model model) {
        model.addAttribute("title", "원형 차트");
        return "modules/charts/echarts/pie-charts";
    }

    @GetMapping("/modules/charts/echarts/radar-charts")
    public String modulesChartsEchartsRadarCharts(Model model) {
        model.addAttribute("title", "레이더 차트");
        return "modules/charts/echarts/radar-charts";
    }

    @GetMapping("/modules/charts/echarts/heatmap-charts")
    public String modulesChartsEchartsHeatmapCharts(Model model) {
        model.addAttribute("title", "히트맵 차트");
        return "modules/charts/echarts/heatmap-charts";
    }

    @GetMapping("/modules/charts/echarts/how-to-use")
    public String modulesChartsEchartsHowToUse(Model model) {
        model.addAttribute("title", "사용 방법");
        return "modules/charts/echarts/how-to-use";
    }

    // Modules - Icons
    @GetMapping("/modules/icons/font-awesome")
    public String modulesIconsFontAwesome(Model model) {
        model.addAttribute("title", "Font Awesome");
        return "modules/icons/font-awesome";
    }

    @GetMapping("/modules/icons/bootstrap-icons")
    public String modulesIconsBootstrapIcons(Model model) {
        model.addAttribute("title", "Bootstrap 아이콘");
        return "modules/icons/bootstrap-icons";
    }

    @GetMapping("/modules/icons/feather")
    public String modulesIconsFeather(Model model) {
        model.addAttribute("title", "Feather");
        return "modules/icons/feather";
    }

    @GetMapping("/modules/icons/material-icons")
    public String modulesIconsMaterialIcons(Model model) {
        model.addAttribute("title", "Material 아이콘");
        return "modules/icons/material-icons";
    }

    // Modules - Maps
    @GetMapping("/modules/maps/google-map")
    public String modulesMapsGoogleMap(Model model) {
        model.addAttribute("title", "Google 지도");
        return "modules/maps/google-map";
    }

    @GetMapping("/modules/maps/leaflet-map")
    public String modulesMapsLeafletMap(Model model) {
        model.addAttribute("title", "Leaflet 지도");
        return "modules/maps/leaflet-map";
    }

    // Modules - Components
    @GetMapping("/modules/components/alerts")
    public String modulesComponentsAlerts(Model model) {
        model.addAttribute("title", "알림");
        return "modules/components/alerts";
    }

    @GetMapping("/modules/components/buttons")
    public String modulesComponentsButtons(Model model) {
        model.addAttribute("title", "버튼");
        return "modules/components/buttons";
    }

    // Modules - Utilities
    @GetMapping("/modules/utilities/colors")
    public String modulesUtilitiesColors(Model model) {
        model.addAttribute("title", "색상");
        return "modules/utilities/colors";
    }

    // Modules - Widgets
    @GetMapping("/modules/widgets")
    public String modulesWidgets(Model model) {
        model.addAttribute("title", "위젯");
        return "modules/widgets";
    }
}
