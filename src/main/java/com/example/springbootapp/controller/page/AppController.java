package com.example.springbootapp.controller.page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * 화면 경로: {@code /app/*}
 * <p>캘린더·이메일·이커머스·e-learning·소셜 등 앱 데모 화면을 렌더링합니다.</p>
 */
@Controller
public class AppController {
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
	 * @return out: Thymeleaf view path {@code app/e-commerce/customer-details}
	 */
	@GetMapping({"/app/e-commerce/customer-details", "/app/e-commerce/customer-details.html"})
	public String appEcommerceCustomerDetails() {
		return "app/e-commerce/customer-details";
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
	 * @return out: Thymeleaf view path {@code app/social/followers}
	 */
	@GetMapping("/app/social/followers")
	public String appSocialFollowers() {
		return "app/social/followers";
	}
}
