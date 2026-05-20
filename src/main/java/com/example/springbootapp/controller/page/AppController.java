package com.example.springbootapp.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    @GetMapping("/app/calendar")
    public String appCalendar() {
        return "app/calendar";
    }

    @GetMapping("/app/chat")
    public String appChat() {
        return "app/chat";
    }

    @GetMapping("/app/email/inbox")
    public String appEmailInbox() {
        return "app/email/inbox";
    }

    @GetMapping("/app/email/email-detail")
    public String appEmailDetail() {
        return "app/email/email-detail";
    }

    @GetMapping("/app/email/compose")
    public String appEmailCompose() {
        return "app/email/compose";
    }

    @GetMapping({"/app/events/create-an-event", "/app/events/create-an-event.html"})
    public String appEventsCreateAnEvent() {
        return "app/events/create-an-event";
    }

    @GetMapping({"/app/events/event-detail", "/app/events/event-detail.html"})
    public String appEventsEventDetail() {
        return "app/events/event-detail";
    }

    @GetMapping({"/app/events/event-list", "/app/events/event-list.html"})
    public String appEventsEventList() {
        return "app/events/event-list";
    }

    @GetMapping("/app/calendar/app/events/event-detail.html")
    public String redirectCalendarEventDetail() {
        return "redirect:/app/events/event-detail";
    }

    @GetMapping("/app/calendar/app/events/create-an-event.html")
    public String redirectCalendarCreateEvent() {
        return "redirect:/app/events/create-an-event";
    }

    @GetMapping({"/app/e-commerce/product/product-list", "/app/e-commerce/product/product-list.html"})
    public String appEcommerceProductList() {
        return "app/e-commerce/product/product-list";
    }

    @GetMapping({"/app/e-commerce/product/product-grid", "/app/e-commerce/product/product-grid.html"})
    public String appEcommerceProductGrid() {
        return "app/e-commerce/product/product-grid";
    }

    @GetMapping({"/app/e-commerce/product/product-details", "/app/e-commerce/product/product-details.html"})
    public String appEcommerceProductDetails() {
        return "app/e-commerce/product/product-details";
    }

    @GetMapping({"/app/e-commerce/orders/order-list", "/app/e-commerce/orders/order-list.html"})
    public String appEcommerceOrderList() {
        return "app/e-commerce/orders/order-list";
    }

    @GetMapping({"/app/e-commerce/orders/order-details", "/app/e-commerce/orders/order-details.html"})
    public String appEcommerceOrderDetails() {
        return "app/e-commerce/orders/order-details";
    }

    @GetMapping({"/app/e-commerce/customers", "/app/e-commerce/customers.html"})
    public String appEcommerceCustomers() {
        return "app/e-commerce/customers";
    }

    @GetMapping({"/app/e-commerce/customer-details", "/app/e-commerce/customer-details.html"})
    public String appEcommerceCustomerDetails() {
        return "app/e-commerce/customer-details";
    }

    @GetMapping({"/app/e-commerce/shopping-cart", "/app/e-commerce/shopping-cart.html"})
    public String appEcommerceShoppingCart() {
        return "app/e-commerce/shopping-cart";
    }

    @GetMapping({"/app/e-commerce/checkout", "/app/e-commerce/checkout.html"})
    public String appEcommerceCheckout() {
        return "app/e-commerce/checkout";
    }

    @GetMapping({"/app/e-commerce/billing", "/app/e-commerce/billing.html"})
    public String appEcommerceBilling() {
        return "app/e-commerce/billing";
    }

    @GetMapping({"/app/e-commerce/invoice", "/app/e-commerce/invoice.html"})
    public String appEcommerceInvoice() {
        return "app/e-commerce/invoice";
    }

    @GetMapping("/app/e-learning/course/course-list")
    public String appElearningCourseList() {
        return "app/e-learning/course/course-list";
    }

    @GetMapping("/app/e-learning/course/course-grid")
    public String appElearningCourseGrid() {
        return "app/e-learning/course/course-grid";
    }

    @GetMapping("/app/e-learning/course/course-details")
    public String appElearningCourseDetails() {
        return "app/e-learning/course/course-details";
    }

    @GetMapping("/app/e-learning/course/create-a-course")
    public String appElearningCreateACourse() {
        return "app/e-learning/course/create-a-course";
    }

    @GetMapping("/app/e-learning/student-overview")
    public String appElearningStudentOverview() {
        return "app/e-learning/student-overview";
    }

    @GetMapping("/app/e-learning/trainer-profile")
    public String appElearningTrainerProfile() {
        return "app/e-learning/trainer-profile";
    }

    @GetMapping("/app/kanban")
    public String appKanban() {
        return "app/kanban";
    }

    @GetMapping("/app/social/feed")
    public String appSocialFeed() {
        return "app/social/feed";
    }

    @GetMapping("/app/social/activity-log")
    public String appSocialActivityLog() {
        return "app/social/activity-log";
    }

    @GetMapping("/app/social/notifications")
    public String appSocialNotifications() {
        return "app/social/notifications";
    }

    @GetMapping("/app/social/followers")
    public String appSocialFollowers() {
        return "app/social/followers";
    }
}
