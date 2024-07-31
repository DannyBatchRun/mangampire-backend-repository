package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.repository.ClientRepository;
import org.altervista.mangampire.repository.CardRepository;
import org.altervista.mangampire.productdto.RequestLogin;
import org.altervista.mangampire.productdto.SearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private CardRepository cardRepository;

    @Override
    public List<Card> getCards() {
        List<Card> cards = new ArrayList<>();
        cardRepository.findAll().iterator().forEachRemaining(cards::add);
        return cards;
    }
    @Override
    public List<Card> getCardsOfSingleClient(long idCardHolder) {
        List<Card> cards = new ArrayList<>();
        List<Card> cardsClient = new ArrayList<>();
        cardRepository.findAll().iterator().forEachRemaining(cards::add);
        for(Card c : cards) {
            if(c.getIdCardHolder() == idCardHolder) {
                cardsClient.add(c);
            }
        }
        return cardsClient;
    }
    @Override
    public List<Client> getClients() {
        List<Client> clients = new ArrayList<>();
        clientRepository.findAll().iterator().forEachRemaining(clients::add);
        return clients;
    }

    @Override
    public boolean addClient(Client client) {
        List<Client> clients = getClients();
        for(Client c : clients) {
            if(client.getEmail() == null) {
                System.out.println("There is no email or password setted.");
                return false;
            } else if(client.getEmail().equalsIgnoreCase(c.getEmail())) {
                System.out.println("Email is already in use");
                return false;
            }
        }
        if(client.getPassword() == null) {
            String password = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            client.setPassword(password);
            System.out.println("Generated a new password for new client " + client.getName() + " " + client.getSurname() + " : " + password);
        }
        clientRepository.save(client);
        return true;
    }

    @Override
    public Card searchCardByCardNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber);
    }

    @Override
    public Client searchClientByNameSurnameAndDateBirth(Client client) {
        return clientRepository.findByNameAndSurnameAndDateBirth(client.getName(), client.getSurname(), client.getDateBirth());
    }
    @Override
    public String addTheCardForTheClient(Card card, long idClient) {
        String message = "No Message";
        BigDecimal balance = new BigDecimal(3000);
        Client client = clientRepository.findByIdClient(idClient);
        Card cardAlreadyExist = cardRepository.findByCardNumber(card.getCardNumber());
        if(cardAlreadyExist == null) {
            card.setIdCardHolder(idClient);
            card.setBalance(balance);
        } else {
            message = "Card already exists on database";
            return message;
        }
        if(!(client == null)) {
            int cardQuantity = client.getCardQuantity();
            cardQuantity++;
            client.setCardQuantity(cardQuantity);
            clientRepository.save(client);
            cardRepository.save(card);
            message = "Added card to the client : " + idClient;
        } else {
            message = "There is no client with id : " + idClient;
        }
        return message;
    }

    @Override
    public Client foundAClientWithAllFieldsExceptPassword(SearchClient client) {
        long idClient = client.getIdClient();
        String email = client.getEmail();
        String name = client.getName();
        String surname = client.getSurname();
        String dateBirth = client.getDateBirth();
        return clientRepository.findByIdClientAndEmailAndNameAndSurnameAndDateBirth(idClient,email,name,surname,dateBirth);
    }

    @Override
    public Card removeImportFromCard(Card card, BigDecimal newImport) {
        card.setBalance(newImport);
        cardRepository.save(card);
        return card;
    }
    @Override
    public boolean isLoginValid(RequestLogin requestLogin) {
        Client client = clientRepository.findByEmail(requestLogin.getEmail());
        if(client == null) {
            System.out.println("Email was not found in the database");
            return false;
        }
        String passwordInserted = requestLogin.getPassword();
        String realPassword = client.getPassword();
        return passwordInserted.trim().equals(realPassword.trim());
    }

    @Override
    public Client searchClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    private void increaseQuantityOfCardPossession(Client client) {
        int newQuantityCard = client.getCardQuantity();
        newQuantityCard++;
        client.setCardQuantity(newQuantityCard);
        clientRepository.save(client);
    }
}
