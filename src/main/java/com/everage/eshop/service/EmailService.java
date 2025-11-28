package com.everage.eshop.service;

import com.everage.eshop.entity.Order;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendOrderConfirmationEmail(Order order){
        sendEmail(order.getEmail(),
                "Order Confirmation",
                "Your order has been confirmed.");
    }
    public void sendEmail(String to, String subject, String body) {
        // Logic to send email
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}
