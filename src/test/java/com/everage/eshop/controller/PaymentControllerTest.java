package com.everage.eshop.controller;

import com.everage.eshop.dto.PaymentRequest;
import com.everage.eshop.dto.PaymentResponse;
import com.everage.eshop.entity.PaymentMethod;
import com.everage.eshop.entity.PaymentStatus;
import com.everage.eshop.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processPayment_ShouldReturn200WithCompletedStatus() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(orderId, PaymentMethod.CREDIT_CARD, "tok_visa");
        PaymentResponse response = createPaymentResponse(orderId, PaymentStatus.COMPLETED);

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void processPayment_WhenFails_ShouldReturn500() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentRequest request = new PaymentRequest(orderId, PaymentMethod.CREDIT_CARD, "bad_token");

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException("Payment failed"));

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void refundPayment_ShouldReturn200WithRefundedStatus() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentResponse response = createPaymentResponse(orderId, PaymentStatus.REFUNDED);

        when(paymentService.refundPayment(paymentId)).thenReturn(response);

        mockMvc.perform(post("/api/payments/{paymentId}/refund", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void refundPayment_WhenNotCompleted_ShouldReturn500() throws Exception {
        UUID paymentId = UUID.randomUUID();

        when(paymentService.refundPayment(paymentId))
                .thenThrow(new RuntimeException("Only completed payments can be refunded"));

        mockMvc.perform(post("/api/payments/{paymentId}/refund", paymentId))
                .andExpect(status().isInternalServerError());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PaymentResponse createPaymentResponse(UUID orderId, PaymentStatus status) {
        return new PaymentResponse(
                UUID.randomUUID(), orderId,
                BigDecimal.valueOf(99.99), status,
                "pi_test_123", LocalDateTime.now()
        );
    }
}
