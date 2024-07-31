package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;

import java.util.List;

public interface RegisterService {
    List<Register> getRegister();
    void addRegister (Register register);
    Register searchRegister(String publisher);
    String addCashToRegister(Manga manga);
    Register addRegisterIfNotExists(String publisher);

}
