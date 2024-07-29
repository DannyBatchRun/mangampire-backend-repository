package org.altervista.mangampire.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.altervista.mangampire.model.*;
import org.altervista.mangampire.dto.SearchManga;
import org.altervista.mangampire.dto.EndpointRequest;
import org.altervista.mangampire.dto.RequestLogin;
import org.altervista.mangampire.dto.SearchClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BackendServiceImpl implements BackendService {

    @Autowired
    private static final Logger LOGGER = LoggerFactory.getLogger(BackendService.class);

    @Autowired
    private RestTemplate restTemplate;
    @Override
    public boolean addClientOnDatabase(EndpointRequest clientDatabase, Client client) {
        boolean isClientAdded = false;
        clientDatabase.setRequest("/client/add");
        String clientAdditionUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        String response = restTemplate.postForObject(clientAdditionUrl, client, String.class);
        if(response.contains("Added client")) {
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
        } else if (client.getIdClient() == 0 || client.getEmail() == null || client.getName() == null ||
                   client.getSurname() == null || client.getDateBirth() == null || client.getPassword() == null) {
            isNotInitialized = true;
        } else {
            isNotInitialized = false;
        }
        return isNotInitialized;
    }
    @Override
    public Boolean loginToPlatform(EndpointRequest clientDatabase, RequestLogin requestLogin) {
        try {
            String loginUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
            return restTemplate.postForObject(loginUrl, requestLogin, Boolean.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Manga not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving manga: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving manga: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving manga", e);
        }
    }
    @Override
    public StringBuilder completeTransaction (SearchClient client, String cardNumber, EndpointRequest storehouseDatabase, EndpointRequest clientDatabase, EndpointRequest shoppingCartDatabase) {
        StringBuilder message = new StringBuilder();
        BigDecimal totalCart = new BigDecimal("0.0");
        Card transactionCard; boolean isOnStock; boolean enoughCredit;
        clientDatabase.setRequest("/client/search?showPasswordToken=no");
        Client clientFound = getAClientFromDatabase(clientDatabase, client);
        shoppingCartDatabase.setRequest("/cart/search?idClient=" + clientFound.getIdClient());
        String searchShoppingCart = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
        ShoppingCart shoppingCartClient = restTemplate.getForObject(searchShoppingCart, ShoppingCart.class);
        if(shoppingCartClient == null) {
            message.append(getRuntimeMessage("There is no manga to buy or client shopping cart was not found"));
            return message;
        } else if (clientFound.getCardQuantity() <= 0) {
            message.append(getRuntimeMessage("There is no card registered for client " + clientFound.getName() + " " + clientFound.getSurname() + ". Please register a new one."));
            return message;
        }
        storehouseDatabase.setRequest("/stock/search");
        List<Manga> mangaCartClient = shoppingCartClient.getManga();
        for(Manga m : mangaCartClient) {
            System.out.println("Check if " + m.getName() + " Volume " + m.getVolume() + " is available on stock...");
            isOnStock = checkIfMangaIsOnStock(storehouseDatabase, m);
            if(isOnStock) {
                message.append(getRuntimeMessage("Manga " + m.getName() + " Volume " + m.getVolume() + " is out of stock at the moment.\n"));
            } else {
                message.append(getRuntimeMessage("Manga " + m.getName() + " Volume " + m.getVolume() + " is available.\n"));
                BigDecimal totalQuantity = multiplyByMangaQuantity(m);
                totalCart = totalCart.add(totalQuantity);
            }
        }
        System.out.println("Total Cart of Client " + clientFound.getName() + " " + clientFound.getSurname() + " is " + totalCart + "€. Checking credit of the card...");
        clientDatabase.setRequest("/card/search?cardNumber=" + cardNumber);
        transactionCard = takeACardFromDatabase(clientDatabase);
        enoughCredit = controlEnoughBalance(transactionCard, totalCart);
        if (!enoughCredit) {
            message.append(getRuntimeMessage("Credit Card N° " + cardNumber + " has no enough credit for buy total cart " + totalCart + "€. Interrupting..."));
            return message;
        } else {
            System.out.println("Transaction in progress...");
            storehouseDatabase.setRequest("/stock/remove");
            finalizeTransaction(clientDatabase, storehouseDatabase, transactionCard, mangaCartClient, totalCart);
            shoppingCartDatabase.setRequest("/cart/clear?idClient=" + clientFound.getIdClient());
            String cartDeletionUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            restTemplate.delete(cartDeletionUrl);
            message.append(getRuntimeMessage("Shopping cart of " + clientFound.getName() + " " + clientFound.getSurname() + " removed successfully from database"));
        }
        message.append(getRuntimeMessage("Shopping cart buyed successful and credit has been scaled. Card used " + cardNumber));
        return message;
    }

    @Override
    public boolean controlEnoughBalance(Card card, BigDecimal totalCart) {
       BigDecimal credit = card.getBalance();
       return credit.compareTo(totalCart) >= 0;
    }

    @Override
    public Manga getAMangaFromDatabase(EndpointRequest storehouseDatabase, SearchManga searchManga) {
        try {
            String searchMangaUrl = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            return restTemplate.postForObject(searchMangaUrl, searchManga, Manga.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Manga not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving manga: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving manga: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving manga", e);
        }
    }

    @Override
    public Client getAClientFromDatabase(EndpointRequest clientDatabase, SearchClient searchClient) {
        try {
            String searchClientUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
            return restTemplate.postForObject(searchClientUrl, searchClient, Client.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Client not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving client: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving client: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving client", e);
        }
    }
    @Override
    public ShoppingCart getAShoppingCartFromDatabase(EndpointRequest shoppingCartDatabase) {
        try {
            String searchShoppingCartUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            return restTemplate.getForObject(searchShoppingCartUrl, ShoppingCart.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Client not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving shopping cart: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving shopping cart: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving shopping cart", e);
        }
    }
    @Override
    public Storehouse takeAStoreHouse(EndpointRequest storehouseDatabase, Manga manga) {
        try {
            String searchStockUrl = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            return restTemplate.postForObject(searchStockUrl, manga, Storehouse.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Stock not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving stock: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving stock: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving stock", e);
        }
    }
    @Override
    public Card takeACardFromDatabase(EndpointRequest clientDatabase) {
        try {
            String searchCardUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
            return restTemplate.getForObject(searchCardUrl, Card.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Stock not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving stock: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving stock: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving stock", e);
        }
    }
    @Override
    public boolean addMangaToCart(EndpointRequest shoppingCartDatabase, Client client, Manga manga) {
        try {
            boolean added = false;
            shoppingCartDatabase.setRequest("/cart/add/manga?idClient=" + client.getIdClient());
            String additionMangaUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            String response = restTemplate.postForObject(additionMangaUrl, manga, String.class);
            if(response.equalsIgnoreCase("Manga is added on Shopping Cart") ||
                response.equalsIgnoreCase("Manga is already present on shopping cart. Increased quantity.")) {
                added = true;
            }
            return added;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Stock not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving stock: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving stock: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving stock", e);
        }
    }
    @Override
    public String checkExistingCartOrCreateIt(EndpointRequest shoppingCartDatabase, Client client) {
        String message = "No Message";
        shoppingCartDatabase.setRequest("/cart/search?idClient=" + client.getIdClient());
        String searchUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
        String response = restTemplate.getForObject(searchUrl, String.class);
        if(response.contains("Cart not found")) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setIdClient(client.getIdClient());
            shoppingCart.setManga(new ArrayList<>());
            shoppingCartDatabase.setRequest("/cart/add");
            String additionUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            response = restTemplate.postForObject(additionUrl, shoppingCart, String.class);
            message = "Cart not found. Created for idClient " + client.getIdClient() + ".";
        } else {
            message = "Cart found for idClient " + client.getIdClient() + ".";
        }
        return message;
    }
    private void finalizeTransaction(EndpointRequest clientDatabase, EndpointRequest stockDatabase, Card card, List<Manga> manga, BigDecimal totalCart) {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        BigDecimal balance = card.getBalance();
        BigDecimal newImport = balance.subtract(totalCart);
        List<SearchManga> searchMangaList = new ArrayList<>();
        String registerUrl = clientDatabase.getEndpoint() + "/register/add";
        for(Manga m : manga) {
            searchMangaList.add(new SearchManga(m.getName(), m.getVolume()));
            JSONObject mangaJson = getJsonFromManga(m);
            System.out.println(m);
            String response = restTemplate.postForObject(registerUrl, mangaJson, String.class);
            System.out.println("Response for register " + response);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String request = "/card/update?newImport=" + newImport;
            String transactionUrl = clientDatabase.getEndpoint() + request;
            Card cardUpdated = restTemplate.postForObject(transactionUrl, card, Card.class);
            System.out.println("Card Updated with new import:\n" + cardUpdated);
            for(SearchManga sm : searchMangaList) {
                String requestBody = objectMapper.writeValueAsString(sm);
                String stockRemoveUrl = stockDatabase.getEndpoint() + stockDatabase.getRequest();
                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> responseEntity = restTemplate.exchange(stockRemoveUrl, HttpMethod.DELETE, requestEntity, String.class);
                System.out.println(responseEntity + "\nSuccessfully removed one manga " + sm.getName() + " Volume " + sm.getVolume() + " from the stock.");
            }
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Something was not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving some data: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving data: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving data", e);
        }
    }
    private JSONObject getJsonFromManga(Manga manga) {
        JSONObject json = new JSONObject();
        json.put("idManga", manga.getIdManga());
        json.put("name", manga.getName());
        json.put("volume", manga.getVolume());
        json.put("genre", manga.getGenre());
        json.put("author", manga.getAuthor());
        json.put("restricted", manga.getRestricted());
        json.put("publisher", manga.getPublisher());
        json.put("price", manga.getPrice());
        return json;
    }
    private boolean checkIfMangaIsOnStock(EndpointRequest storehouseDatabase, Manga manga) {
        try {
            String searchStockUrl = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            Storehouse storehouse = restTemplate.postForObject(searchStockUrl, manga, Storehouse.class);
            int quantity = storehouse.getQuantity();
            if(quantity <= 0 || manga.getQuantity() <= quantity) {
                System.out.println("Manga " + manga.getName() + " Volume " + manga.getVolume() + " is not available or haven't sufficient stock. Interrupting process.");
                return false;
            } else {
                System.out.println("Manga " + manga.getName() + " Volume " + manga.getVolume() + "is available. Current stock : " + storehouse.getQuantity());
                return true;
            }
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("Stock not found: {}", e.getResponseBodyAsString());
            } else {
                LOGGER.error("Error while retrieving stock: {} - {}", e.getStatusCode(), e.getStatusText());
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while retrieving stock: {}", e.getMessage());
            throw new RuntimeException("Error while retrieving stock", e);
        }
    }

    @Override
    public String checkIfEmailIsExisting(EndpointRequest clientDatabase, RequestLogin requestLogin) {
        String isOnDatabase;
        String clientUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        String response = restTemplate.postForObject(clientUrl, requestLogin, String.class);
        if (response.contains("Almeno uno dei parametri client è vuoto")) {
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
        System.out.println(manga.getName() + manga.getVolume());
        if(response.contains(manga.getName()) && response.contains(String.valueOf(manga.getVolume()))) {
            available = true;
        } else {
            storehouseDatabase.setRequest("/stock/out");
            urlRequest = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            response = restTemplate.getForObject(urlRequest, String.class);
            if(response.contains(manga.getName()) && response.contains(String.valueOf(manga.getVolume()))) {
                System.out.println("Manga " + manga.getName() + " Volume " + manga.getVolume() + " is not available");
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
    public boolean addCardAndVerifyIfIsAdded(EndpointRequest clientDatabase, long idClient, Card card) {
        boolean added = false;
        clientDatabase.setRequest("/card/add?idClient=" + idClient);
        String additionUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        String message = restTemplate.postForObject(additionUrl, card, String.class);
        added = !message.equalsIgnoreCase("Card already exists on database") && !message.contains("There is no client with id");
        return added;
    }
    private StringBuilder getRuntimeMessage(String message) {
        StringBuilder stringMessage = new StringBuilder();
        stringMessage.append(message);
        return stringMessage;
    }

    private BigDecimal multiplyByMangaQuantity(Manga manga) {
        BigDecimal price = BigDecimal.valueOf(manga.getPrice());
        BigDecimal quantity = BigDecimal.valueOf(manga.getQuantity());
        System.out.println("PRICE IS ${price} and its quantity is ${quantity}");
        return price.multiply(quantity);
    }

}
