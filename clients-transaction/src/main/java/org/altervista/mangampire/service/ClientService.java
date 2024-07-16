package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.dto.RequestLogin;
import org.altervista.mangampire.dto.SearchClient;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {
    List<Card> getCards();
    List<Card> getCardsOfSingleClient(long idCardHolder);
    List<Register> getRegister();
    List<Client> getClients();
    boolean addClient(Client client);
    void addRegister (Register register);
    String addTheCardForTheClient(Card card, long idClient);
    Client foundAClientWithAllFieldsExceptPassword(SearchClient client);
    String addCashToRegister(Manga manga);
    Card removeImportFromCard(Card card, BigDecimal newImport);
    Register searchRegister(String publisher);
    Card searchCardByCardNumber(String cardNumber);
    Client searchClientByNameSurnameAndDateBirth(Client client);
    Register addRegisterIfNotExists(String publisher);
    boolean isLoginValid(RequestLogin requestLogin);
    Client searchClientByEmail(String email);

}
