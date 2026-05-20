package com.example.springbootapp.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    // App - Calendar
    @GetMapping("/app/calendar")
    public String appCalendar(Model model) {
        model.addAttribute("title", "캘린더");
        model.addAttribute("loadCalendar", true);
        return "app/calendar";
    }

    // App - Chat
    @GetMapping("/app/chat")
    public String appChat(Model model) {
        model.addAttribute("title", "채팅");
        return "app/chat";
    }

    // App - Email
    @GetMapping("/app/email/inbox")
    public String appEmailInbox(Model model) {
        model.addAttribute("title", "받은편지함");
        return "app/email/inbox";
    }

    @GetMapping("/app/email/email-detail")
    public String appEmailDetail(Model model) {
        model.addAttribute("title", "이메일 상세");
        return "app/email/email-detail";
    }

    @GetMapping("/app/email/compose")
    public String appEmailCompose(Model model) {
        model.addAttribute("title", "작성");
        return "app/email/compose";
    }

    // App - Events
    @GetMapping({"/app/events/create-an-event", "/app/events/create-an-event.html"})
    public String appEventsCreateAnEvent(Model model) {
        model.addAttribute("title", "이벤트 만들기");
        return "app/events/create-an-event";
    }

    @GetMapping({"/app/events/event-detail", "/app/events/event-detail.html"})
    public String appEventsEventDetail(Model model) {
        model.addAttribute("title", "이벤트 상세");
        return "app/events/event-detail";
    }

    @GetMapping({"/app/events/event-list", "/app/events/event-list.html"})
    public String appEventsEventList(Model model) {
        model.addAttribute("title", "이벤트 목록");
        return "app/events/event-list";
    }

    /** 캘린더 상대 경로 오류로 잘못 열린 URL 보정 */
    @GetMapping("/app/calendar/app/events/event-detail.html")
    public String redirectCalendarEventDetail() {
        return "redirect:/app/events/event-detail";
    }

    @GetMapping("/app/calendar/app/events/create-an-event.html")
    public String redirectCalendarCreateEvent() {
        return "redirect:/app/events/create-an-event";
    }

    // App - E-commerce (원본 Falcon 링크는 *.html 접미사를 쓰는 경우가 많음)
    @GetMapping({"/app/e-commerce/product/product-list", "/app/e-commerce/product/product-list.html"})
    public String appEcommerceProductList(Model model) {
        model.addAttribute("title", "상품 목록");
        return "app/e-commerce/product/product-list";
    }

    @GetMapping({"/app/e-commerce/product/product-grid", "/app/e-commerce/product/product-grid.html"})
    public String appEcommerceProductGrid(Model model) {
        model.addAttribute("title", "상품 그리드");
        return "app/e-commerce/product/product-grid";
    }

    @GetMapping({"/app/e-commerce/product/product-details", "/app/e-commerce/product/product-details.html"})
    public String appEcommerceProductDetails(Model model) {
        model.addAttribute("title", "상품 상세");
        return "app/e-commerce/product/product-details";
    }

    @GetMapping({"/app/e-commerce/orders/order-list", "/app/e-commerce/orders/order-list.html"})
    public String appEcommerceOrderList(Model model) {
        model.addAttribute("title", "주문 목록");
        return "app/e-commerce/orders/order-list";
    }

    @GetMapping({"/app/e-commerce/orders/order-details", "/app/e-commerce/orders/order-details.html"})
    public String appEcommerceOrderDetails(Model model) {
        model.addAttribute("title", "주문 상세");
        return "app/e-commerce/orders/order-details";
    }

    @GetMapping({"/app/e-commerce/customers", "/app/e-commerce/customers.html"})
    public String appEcommerceCustomers(Model model) {
        model.addAttribute("title", "고객");
        return "app/e-commerce/customers";
    }

    @GetMapping({"/app/e-commerce/customer-details", "/app/e-commerce/customer-details.html"})
    public String appEcommerceCustomerDetails(Model model) {
        model.addAttribute("title", "고객 상세");
        return "app/e-commerce/customer-details";
    }

    @GetMapping({"/app/e-commerce/shopping-cart", "/app/e-commerce/shopping-cart.html"})
    public String appEcommerceShoppingCart(Model model) {
        model.addAttribute("title", "장바구니");
        return "app/e-commerce/shopping-cart";
    }

    @GetMapping({"/app/e-commerce/checkout", "/app/e-commerce/checkout.html"})
    public String appEcommerceCheckout(Model model) {
        model.addAttribute("title", "결제");
        return "app/e-commerce/checkout";
    }

    @GetMapping({"/app/e-commerce/billing", "/app/e-commerce/billing.html"})
    public String appEcommerceBilling(Model model) {
        model.addAttribute("title", "청구");
        return "app/e-commerce/billing";
    }

    @GetMapping({"/app/e-commerce/invoice", "/app/e-commerce/invoice.html"})
    public String appEcommerceInvoice(Model model) {
        model.addAttribute("title", "인보이스");
        model.addAttribute("loadInvoicePdf", true);
        return "app/e-commerce/invoice";
    }

    // App - E-learning
    @GetMapping("/app/e-learning/course/course-list")
    public String appElearningCourseList(Model model) {
        model.addAttribute("title", "강좌 목록");
        return "app/e-learning/course/course-list";
    }

    @GetMapping("/app/e-learning/course/course-grid")
    public String appElearningCourseGrid(Model model) {
        model.addAttribute("title", "강좌 그리드");
        return "app/e-learning/course/course-grid";
    }

    @GetMapping("/app/e-learning/course/course-details")
    public String appElearningCourseDetails(Model model) {
        model.addAttribute("title", "강좌 상세");
        return "app/e-learning/course/course-details";
    }

    @GetMapping("/app/e-learning/course/create-a-course")
    public String appElearningCreateACourse(Model model) {
        model.addAttribute("title", "강좌 만들기");
        return "app/e-learning/course/create-a-course";
    }

    @GetMapping("/app/e-learning/student-overview")
    public String appElearningStudentOverview(Model model) {
        model.addAttribute("title", "수강생 개요");
        return "app/e-learning/student-overview";
    }

    @GetMapping("/app/e-learning/trainer-profile")
    public String appElearningTrainerProfile(Model model) {
        model.addAttribute("title", "강사 프로필");
        return "app/e-learning/trainer-profile";
    }

    // App - Kanban
    @GetMapping("/app/kanban")
    public String appKanban(Model model) {
        model.addAttribute("title", "칸반");
        return "app/kanban";
    }

    // App - Social
    @GetMapping("/app/social/feed")
    public String appSocialFeed(Model model) {
        model.addAttribute("title", "피드");
        return "app/social/feed";
    }

    @GetMapping("/app/social/activity-log")
    public String appSocialActivityLog(Model model) {
        model.addAttribute("title", "활동 로그");
        return "app/social/activity-log";
    }

    @GetMapping("/app/social/notifications")
    public String appSocialNotifications(Model model) {
        model.addAttribute("title", "알림");
        return "app/social/notifications";
    }

    @GetMapping("/app/social/followers")
    public String appSocialFollowers(Model model) {
        model.addAttribute("title", "팔로워");
        return "app/social/followers";
    }
}
