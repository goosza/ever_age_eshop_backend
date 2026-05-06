package com.everage.eshop.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Hidden
public class CheckoutPageController {

    @GetMapping(value = "/checkout/success", produces = MediaType.TEXT_HTML_VALUE)
    public String checkoutSuccess(@RequestParam(required = false) String session_id) {
        log.info("Checkout success page accessed. Session ID: {}", session_id);
        
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Payment Successful</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            min-height: 100vh;
                            margin: 0;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        }
                        .container {
                            background: white;
                            padding: 3rem;
                            border-radius: 1rem;
                            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                            text-align: center;
                            max-width: 500px;
                        }
                        .success-icon {
                            width: 80px;
                            height: 80px;
                            background: #10b981;
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin: 0 auto 1.5rem;
                            animation: scaleIn 0.5s ease-out;
                        }
                        .success-icon svg {
                            width: 50px;
                            height: 50px;
                            stroke: white;
                            stroke-width: 3;
                            stroke-linecap: round;
                            stroke-linejoin: round;
                            fill: none;
                        }
                        h1 {
                            color: #1f2937;
                            margin: 0 0 1rem;
                            font-size: 2rem;
                        }
                        p {
                            color: #6b7280;
                            line-height: 1.6;
                            margin: 0 0 2rem;
                        }
                        .session-id {
                            background: #f3f4f6;
                            padding: 0.75rem;
                            border-radius: 0.5rem;
                            font-family: monospace;
                            font-size: 0.875rem;
                            color: #4b5563;
                            word-break: break-all;
                            margin-bottom: 2rem;
                        }
                        .info {
                            background: #dbeafe;
                            border-left: 4px solid #3b82f6;
                            padding: 1rem;
                            border-radius: 0.5rem;
                            text-align: left;
                            margin-top: 2rem;
                        }
                        .info p {
                            margin: 0;
                            color: #1e40af;
                            font-size: 0.875rem;
                        }
                        @keyframes scaleIn {
                            from {
                                transform: scale(0);
                                opacity: 0;
                            }
                            to {
                                transform: scale(1);
                                opacity: 1;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success-icon">
                            <svg viewBox="0 0 24 24">
                                <polyline points="20 6 9 17 4 12"></polyline>
                            </svg>
                        </div>
                        <h1>Payment Successful!</h1>
                        <p>Thank you for your purchase. Your order has been confirmed and will be processed shortly.</p>
                        """ + (session_id != null ? 
                        "<div class=\"session-id\">Session ID: " + session_id + "</div>" : "") + """
                        <div class="info">
                            <p><strong>What's next?</strong><br>
                            You will receive an email confirmation with your order details and tracking information.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/checkout/cancel", produces = MediaType.TEXT_HTML_VALUE)
    public String checkoutCancel() {
        log.info("Checkout cancel page accessed");
        
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Payment Cancelled</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            min-height: 100vh;
                            margin: 0;
                            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                        }
                        .container {
                            background: white;
                            padding: 3rem;
                            border-radius: 1rem;
                            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                            text-align: center;
                            max-width: 500px;
                        }
                        .cancel-icon {
                            width: 80px;
                            height: 80px;
                            background: #ef4444;
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin: 0 auto 1.5rem;
                            animation: scaleIn 0.5s ease-out;
                        }
                        .cancel-icon svg {
                            width: 50px;
                            height: 50px;
                            stroke: white;
                            stroke-width: 3;
                            stroke-linecap: round;
                            stroke-linejoin: round;
                            fill: none;
                        }
                        h1 {
                            color: #1f2937;
                            margin: 0 0 1rem;
                            font-size: 2rem;
                        }
                        p {
                            color: #6b7280;
                            line-height: 1.6;
                            margin: 0 0 2rem;
                        }
                        .info {
                            background: #fef3c7;
                            border-left: 4px solid #f59e0b;
                            padding: 1rem;
                            border-radius: 0.5rem;
                            text-align: left;
                            margin-top: 2rem;
                        }
                        .info p {
                            margin: 0;
                            color: #92400e;
                            font-size: 0.875rem;
                        }
                        @keyframes scaleIn {
                            from {
                                transform: scale(0);
                                opacity: 0;
                            }
                            to {
                                transform: scale(1);
                                opacity: 1;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="cancel-icon">
                            <svg viewBox="0 0 24 24">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                        </div>
                        <h1>Payment Cancelled</h1>
                        <p>Your payment was cancelled. No charges have been made to your account.</p>
                        <div class="info">
                            <p><strong>Need help?</strong><br>
                            If you experienced any issues during checkout, please contact our support team.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
