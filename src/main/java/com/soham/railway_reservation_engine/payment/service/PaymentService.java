package com.soham.railway_reservation_engine.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.soham.railway_reservation_engine.bookings.entity.Booking;
import com.soham.railway_reservation_engine.bookings.repository.BookingRepository;
import com.soham.railway_reservation_engine.common.enums.BookingStatus;
import com.soham.railway_reservation_engine.common.enums.PaymentGateway;
import com.soham.railway_reservation_engine.common.enums.PaymentMethod;
import com.soham.railway_reservation_engine.common.enums.PaymentStatus;
import com.soham.railway_reservation_engine.common.exception.BookingNotFoundException;
import com.soham.railway_reservation_engine.common.exception.PaymentAlreadyInitiatedException;
import com.soham.railway_reservation_engine.common.exception.PaymentNotAllowedException;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.payment.config.RazorpayProperties;
import com.soham.railway_reservation_engine.payment.dto.PaymentInitiateRequest;
import com.soham.railway_reservation_engine.payment.dto.PaymentInitiateResponse;
import com.soham.railway_reservation_engine.payment.dto.PaymentWebhookRequest;
import com.soham.railway_reservation_engine.payment.entity.Payment;
import com.soham.railway_reservation_engine.payment.repository.PaymentRepository;
import com.soham.railway_reservation_engine.seat.service.SeatHoldService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


//Responsibilty is to create a payment order and process the payment result
@Service
@RequiredArgsConstructor

public class PaymentService {
    /*
    So a general flow would be
    Find booking
    validate booking
    create razorpay order
    build the payment entity save payment and then as usual return the dto
`*/


    private final PaymentFailureService paymentFailureService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final SeatHoldService seatHoldService;


    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request)  throws Exception {
        //find the booking by the pnr
        Booking booking = bookingRepository.findByPnr(request.pnr())
                .orElseThrow(() -> new BookingNotFoundException(request.pnr()));

        //Validate the booking status
        if(booking.getBookingStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new PaymentNotAllowedException(
                    booking.getPnr());
        }

        //preventing duplicate payment initiation for the same booking
        paymentRepository.findByBooking(booking).ifPresent(payment -> {
            throw new PaymentAlreadyInitiatedException(
                    booking.getPnr());
        });


        //converting the amount to the smallest currency unit
        long amountInPaise = booking.getTotalFare()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        //create a razorpay order request
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", booking.getPnr());

        //Call razor pay api
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");


        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalFare())
                .gateway(PaymentGateway.RAZORPAY)
                .paymentMethod(PaymentMethod.ONLINE)
                .paymentStatus(PaymentStatus.PENDING)
                .razorpayOrderId(razorpayOrderId)
                .build();

        paymentRepository.save(payment);

        return new PaymentInitiateResponse(
                razorpayOrderId,
                razorpayProperties.getKeyId(),
                booking.getTotalFare(),
                "INR",
                booking.getPnr()

        );

    }
    /*
    Verify the razorpay signature
    find the payment
    update payment status
    update the booking status
    trigger cancellation flow if payment failed
*/

    @Transactional
    public void handleWebhook(
            String payload,
            String signature
    ) throws Exception {

        Utils.verifyWebhookSignature(
                payload,
                signature,
                razorpayProperties.getWebhookSecret()
        );

        JSONObject webhook = new JSONObject(payload);

        String event = webhook.getString("event");

        JSONObject paymentEntity = webhook
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId =
                paymentEntity.getString("order_id");

        String razorpayPaymentId =
                paymentEntity.getString("id");

        Payment payment = paymentRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for Razorpay Order : "
                                        + razorpayOrderId
                        ));

        // Idempotency
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS
                || payment.getPaymentStatus() == PaymentStatus.FAILED) {

            return;
        }

        // Payment successful
        if ("payment.captured".equals(event)) {

            payment.setPaymentStatus(
                    PaymentStatus.SUCCESS
            );

            payment.setTransactionId(
                    razorpayPaymentId
            );

            payment.setPaidAt(
                    LocalDateTime.now()
            );

            Booking booking = payment.getBooking();

            booking.setBookingStatus(
                    BookingStatus.CONFIRMED
            );

            // Remove temporary Redis seat holds
            for (Passenger passenger : booking.getPassengers()) {

                if (passenger.getSeat() != null) {

                    seatHoldService.releaseSeat(
                            booking.getSchedule().getId(),
                            passenger.getSeat().getId()
                    );
                }
            }

            paymentRepository.save(payment);
            bookingRepository.save(booking);

            return;
        }

        // Payment failed
        if ("payment.failed".equals(event)) {

            payment.setPaymentStatus(
                    PaymentStatus.FAILED
            );

            payment.setTransactionId(
                    razorpayPaymentId
            );

            paymentRepository.save(payment);

            paymentFailureService.handlePaymentFailure(
                    payment.getBooking()
            );

            return;
        }
    }
}