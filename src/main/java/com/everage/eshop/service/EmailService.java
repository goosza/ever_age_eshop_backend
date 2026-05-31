package com.everage.eshop.service;

import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.Shipping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@everage.com}")
    private String fromEmail;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Send order confirmation email after successful payment.
     */
    @Async
    public void sendOrderConfirmation(Order order, Shipping shipping) {
        try {
            Context ctx = new Context();
            ctx.setVariable("order", order);
            ctx.setVariable("shipping", shipping);
            ctx.setVariable("trackOrderUrl",
                    frontendUrl + "/orders/track/" + order.getOrderNumber());

            String html = templateEngine.process("email/order-confirmation", ctx);

            sendEmail(
                    order.getEmail(),
                    "Order Confirmed: " + order.getOrderNumber(),
                    html
            );

            log.info("Order confirmation email sent to: {}", order.getEmail());

        } catch (Exception e) {
            // Email failure should never break the order flow
            log.error("Failed to send order confirmation email to {}: {}",
                    order.getEmail(), e.getMessage());
        }
    }

    /**
     * Send shipping notification when order is dispatched.
     */
    @Async
    public void sendShippingNotification(Order order, Shipping shipping) {
        try {
            Context ctx = new Context();
            ctx.setVariable("order", order);
            ctx.setVariable("shipping", shipping);
            ctx.setVariable("trackingNumber", shipping.getTrackingNumber());
            ctx.setVariable("trackOrderUrl",
                    frontendUrl + "/orders/track/" + order.getOrderNumber());

            String html = templateEngine.process("email/shipping-notification", ctx);

            sendEmail(
                    order.getEmail(),
                    "Your order is on its way: " + order.getOrderNumber(),
                    html
            );

            log.info("Shipping notification email sent to: {}", order.getEmail());

        } catch (Exception e) {
            log.error("Failed to send shipping notification email to {}: {}",
                    order.getEmail(), e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}
