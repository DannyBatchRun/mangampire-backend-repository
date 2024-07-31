package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.productdto.RequestLogin;
import org.altervista.mangampire.productdto.SearchClient;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {
    List<Card> getCards();
    List<Card> getCardsOfSingleClient(long idCardHolder);
    List<Client> getClients();
    boolean addClient(Client client);
    String addTheCardForTheClient(Card card, long idClient);
    Client foundAClientWithAllFieldsExceptPassword(SearchClient client);
    Card removeImportFromCard(Card card, BigDecimal newImport);
    Card searchCardByCardNumber(String cardNumber);
    Client searchClientByNameSurnameAndDateBirth(Client client);
    boolean isLoginValid(RequestLogin requestLogin);
    Client searchClientByEmail(String email);

}
