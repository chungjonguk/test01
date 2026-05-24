package com.example.springbootapp.controller.page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * 화면 경로: {@code /dashboard/*}
 * <p>업종별 대시보드(분석·CRM·이커머스 등) 데모 화면을 렌더링합니다.</p>
 */
@Controller
public class DashboardController {
	/**
	 * @return out: Thymeleaf view path {@code dashboard/analytics}
	 */
	@GetMapping("/dashboard/analytics")
	public String dashboardAnalytics() {
		return "dashboard/analytics";
	}
	/**
	 * @return out: Thymeleaf view path {@code dashboard/crm}
	 */
	@GetMapping("/dashboard/crm")
	public String dashboardCrm() {
		return "dashboard/crm";
	}
	/**
	 * @return out: Thymeleaf view path {@code dashboard/e-commerce}
	 */
	@GetMapping("/dashboard/e-commerce")
	public String dashboardEcommerce() {
		return "dashboard/e-commerce";
	}
	/**
	 * @return out: Thymeleaf view path {@code dashboard/lms}
	 */
	@GetMapping("/dashboard/lms")
	public String dashboardLms() {
		return "dashboard/lms";
	}
	/**
	 * @return out: Thymeleaf view path {@code dashboard/project-management}
	 */
	@GetMapping("/dashboard/project-management")
	public String dashboardProjectManagement() {
		return "dashboard/project-management";
	}
	/**
	 * @return out: Thymeleaf view path {@code dashboard/saas}
	 */
	@GetMapping("/dashboard/saas")
	public String dashboardSaas() {
		return "dashboard/saas";
	}
}
