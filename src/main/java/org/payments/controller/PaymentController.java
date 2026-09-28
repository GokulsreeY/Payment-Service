package org.payments.controller;

import org.payments.entities.Payment;
import org.payments.models.PaymentDto;
import org.payments.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class PaymentController {

    PaymentService paymentService;
    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }
    @PostMapping("/payments")
    public ResponseEntity<String> createPayment(@RequestBody PaymentDto payment){

        Payment paymentEntity = paymentService.createTransaction(payment);
        System.out.println(paymentEntity);

        return ResponseEntity.ok("Transaction Saved to DB");
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDto>> getPayments(){
        List<PaymentDto> payments = paymentService.getPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable UUID id){

        PaymentDto payment = paymentService.getPayment(id);

        return ResponseEntity.ok(payment);
    }
}
