package org.altervista.mangampire.controller;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.dto.RequestLogin;
import org.altervista.mangampire.dto.SearchClient;
import org.altervista.mangampire.service.ClientService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
public class ClientController {

    @Autowired
    private ClientService service;

    @GetMapping("/health")
    public String getHealth() {
        return "Service is up and running";
    }

    @GetMapping("/client/all")
    public ResponseEntity<String> getAllClients() {
        List<Client> clients = service.getClients();
        return ResponseEntity.ok().body("List of all clients:\n" + clients);
    }

    @PostMapping("/client/add")
    public ResponseEntity<String> addAClientToDatabase(@Validated @RequestBody Client client) {
        boolean isClientAdded = service.addClient(client);
        String message = client.getName() + " " + client.getSurname() + " " + " born on " + client.getDateBirth();
        String responseAdded = (isClientAdded) ? "Added client:\n" : "Email is already used. Try again.\n";
        return ResponseEntity.ok().body(responseAdded + message);
    }

    @PostMapping("/client/search")
    public String searchClient(@Validated @RequestParam String showPasswordToken, @Validated @RequestBody SearchClient client) {
        Client clientSearch = service.foundAClientWithAllFieldsExceptPassword(client);
        if (clientSearch == null) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Almeno uno dei parametri client è vuoto");
            return errorJson.toString();
        }
        JSONObject json = new JSONObject();
        json.put("idClient", clientSearch.getIdClient());
        json.put("email", clientSearch.getEmail());
        json.put("name", clientSearch.getName());
        json.put("surname", clientSearch.getSurname());
        json.put("dateBirth", clientSearch.getDateBirth());
        json.put("cardQuantity", clientSearch.getCardQuantity());
        json.put("password", showPasswordToken.matches("ER2#yF8@!Dq1&xN5") ? clientSearch.getPassword() : "** RESTRICTED **");
        return json.toString();
    }
    @PostMapping("/client/search/email")
    public String searchClientByEmail(@Validated @RequestParam String showPasswordToken, @Validated @RequestBody RequestLogin requestLogin) {
        Client clientSearch = service.searchClientByEmail(requestLogin.getEmail());
        if (clientSearch == null) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Almeno uno dei parametri client è vuoto");
            return errorJson.toString();
        }
        JSONObject json = new JSONObject();
        json.put("idClient", clientSearch.getIdClient());
        json.put("email", clientSearch.getEmail());
        json.put("name", clientSearch.getName());
        json.put("surname", clientSearch.getSurname());
        json.put("dateBirth", clientSearch.getDateBirth());
        json.put("cardQuantity", clientSearch.getCardQuantity());
        json.put("password", showPasswordToken.matches("ER2#yF8@!Dq1&xN5") ? clientSearch.getPassword() : "** RESTRICTED **");
        return json.toString();
    }
    @GetMapping("/card/all")
    public ResponseEntity<String> getAllCards() {
        List<Card> cards = service.getCards();
        return ResponseEntity.ok().body("List of cards:\n" + cards);
    }

    @GetMapping("/client/cards")
    public List<Card> getCardsOfTheClient(@Validated @RequestParam long idCardHolder) {
        return service.getCardsOfSingleClient(idCardHolder);
    }
    @PostMapping("/card/add")
    public ResponseEntity<String> addCardForTheClient(@Validated @RequestParam long idClient, @Validated @RequestBody Card card) {
        String message = service.addTheCardForTheClient(card, idClient);
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/card/search")
    public String searchCard(@Validated @RequestParam String cardNumber) {
        Card cardFound = service.searchCardByCardNumber(cardNumber);
        if (cardFound == null) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Impossible to found the card");
            return errorJson.toString();
        }
        JSONObject json = new JSONObject();
        json.put("id", cardFound.getId());
        json.put("idCardHolder", cardFound.getIdCardHolder());
        json.put("cardNumber", cardFound.getCardNumber());
        json.put("cardHolderName", cardFound.getCardHolderName());
        json.put("cardHolderSurname", cardFound.getCardHolderSurname());
        json.put("cardExpire", cardFound.getCardExpire());
        json.put("balance", cardFound.getBalance());
        return json.toString();
    }

    @PostMapping("/card/update")
    public String defineNewImport(@Validated @RequestBody Card card, @Validated @RequestParam BigDecimal newImport) {
        Card cardUpdated = service.removeImportFromCard(card, newImport);
        if (cardUpdated == null) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Impossible to found the card");
            return errorJson.toString();
        }
        JSONObject json = new JSONObject();
        json.put("id", cardUpdated.getId());
        json.put("idCardHolder", cardUpdated.getIdCardHolder());
        json.put("cardNumber", cardUpdated.getCardNumber());
        json.put("cardHolderName", cardUpdated.getCardHolderName());
        json.put("cardHolderSurname", cardUpdated.getCardHolderSurname());
        json.put("cardExpire", cardUpdated.getCardExpire());
        json.put("balance", cardUpdated.getBalance());
        return json.toString();
    }
    @PostMapping("/client/login")
    public boolean defineLoginResponse(@Validated @RequestBody RequestLogin requestLogin) {
        return service.isLoginValid(requestLogin);
    }

    private boolean isClientEmpty (Client client){
        return client.getName() == null || client.getName().isEmpty() ||
                client.getSurname() == null || client.getSurname().isEmpty() ||
                client.getDateBirth() == null || client.getDateBirth().isEmpty();
    }
}
