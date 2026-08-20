
package com.soham.railway_reservation_engine.payment.controller;


import com.soham.railway_reservation_engine.payment.dto.PaymentInitiateRequest;
import com.soham.railway_reservation_engine.payment.dto.PaymentInitiateResponse;
import com.soham.railway_reservation_engine.payment.dto.PaymentWebhookRequest;
import com.soham.railway_reservation_engine.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP layer for payments.
 *
 * <p>{@code POST /payment/initiate} — client sends the PNR, server creates the Razorpay order and
 * returns the order id + public key id. {@code POST /payment/webhook} — Razorpay's server-to-server
 * callback: signature arrives in the {@code X-Razorpay-Signature} header and the raw JSON payload
 * in the body. The webhook endpoint deliberately takes the body as a raw {@code String} (not a DTO)
 * because signature verification must hash the exact bytes received.
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request
    ) throws Exception {

        PaymentInitiateResponse response =
                paymentService.initiatePayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    //whenever an HTTP  request comes to /payment/webhook, this method will be invoked.
    // It will extract the Razorpay signature from the request header and the payload from the request body.
    // The handleWebhook method of the paymentService will be called to process the webhook event.
    // If everything goes well, it will return a 200 OK response with a success message.
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-Razorpay-Signature")
            String signature,
            @RequestBody
            String payload
    ) throws Exception {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed successfully.");

    }
}

