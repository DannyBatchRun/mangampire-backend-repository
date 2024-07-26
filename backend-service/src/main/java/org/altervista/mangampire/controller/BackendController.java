package org.altervista.mangampire.controller;

import org.altervista.mangampire.dto.*;
import org.altervista.mangampire.model.*;
import org.altervista.mangampire.service.BackendService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class BackendController {

    @Autowired
    private Map<Client, List<Manga>> shoppingCartList;
    @Autowired
    private BackendService service;
    @Autowired
    private EndpointRequest storehouseDatabase;
    @Autowired
    private EndpointRequest clientDatabase;
    @Autowired
    private EndpointRequest shoppingCartDatabase;
    @GetMapping("/health")
    public String getHealth() {
        return "Service is up and running";
    }
    @PostConstruct
    public void init() {
        System.out.println("Initializing localhost for Patch Requests as default.");
        storehouseDatabase.setEndpoint("http://localhost:8081");
        clientDatabase.setEndpoint("http://localhost:8082");
        shoppingCartDatabase.setEndpoint("http://localhost:5000");
        System.out.println("Storehouse Database ---> " + storehouseDatabase.getEndpoint());
        System.out.println("Client Database ---> " + clientDatabase.getEndpoint());
        System.out.println("Shopping Cart Database ---> " + shoppingCartDatabase.getEndpoint());
    }
    @PatchMapping("/storehouse/url")
    public ResponseEntity<String> setStorehouseDatabase(@Validated @RequestBody EndpointRequest endpointRequest) {
        storehouseDatabase.setEndpoint("http://" + endpointRequest.getEndpoint());
        return ResponseEntity.ok().body("Endpoint for Storehouse setted");
    }

    @PatchMapping("/database/url")
    public ResponseEntity<String> setClientDatabase(@Validated @RequestBody EndpointRequest endpointRequest) {
        clientDatabase.setEndpoint("http://" + endpointRequest.getEndpoint());
        return ResponseEntity.ok().body("Endpoint for Client Database setted");
    }
    @PatchMapping("/shopping-cart/url")
    public ResponseEntity<String> setShoppingCartDatabase(@Validated @RequestBody EndpointRequest endpointRequest) {
        clientDatabase.setEndpoint("http://" + endpointRequest.getEndpoint());
        return ResponseEntity.ok().body("Endpoint for Shopping Cart Database setted");
    }
    @PostMapping("/client/add")
    public boolean addClientToDatabase(@Validated @RequestBody Client client) {
        return service.addClientOnDatabase(clientDatabase, client);
    }
    @PostMapping("/client/login")
    public ResponseEntity<String> loginToPlatform(@Validated @RequestBody RequestLogin requestLogin) {
        clientDatabase.setRequest("/client/login");
        boolean isPasswordValid = service.loginToPlatform(clientDatabase,requestLogin);
        String messageResponse = (isPasswordValid) ? "Login Succeded" : "Login Failed! Wrong Password";
        return ResponseEntity.ok().body(messageResponse);
    }

    @PostMapping("/client/search")
    public ResponseEntity<String> searchClientOnDatabase(@Validated @RequestBody RequestLogin requestLogin) {
        clientDatabase.setRequest("/client/search/email?showPasswordToken=No");
        String response = service.checkIfEmailIsExisting(clientDatabase,requestLogin);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/client/login/search")
    public String getClientFromDatabase(@Validated @RequestBody RequestLogin requestLogin) {
        clientDatabase.setRequest("/client/search/email?showPasswordToken=No");
        Client client = service.getClientByEmailFromDatabase(clientDatabase,requestLogin);
        if (client == null) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Almeno uno dei parametri client è vuoto");
            return errorJson.toString();
        }
        JSONObject json = new JSONObject();
        json.put("idClient", client.getIdClient());
        json.put("email", client.getEmail());
        json.put("name", client.getName());
        json.put("surname", client.getSurname());
        json.put("dateBirth", client.getDateBirth());
        json.put("cardQuantity", client.getCardQuantity());
        return json.toString();
    }

    @GetMapping("/client/cards")
    public String getCardsOfTheClient(@Validated @RequestParam long idCardHolder) {
        clientDatabase.setRequest("/client/cards?idCardHolder=" + idCardHolder);
        return service.getCardsOfTheClient(clientDatabase);
    }

    @PostMapping("/client/card/add")
    public boolean addCardToTheClientOnDatabase(@Validated @RequestParam long idClient, @Validated @RequestBody Card card) {
        return service.addCardAndVerifyIfIsAdded(clientDatabase, idClient, card);
    }
    @PostMapping("/cart/add")
    public ResponseEntity<String> addToCart(@Validated @RequestBody SearchClientManga clientManga) {
        boolean alreadyAdded = false;
        String message = "No Message";
        storehouseDatabase.setRequest("/manga/search");
        clientDatabase.setRequest("/client/search?showPasswordToken=no");
        Manga mangaFound = service.getAMangaFromDatabase(storehouseDatabase, clientManga.getManga());
        Client clientFound = service.getAClientFromDatabase(clientDatabase, clientManga.getClient());
        boolean isClientOrMangaEmpty = service.controlIfClientOrMangaIfEmpty(mangaFound,clientFound);
        if(isClientOrMangaEmpty) {
            message = "Client or manga was not found";
            return ResponseEntity.ok().body(message);
        }
        boolean isMangaOnStock = service.controlIfMangaIsOnStock(storehouseDatabase, mangaFound);
        if(isMangaOnStock) {
            System.out.println(service.checkExistingCartOrCreateIt(shoppingCartDatabase, clientFound));
            boolean added = service.addMangaToCart(shoppingCartDatabase,clientFound,mangaFound);
            if(added) {
                message = "Manga added for idClient " + clientFound.getIdClient();
            } else {
                message = "Something went wrong for addition manga. Retry.";
            }
        } else {
            return ResponseEntity.ok().body("Manga is not available");
        }
        return ResponseEntity.ok().body(message);
    }
    @PostMapping("/cart/search")
    public List<Manga> getSingleCartClient(@Validated @RequestBody SearchClient client) {
        clientDatabase.setRequest("/client/search?showPasswordToken=no");
        Client clientFound = service.getAClientFromDatabase(clientDatabase, client);
        List<Manga> mangaList = shoppingCartList.get(clientFound);
        return shoppingCartList.get(clientFound);
    }
    @PostMapping("/transaction/complete")
    public ResponseEntity<StringBuilder> buyAManga(@Validated @RequestParam String cardNumber, @Validated @RequestBody SearchClient client) {
        StringBuilder response = service.completeTransaction(client,cardNumber,storehouseDatabase,clientDatabase,shoppingCartDatabase);
        return ResponseEntity.ok().body(response);
    }
}
