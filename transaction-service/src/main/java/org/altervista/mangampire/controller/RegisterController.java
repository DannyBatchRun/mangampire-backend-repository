package org.altervista.mangampire.controller;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.Register;
import org.altervista.mangampire.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RegisterController {

    @Autowired
    private RegisterService service;

    @GetMapping("/register/all")
    public ResponseEntity<String> getAllRegisters() {
        List<Register> registers = service.getRegister();
        return ResponseEntity.ok().body("List of all registers :\n" + registers);
    }
    @GetMapping("/register/search")
    public ResponseEntity<Register> searchRegister(@Validated @RequestParam String publisher) {
        String message = "No message";
        Register register = service.addRegisterIfNotExists(publisher);
        return ResponseEntity.ok().body(register);
    }

    @PostMapping("/register/add")
    public String searchRegister(@Validated @RequestBody Manga manga) {
        return service.addCashToRegister(manga);
    }
}
