package org.altervista.mangampire.controller;

import org.altervista.mangampire.login.RequestLogin;
import org.altervista.mangampire.request.EndpointRequest;
import org.altervista.mangampire.productdto.*;
import org.altervista.mangampire.model.*;
import org.altervista.mangampire.service.BackendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import org.json.JSONObject;
import java.util.Map;

@RestController
public class BackendController {
    private static final Logger logger = LoggerFactory.getLogger(BackendController.class);
    @Autowired
    private BackendService service;
    @Autowired
    private Map<String, EndpointRequest> services;
    @GetMapping("/health")
    public String getHealth() {
        return "Service is up and running";
    }
    @PostConstruct
    public void init() {
        logger.info("Initializing localhost for Patch Requests as default.");
        services.put("storehouseDatabase",new EndpointRequest("http://localhost:8081", ""));
        services.put("clientDatabase", new EndpointRequest("http://localhost:8082", ""));
        services.put("transactionDatabase", new EndpointRequest("http://localhost:8083",""));
        services.put("shoppingCartDatabase", new EndpointRequest("http://localhost:5000",""));
        logger.info("Storehouse Database ---> " + services.get("storehouseDatabase").getEndpoint());
        logger.info("Client Database ---> " + services.get("clientDatabase").getEndpoint());
        logger.info("Transaction Database ---> " + services.get("transactionDatabase").getEndpoint());
        logger.info("Shopping Cart Database ---> " + services.get("shoppingCartDatabase").getEndpoint());
    }
    @PatchMapping("/storehouse/url")
    public ResponseEntity<String> setStorehouseDatabase(@Validated @RequestBody EndpointRequest endpointRequest) {
        services.get("storehouseDatabase").setEndpoint("http://" + endpointRequest.getEndpoint());
        return ResponseEntity.ok().body("Endpoint for Storehouse setted");
    }
    @PatchMapping("/client/url")
    public ResponseEntity<String> setClientDatabase(@Validated @RequestBody EndpointRequest endpointRequest) {
        services.get("clientDatabase").setEndpoint("http://" + endpointRequest.getEndpoint());
        return ResponseEntity.ok().body("Endpoint for Client Database setted");
    }
    @PatchMapping("/transaction/url")
    public ResponseEntity<String> setTransactionDatabase(@Validated @RequestBody EndpointRequest endpointRequest) {
        services.get("transactionDatabase").setEndpoint("http://" + endpointRequest.getEndpoint());
        return ResponseEntity.ok().body("Endpoint for Transaction Database setted");
    }
    @PatchMapping("/shopping-cart/url")
    public ResponseEntity<String> setShoppingCartDatabase(@Validated @RequestBody EndpointRequest endpointRequest) {
        services.get("shoppingCartDatabase").setEndpoint("http://" + endpointRequest.getEndpoint());
        return ResponseEntity.ok().body("Endpoint for Shopping Cart Database setted");
    }
    @PostMapping("/client/add")
    public boolean addClientToDatabase(@Validated @RequestBody Client client) {
        return service.addClientOnDatabase(services.get("clientDatabase"), client);
    }
    @PostMapping("/client/login")
    public ResponseEntity<String> loginToPlatform(@Validated @RequestBody RequestLogin requestLogin) {
        services.get("clientDatabase").setRequest("/client/login");
        boolean isPasswordValid = service.loginToPlatform(services.get("clientDatabase"),requestLogin);
        String messageResponse = (isPasswordValid) ? "Login Succeded" : "Login Failed! Wrong Password";
        return ResponseEntity.ok().body(messageResponse);
    }

    @PostMapping("/client/search")
    public ResponseEntity<String> searchClientOnDatabase(@Validated @RequestBody RequestLogin requestLogin) {
        services.get("clientDatabase").setRequest("/client/search/email?showPasswordToken=No");
        String response = service.checkIfEmailIsExisting(services.get("clientDatabase"),requestLogin);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/client/login/search")
    public String getClientFromDatabase(@Validated @RequestBody RequestLogin requestLogin) {
        services.get("clientDatabase").setRequest("/client/search/email?showPasswordToken=No");
        Client client = service.getClientByEmailFromDatabase(services.get("clientDatabase"),requestLogin);
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
        services.get("clientDatabase").setRequest("/client/cards?idCardHolder=" + idCardHolder);
        return service.getCardsOfTheClient(services.get("clientDatabase"));
    }

    @PostMapping("/client/card/add")
    public boolean addCardToTheClientOnDatabase(@Validated @RequestParam long idClient, @Validated @RequestBody Card card) {
        return service.addCardAndVerifyIfIsAdded(services.get("clientDatabase"), idClient, card);
    }
    @PostMapping("/cart/add")
    public ResponseEntity<String> addToCart(@Validated @RequestBody SearchClientManga clientManga) {
        boolean alreadyAdded = false;
        String message = "No Message";
        services.get("storehouseDatabase").setRequest("/manga/search");
        services.get("clientDatabase").setRequest("/client/search?showPasswordToken=no");
        Manga mangaFound = service.getAMangaFromDatabase(services.get("storehouseDatabase"), clientManga.getManga());
        Client clientFound = service.getAClientFromDatabase(services.get("clientDatabase"), clientManga.getClient());
        boolean isClientOrMangaEmpty = service.controlIfClientOrMangaIfEmpty(mangaFound,clientFound);
        if(isClientOrMangaEmpty) {
            message = "Client or manga was not found";
            return ResponseEntity.ok().body(message);
        }
        boolean isMangaOnStock = service.controlIfMangaIsOnStock(services.get("storehouseDatabase"), mangaFound);
        if(isMangaOnStock) {
            logger.info(service.checkExistingCartOrCreateIt(services.get("shoppingCartDatabase"), clientFound));
            boolean added = service.addMangaToCart(services.get("shoppingCartDatabase"),clientFound,mangaFound);
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
    public ShoppingCart getSingleCartClient(@Validated @RequestBody SearchClient client) {
        services.get("clientDatabase").setRequest("/client/search?showPasswordToken=no");
        Client clientFound = service.getAClientFromDatabase(services.get("clientDatabase"), client);
        services.get("shoppingCartDatabase").setRequest("/cart/search?idClient=" + clientFound.getIdClient());
        return service.getAShoppingCartFromDatabase(services.get("shoppingCartDatabase"));
    }
    @GetMapping("/cart/clear")
    public Boolean clearSingleCartClient(@Validated @RequestParam long idClient) {
        Boolean cleared = Boolean.FALSE;
        services.get("shoppingCartDatabase").setRequest("/cart/clear?idClient=" + idClient);
        String response = service.clearCartClient(services.get("shoppingCartDatabase"), idClient);
        if(response.equalsIgnoreCase("All elements in cart are deleted")) {
            cleared = Boolean.TRUE;
        }
        return cleared;
    }
    @PostMapping("/transaction/complete")
    public ResponseEntity<StringBuilder> buyAManga(@Validated @RequestParam String cardNumber, @Validated @RequestBody SearchClient client) {
        StringBuilder response = new StringBuilder();
        boolean transactionCompleted = service.completeTransaction(client,cardNumber,services);
        if(transactionCompleted) {
            response.append("Transaction for ").append(client.getName()).append(" ").append(client.getSurname()).append(" completed.");
        } else {
            response.append("Something went wrong with ").append(client.getName()).append(" ").append(client.getSurname()).append(" transaction.");
        }
        return ResponseEntity.ok().body(response);
    }
}
