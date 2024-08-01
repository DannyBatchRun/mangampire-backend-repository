package org.altervista.mangampire.controller;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.Register;
import org.altervista.mangampire.service.RegisterService;
import org.altervista.mangampire.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.altervista.mangampire.dto.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class TransactionController {
    @Autowired
    private RegisterService registerService;
    @Autowired
    private TransactionService transactionService;
    @GetMapping("/register/all")
    public ResponseEntity<String> getAllRegisters() {
        List<Register> registers = registerService.getRegister();
        return ResponseEntity.ok().body("List of all registers :\n" + registers);
    }
    @GetMapping("/register/search")
    public ResponseEntity<Register> searchRegister(@Validated @RequestParam String publisher) {
        String message = "No message";
        Register register = registerService.addRegisterIfNotExists(publisher);
        return ResponseEntity.ok().body(register);
    }
    @PostMapping("/register/add")
    public String searchRegister(@Validated @RequestBody Manga manga) {
        return registerService.addCashToRegister(manga);
    }
    @PostMapping("/transaction/complete")
    public ResponseEntity<StringBuilder> purchaseCart(@Validated @RequestBody Transaction transaction) {
        StringBuilder response = new StringBuilder();
        String name = transaction.getClient().getName();
        String surname = transaction.getClient().getSurname();
        boolean transactionCompleted = transactionService.completeTransaction(transaction.getClient(),transaction.getCardNumber(),transaction.getServices());
        if(transactionCompleted) {
            response.append("Transaction for ").append(name).append(" ").append(surname).append(" completed.");
        } else {
            response.append("Something went wrong with ").append(name).append(" ").append(surname).append(" transaction.");
        }
        return ResponseEntity.ok().body(response);
    }

}
