package com.example.springbootapp.controller.page;
import com.example.springbootapp.auth.LoginSession;
import com.example.springbootapp.auth.SessionAuthService;
import com.example.springbootapp.domain.User;
import com.example.springbootapp.domain.UserAccessLog;
import com.example.springbootapp.mapper.UserMapper;
import com.example.springbootapp.service.UserAccessLogService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.example.springbootapp.util.AppDateTimeFormats;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * 화면 경로: {@code /app/*}
 * <p>캘린더·이메일·이커머스·e-learning·소셜 등 앱 데모 화면을 렌더링합니다.</p>
 */
@Controller
public class AppController {
	private final SessionAuthService sessionAuthService;
	private final UserMapper userMapper;
	private final UserAccessLogService userAccessLogService;
	public AppController(
			SessionAuthService sessionAuthService,
			UserMapper userMapper,
			UserAccessLogService userAccessLogService) {
		this.sessionAuthService = sessionAuthService;
		this.userMapper = userMapper;
		this.userAccessLogService = userAccessLogService;
	}
	/**
	 * @return out: Thymeleaf view path {@code app/calendar}
	 */
	@GetMapping("/app/calendar")
	public String appCalendar() {
		return "app/calendar";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/chat}
	 */
	@GetMapping("/app/chat")
	public String appChat() {
		return "app/chat";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/email/inbox}
	 */
	@GetMapping("/app/email/inbox")
	public String appEmailInbox() {
		return "app/email/inbox";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/email/email-detail}
	 */
	@GetMapping("/app/email/email-detail")
	public String appEmailDetail() {
		return "app/email/email-detail";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/email/compose}
	 */
	@GetMapping("/app/email/compose")
	public String appEmailCompose() {
		return "app/email/compose";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/events/create-an-event}
	 */
	@GetMapping({"/app/events/create-an-event", "/app/events/create-an-event.html"})
	public String appEventsCreateAnEvent() {
		return "app/events/create-an-event";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/events/event-detail}
	 */
	@GetMapping({"/app/events/event-detail", "/app/events/event-detail.html"})
	public String appEventsEventDetail() {
		return "app/events/event-detail";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/events/event-list}
	 */
	@GetMapping({"/app/events/event-list", "/app/events/event-list.html"})
	public String appEventsEventList() {
		return "app/events/event-list";
	}
	/**
	 * @return out: redirect {@code /app/events/event-detail}
	 */
	@GetMapping("/app/calendar/app/events/event-detail.html")
	public String redirectCalendarEventDetail() {
		return "redirect:/app/events/event-detail";
	}
	/**
	 * @return out: redirect {@code /app/events/create-an-event}
	 */
	@GetMapping("/app/calendar/app/events/create-an-event.html")
	public String redirectCalendarCreateEvent() {
		return "redirect:/app/events/create-an-event";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/product/product-list}
	 */
	@GetMapping({"/app/e-commerce/product/product-list", "/app/e-commerce/product/product-list.html"})
	public String appEcommerceProductList() {
		return "app/e-commerce/product/product-list";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/product/product-grid}
	 */
	@GetMapping({"/app/e-commerce/product/product-grid", "/app/e-commerce/product/product-grid.html"})
	public String appEcommerceProductGrid() {
		return "app/e-commerce/product/product-grid";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/orders/order-list}
	 */
	@GetMapping({"/app/e-commerce/orders/order-list", "/app/e-commerce/orders/order-list.html"})
	public String appEcommerceOrderList() {
		return "app/e-commerce/orders/order-list";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/orders/order-details}
	 */
	@GetMapping({"/app/e-commerce/orders/order-details", "/app/e-commerce/orders/order-details.html"})
	public String appEcommerceOrderDetails() {
		return "app/e-commerce/orders/order-details";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/customers}
	 */
	@GetMapping({"/app/e-commerce/customers", "/app/e-commerce/customers.html"})
	public String appEcommerceCustomers() {
		return "app/e-commerce/customers";
	}
	/**
	 * 로그인 세션·DB 사용자 정보로 고객 상세 화면을 채웁니다.
	 *
	 * @param session HTTP 세션
	 * @param model   Thymeleaf 모델 (profileUser, customerAccessLogs 등)
	 * @return out: Thymeleaf view path {@code app/e-commerce/customer-details}
	 */
	@GetMapping({"/app/e-commerce/customer-details", "/app/e-commerce/customer-details.html"})
	public String appEcommerceCustomerDetails(HttpSession session, Model model) {
		model.addAttribute("title", "고객 상세");
		LoginSession login = sessionAuthService.getLoginSession(session);
		if (login == null) {
			model.addAttribute("customerSessionMissing", true);
			return "app/e-commerce/customer-details";
		}
		Optional<User> profile = userMapper.findById(login.getUserId());
		profile.ifPresent(user -> model.addAttribute("profileUser", user));
		model.addAttribute("customerDisplayName", resolveDisplayName(login, profile.orElse(null)));
		model.addAttribute("customerEmail", resolveEmail(login, profile.orElse(null)));
		model.addAttribute("customerCreatedText", formatDateTime(resolveCreatedAt(profile.orElse(null))));
		model.addAttribute("customerLoginAtText", formatDateTimeLong(login.getLoginAt()));
		model.addAttribute("customerLoginType", login.getLoginType());
		model.addAttribute("customerAddressLine", formatAddress(profile.orElse(null)));
		model.addAttribute("customerZipcode", profile.map(User::getZipcode).orElse(null));
		model.addAttribute("customerSex", formatSex(profile.map(User::getSex).orElse(null)));
		model.addAttribute("customerInvoicePrefix", invoicePrefix(login.getUserId()));
		List<UserAccessLog> logs = userAccessLogService.findRecentByUserId(login.getUserId(), 8);
		model.addAttribute("customerAccessLogs", logs);
		return "app/e-commerce/customer-details";
	}
	private static String resolveDisplayName(LoginSession login, User profile) {
		if (profile != null && profile.getName() != null && !profile.getName().isBlank()) {
			return profile.getName();
		}
		return login.getUserName() != null ? login.getUserName() : login.getUserId();
	}
	private static String resolveEmail(LoginSession login, User profile) {
		if (profile != null && profile.getEmail() != null && !profile.getEmail().isBlank()) {
			return profile.getEmail();
		}
		return login.getEmail();
	}
	private static LocalDateTime resolveCreatedAt(User profile) {
		return profile != null ? profile.getRegDt() : null;
	}
	private static String formatDateTime(LocalDateTime value) {
		String formatted = AppDateTimeFormats.formatDateTime(value);
		return formatted != null ? formatted : "-";
	}
	private static String formatDateTimeLong(LocalDateTime value) {
		return formatDateTime(value);
	}
	private static String formatAddress(User profile) {
		if (profile == null) {
			return null;
		}
		String base = profile.getAddress();
		String detail = profile.getAddressDetail();
		if ((base == null || base.isBlank()) && (detail == null || detail.isBlank())) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (base != null && !base.isBlank()) {
			sb.append(base.trim());
		}
		if (detail != null && !detail.isBlank()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(detail.trim());
		}
		return sb.toString();
	}
	private static String formatSex(String sex) {
		if (sex == null || sex.isBlank()) {
			return null;
		}
		return switch (sex.trim().toUpperCase(Locale.ROOT)) {
			case "M", "MALE", "1" -> "남";
			case "F", "FEMALE", "2" -> "여";
			default -> sex;
		};
	}
	private static String invoicePrefix(String userId) {
		if (userId == null || userId.length() < 4) {
			return userId != null ? userId.toUpperCase(Locale.ROOT) : "-";
		}
		return userId.substring(0, 4).toUpperCase(Locale.ROOT);
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/shopping-cart}
	 */
	@GetMapping({"/app/e-commerce/shopping-cart", "/app/e-commerce/shopping-cart.html"})
	public String appEcommerceShoppingCart() {
		return "app/e-commerce/shopping-cart";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/checkout}
	 */
	@GetMapping({"/app/e-commerce/checkout", "/app/e-commerce/checkout.html"})
	public String appEcommerceCheckout() {
		return "app/e-commerce/checkout";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/billing}
	 */
	@GetMapping({"/app/e-commerce/billing", "/app/e-commerce/billing.html"})
	public String appEcommerceBilling() {
		return "app/e-commerce/billing";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-commerce/invoice}
	 */
	@GetMapping({"/app/e-commerce/invoice", "/app/e-commerce/invoice.html"})
	public String appEcommerceInvoice() {
		return "app/e-commerce/invoice";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-learning/course/course-list}
	 */
	@GetMapping("/app/e-learning/course/course-list")
	public String appElearningCourseList() {
		return "app/e-learning/course/course-list";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-learning/course/course-grid}
	 */
	@GetMapping("/app/e-learning/course/course-grid")
	public String appElearningCourseGrid() {
		return "app/e-learning/course/course-grid";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-learning/course/course-details}
	 */
	@GetMapping("/app/e-learning/course/course-details")
	public String appElearningCourseDetails() {
		return "app/e-learning/course/course-details";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-learning/course/create-a-course}
	 */
	@GetMapping("/app/e-learning/course/create-a-course")
	public String appElearningCreateACourse() {
		return "app/e-learning/course/create-a-course";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-learning/student-overview}
	 */
	@GetMapping("/app/e-learning/student-overview")
	public String appElearningStudentOverview() {
		return "app/e-learning/student-overview";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/e-learning/trainer-profile}
	 */
	@GetMapping("/app/e-learning/trainer-profile")
	public String appElearningTrainerProfile() {
		return "app/e-learning/trainer-profile";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/kanban}
	 */
	@GetMapping("/app/kanban")
	public String appKanban() {
		return "app/kanban";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/social/feed}
	 */
	@GetMapping("/app/social/feed")
	public String appSocialFeed() {
		return "app/social/feed";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/social/activity-log}
	 */
	@GetMapping("/app/social/activity-log")
	public String appSocialActivityLog() {
		return "app/social/activity-log";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/social/notifications}
	 */
	@GetMapping("/app/social/notifications")
	public String appSocialNotifications() {
		return "app/social/notifications";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/social/notification-list}
	 */
	@GetMapping("/app/social/notification-list")
	public String appSocialNotificationList() {
		return "app/social/notification-list";
	}
	/**
	 * @return out: Thymeleaf view path {@code app/social/followers}
	 */
	@GetMapping("/app/social/followers")
	public String appSocialFollowers() {
		return "app/social/followers";
	}
}
