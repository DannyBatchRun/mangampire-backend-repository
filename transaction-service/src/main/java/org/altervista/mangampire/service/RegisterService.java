package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RegisterService {
    @Autowired
    private RegisterRepository registerRepository;
    public List<Register> getRegister() {
        List<Register> registers = new ArrayList<>();
        registerRepository.findAll().iterator().forEachRemaining(registers::add);
        return registers;
    }
    public void addRegister(Register register) { registerRepository.save(register); }
    public Register searchRegister(String publisher) {
        return registerRepository.findByPublisher(publisher);
    }
    public String addCashToRegister(Manga manga) {
        String message = "No Message";
        BigDecimal cashAdd = BigDecimal.valueOf(manga.getPrice());
        String publisher = manga.getPublisher();
        Register register = registerRepository.findByPublisher(publisher);
        if(register != null) {
            BigDecimal newCash = register.getCash().add(cashAdd);
            register.setCash(newCash);
            registerRepository.save(register);
            message = "Added import of " + manga.getPrice() + "€ on register " + manga.getPublisher();
        } else {
            Register newRegister = new Register();
            newRegister.setPublisher(manga.getPublisher());
            newRegister.setCash(cashAdd);
            registerRepository.save(newRegister);
            message = "A new registry is created of " + manga.getPublisher() + " and have a cash of " + manga.getPrice() + "€.";
        }
        return message;
    }
    public Register addRegisterIfNotExists(String publisher) {
        Register searchRegister = registerRepository.findByPublisher(publisher);
        Register newRegisterIfRequired = new Register();
        if (searchRegister == null) {
            newRegisterIfRequired.setPublisher(publisher);
            newRegisterIfRequired.setCash(new BigDecimal("0"));
            addRegister(newRegisterIfRequired);
            return newRegisterIfRequired;
        } else {
            return searchRegister;
        }
    }
}
