package org.altervista.mangampire.service;

import org.altervista.mangampire.model.*;
import org.altervista.mangampire.dto.SearchManga;
import org.altervista.mangampire.dto.Transaction;
import org.altervista.mangampire.request.EndpointRequest;
import org.altervista.mangampire.login.RequestLogin;
import org.altervista.mangampire.dto.SearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
@Service
public class BackendServiceImpl implements BackendService {
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(BackendService.class);
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public HttpStatus callAndCompleteTransaction(EndpointRequest transactionService, Transaction transaction) {
        transactionService.setRequest("/transaction/complete");
        String transactionUrl = transactionService.getEndpoint() + transactionService.getRequest();
        HttpEntity<Transaction> requestEntity = new HttpEntity<>(transaction);
        ResponseEntity<String> responseEntity = restTemplate.exchange(transactionUrl, HttpMethod.POST, requestEntity, String.class);
        return responseEntity.getStatusCode();
    }
    @Override
    public StringBuilder defineResponseStatus(HttpStatus status) {
        StringBuilder response = new StringBuilder();
        if (status == HttpStatus.OK) {
            logger.info("Transaction Service response: OK");
            response.append("Transaction Service response: OK");
        } else if (status == HttpStatus.PAYMENT_REQUIRED) {
            logger.info("Transaction Service response: PAYMENT REQUIRED");
            response.append("Transaction Service response: PAYMENT REQUIRED");
        } else if (status == HttpStatus.NOT_FOUND) {
            logger.info("Transaction Service response: NOT FOUND");
            response.append("Transaction Service response: NOT FOUND");
        } else {
            logger.error("Transaction Service response: ERROR. Please refer to related microservice to debug.");
            response.append("Transaction Service response: ERROR. Please refer to related microservice to debug.");
        }
        return response;
    }
    @Override
    public boolean addClientOnDatabase(EndpointRequest clientDatabase, Client client) {
        boolean isClientAdded = false;
        clientDatabase.setRequest("/client/add");
        String clientAdditionUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        String response = restTemplate.postForObject(clientAdditionUrl, client, String.class);
        if(response != null && response.contains("Added client")) {
            isClientAdded = true;
        }
        return isClientAdded;
    }
    @Override
    public Boolean controlIfClientOrMangaIfEmpty(Manga manga, Client client) {
        boolean isNotInitialized;
        if (manga.getIdManga() == 0 || manga.getName() == null || manga.getAuthor() == null ||
            manga.getGenre() == null || manga.getPrice() == 0 || manga.getQuantity() == 0) {
            isNotInitialized = true;
        } else isNotInitialized = client.getIdClient() == 0 || client.getEmail() == null || client.getName() == null ||
                client.getSurname() == null || client.getDateBirth() == null || client.getPassword() == null;
        return isNotInitialized;
    }
    @Override
    public Boolean loginToPlatform(EndpointRequest clientDatabase, RequestLogin requestLogin) {
        Boolean logged = false;
        try {
            String loginUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
            logged = restTemplate.postForObject(loginUrl, requestLogin, Boolean.class);
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Client");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Client");
        }
        return logged;
    }
    @Override
    public Manga getAMangaFromDatabase(EndpointRequest storehouseDatabase, SearchManga searchManga) {
        Manga mangaFound = new Manga();
        try {
            String searchMangaUrl = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            mangaFound = restTemplate.postForObject(searchMangaUrl, searchManga, Manga.class);
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Manga");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Manga");
        }
        return mangaFound;
    }
    @Override
    public Client getAClientFromDatabase(EndpointRequest clientDatabase, SearchClient searchClient) {
        Client clientFound = new Client();
        try {
            String searchClientUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
            clientFound = restTemplate.postForObject(searchClientUrl, searchClient, Client.class);
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Client");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Client");
        }
        return clientFound;
    }
    @Override
    public ShoppingCart getAShoppingCartFromDatabase(EndpointRequest shoppingCartDatabase) {
        ShoppingCart shoppingCart = new ShoppingCart();
        try {
            String searchShoppingCartUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            shoppingCart = restTemplate.getForObject(searchShoppingCartUrl, ShoppingCart.class);
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Shopping Cart");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Shopping Cart");
        }
        return shoppingCart;
    }
    @Override
    public boolean addMangaToCart(EndpointRequest shoppingCartDatabase, Client client, Manga manga) {
        boolean added = false;
        try {
            shoppingCartDatabase.setRequest("/cart/add/manga?idClient=" + client.getIdClient());
            String additionMangaUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            String response = restTemplate.postForObject(additionMangaUrl, manga, String.class);
            if(response != null && response.equalsIgnoreCase("Manga is added on Shopping Cart") ||
                response != null && response.equalsIgnoreCase("Manga is already present on shopping cart. Increased quantity.")) {
                added = true;
            }
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Shopping Cart");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Shopping Cart");
        }
        return added;
    }
    @Override
    public String checkExistingCartOrCreateIt(EndpointRequest shoppingCartDatabase, Client client) {
        String message = "No Message";
        shoppingCartDatabase.setRequest("/cart/search?idClient=" + client.getIdClient());
        String searchUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
        String response = restTemplate.getForObject(searchUrl, String.class);
        if(response != null && response.contains("Cart not found")) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setIdClient(client.getIdClient());
            shoppingCart.setManga(new ArrayList<>());
            shoppingCartDatabase.setRequest("/cart/add");
            String additionUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            response = restTemplate.postForObject(additionUrl, shoppingCart, String.class);
            logger.info("ShoppingCartService response : " + response);
        } else {
            message = "Cart found for idClient " + client.getIdClient() + ".";
        }
        return message;
    }
    @Override
    public String checkIfEmailIsExisting(EndpointRequest clientDatabase, RequestLogin requestLogin) {
        String isOnDatabase;
        String clientUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        String response = restTemplate.postForObject(clientUrl, requestLogin, String.class);
        if (response != null && response.contains("Almeno uno dei parametri client è vuoto")) {
            isOnDatabase = "Client is not on database";
        } else {
            isOnDatabase = "Client found";
        }
        return isOnDatabase;
    }
    @Override
    public Client getClientByEmailFromDatabase(EndpointRequest clientDatabase, RequestLogin requestLogin) {
        String clientUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        return restTemplate.postForObject(clientUrl, requestLogin, Client.class);
    }

    @Override
    public String getCardsOfTheClient(EndpointRequest clientDatabase) {
        String clientUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        return restTemplate.getForObject(clientUrl, String.class);
    }
    @Override
    public boolean controlIfMangaIsOnStock(EndpointRequest storehouseDatabase, Manga manga) {
        boolean available = false;
        storehouseDatabase.setRequest("/stock/in");
        String urlRequest = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
        String response = restTemplate.getForObject(urlRequest, String.class);
        if(response != null && response.contains(manga.getName()) && response.contains(String.valueOf(manga.getVolume()))) {
            available = true;
        } else {
            storehouseDatabase.setRequest("/stock/out");
            urlRequest = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            response = restTemplate.getForObject(urlRequest, String.class);
            if(response != null && response.contains(manga.getName()) && response.contains(String.valueOf(manga.getVolume()))) {
                logger.warn("Manga " + manga.getName() + " Volume " + manga.getVolume() + " is not available");
            }
        }
        return available;
    }
    @Override
    public String clearCartClient(EndpointRequest shoppingCartDatabase, long idClient) {
        String clearRequest = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
        HttpEntity<String> entity = new HttpEntity<String>("");
        ResponseEntity<String> response = restTemplate.exchange(clearRequest, HttpMethod.DELETE, entity, String.class);
        return response.getBody();
    }
    @Override
    public boolean addCardAndVerifyIfIsAdded(EndpointRequest clientDatabase, long idClient, Card card) {
        boolean added = false;
        clientDatabase.setRequest("/card/add?idClient=" + idClient);
        String additionUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        String message = restTemplate.postForObject(additionUrl, card, String.class);
        added = !message.equalsIgnoreCase("Card already exists on database") && !message.contains("There is no client with id");
        return added;
    }
    private void getNotFoundWarningParameter(HttpStatusCodeException e, String notFound) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            logger.warn(notFound + " not found: {}", e.getResponseBodyAsString());
        } else {
            logger.error("Error while retrieving " + notFound +  " {} - {}", e.getStatusCode(), e.getStatusText());
        }
        throw e;
    }
    private void launchAnErrorStatement(Exception e, String objectNotRetrieved) {
        logger.error("Error while retrieving " + objectNotRetrieved + ": {}", e.getMessage());
        throw new RuntimeException("Error while retrieving stock", e);
    }

}
