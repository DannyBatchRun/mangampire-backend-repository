package org.altervista.mangampire.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.altervista.mangampire.exception.InsufficientCreditException;
import org.altervista.mangampire.exception.NoCartItemsException;
import org.altervista.mangampire.model.*;
import org.altervista.mangampire.productdto.SearchManga;
import org.altervista.mangampire.request.EndpointRequest;
import org.altervista.mangampire.login.RequestLogin;
import org.altervista.mangampire.productdto.SearchClient;
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
    private static final Logger logger = LoggerFactory.getLogger(BackendService.class);
    @Autowired
    private RestTemplate restTemplate;
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
    public boolean completeTransaction (SearchClient client, String cardNumber, Map<String, EndpointRequest> services) {
        boolean buyed = false;
        services.get("clientDatabase").setRequest("/client/search?showPasswordToken=no");
        Client clientFound = getAClientFromDatabase(services.get("clientDatabase"), client);
        services.get("shoppingCartDatabase").setRequest("/cart/search?idClient=" + clientFound.getIdClient());
        String searchShoppingCart = services.get("shoppingCartDatabase").getEndpoint() + services.get("shoppingCartDatabase").getRequest();
        ShoppingCart shoppingCartClient = restTemplate.getForObject(searchShoppingCart, ShoppingCart.class);
        checkShoppingCartOrCardsAreEmpty(clientFound,shoppingCartClient);
        services.get("storehouseDatabase").setRequest("/stock/search");
        BigDecimal totalCart = calculateCartTotal(shoppingCartClient.getManga(),services.get("storehouseDatabase"),clientFound);
        services.get("clientDatabase").setRequest("/card/search?cardNumber=" + cardNumber);
        Card transactionCard = takeACardFromDatabase(services.get("clientDatabase"));
        verifySufficientCredit(cardNumber,transactionCard, totalCart);
        services.get("storehouseDatabase").setRequest("/stock/remove");
        buyed = finalizeTransaction(services, transactionCard, shoppingCartClient.getManga(), totalCart);
        if(buyed) {
            services.get("shoppingCartDatabase").setRequest("/cart/clear?idClient=" + clientFound.getIdClient());
            String cartDeletionUrl = services.get("shoppingCartDatabase").getEndpoint() + services.get("shoppingCartDatabase").getRequest();
            restTemplate.delete(cartDeletionUrl);
            logger.info("Shopping cart of " + clientFound.getName() + " " + clientFound.getSurname() + " removed successfully from database");
            logger.info("Shopping cart bought successful and credit has been scaled. Card used " + cardNumber);
        }
        return buyed;
    }
    @Override
    public boolean controlEnoughBalance(Card card, BigDecimal totalCart) {
       BigDecimal credit = card.getBalance();
       return credit.compareTo(totalCart) >= 0;
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
    public Card takeACardFromDatabase(EndpointRequest clientDatabase) {
        Card cardFound = new Card();
        try {
            String searchCardUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
            cardFound = restTemplate.getForObject(searchCardUrl, Card.class);
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Card");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Card");
        }
        return cardFound;
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
            message = "Cart not found. Created for idClient " + client.getIdClient() + ".";
        } else {
            message = "Cart found for idClient " + client.getIdClient() + ".";
        }
        return message;
    }
    private boolean finalizeTransaction(Map<String, EndpointRequest> services, Card card, List<Manga> manga, BigDecimal totalCart) {
        boolean buyed = false; ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders(); BigDecimal balance = card.getBalance();
        logger.info("Current balance is " + balance);
        BigDecimal newImport = balance.subtract(totalCart);
        List<SearchManga> searchMangaList = new ArrayList<>();
        String registerUrl = services.get("transactionDatabase").getEndpoint() + "/register/add";
        for(Manga m : manga) {
            searchMangaList.add(new SearchManga(m.getName(), m.getVolume()));
            String response = restTemplate.postForObject(registerUrl, m, String.class);
            logger.info(response);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            services.get("clientDatabase").setRequest("/card/update?newImport=" + newImport);
            String transactionUrl = services.get("clientDatabase").getEndpoint() + services.get("clientDatabase").getRequest();
            Card cardUpdated = restTemplate.postForObject(transactionUrl, card, Card.class);
            logger.info("Full card updated is " + cardUpdated);
            logger.info("Card updated with newImport : " + newImport);
            for(SearchManga sm : searchMangaList) {
                String requestBody = objectMapper.writeValueAsString(sm);
                String stockRemoveUrl = services.get("storehouseDatabase").getEndpoint() + services.get("storehouseDatabase").getRequest();
                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> responseEntity = restTemplate.exchange(stockRemoveUrl, HttpMethod.DELETE, requestEntity, String.class);
                String response = responseEntity.toString(); response = response.substring(0, response.indexOf("IDManga"));
                logger.info("Response from Storehouse Service : " + response.trim());
            }
            buyed = true;
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Something");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Something");
        }
        return buyed;
    }
    private boolean checkIfMangaIsOnStock(EndpointRequest storehouseDatabase, Manga manga) {
        boolean inStock = false;
        try {
            String searchStockUrl = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            Storehouse storehouse = restTemplate.postForObject(searchStockUrl, manga, Storehouse.class);
            int quantity = storehouse.getQuantity();
            if(quantity <= 0 || manga.getQuantity() <= quantity) {
                logger.error("Manga " + manga.getName() + " Volume " + manga.getVolume() + " is not in stock. Interrupting process.");
            } else {
                logger.info("Manga " + manga.getName() + " Volume " + manga.getVolume() + "is available. Current stock : " + storehouse.getQuantity());
                inStock = true;
            }
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Stock");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Stock");
        }
        return inStock;
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
    public boolean addCardAndVerifyIfIsAdded(EndpointRequest clientDatabase, long idClient, Card card) {
        boolean added = false;
        clientDatabase.setRequest("/card/add?idClient=" + idClient);
        String additionUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        String message = restTemplate.postForObject(additionUrl, card, String.class);
        added = !message.equalsIgnoreCase("Card already exists on database") && !message.contains("There is no client with id");
        return added;
    }
    private BigDecimal calculateCartTotal(List<Manga> mangaCartClient, EndpointRequest storehouseDatabase, Client client) throws NoCartItemsException {
        boolean isOnStock = false;
        BigDecimal totalCart = new BigDecimal("0.0");
        if(mangaCartClient == null || mangaCartClient.isEmpty()) {
            logger.error("There is no manga to buy for client " + client.getName() + " " + client.getSurname());
            throw new NoCartItemsException();
        } else {
            for(Manga m : mangaCartClient) {
                logger.info("Check if " + m.getName() + " Volume " + m.getVolume() + " is available on stock...");
                isOnStock = checkIfMangaIsOnStock(storehouseDatabase, m);
                if(isOnStock) {
                    logger.warn("Manga " + m.getName() + " Volume " + m.getVolume() + " is out of stock at the moment.");
                } else {
                    logger.info("Manga " + m.getName() + " Volume " + m.getVolume() + " is available.");
                    BigDecimal totalQuantity = multiplyByMangaQuantity(m);
                    totalCart = totalCart.add(totalQuantity);
                }
            }
        }
        logger.info("Total Cart of Client " + client.getName() + " " + client.getSurname() + " is " + totalCart + "€. Checking credit of the card...");
        return totalCart;
    }
    private void verifySufficientCredit(String cardNumber, Card transactionCard, BigDecimal totalCart) throws InsufficientCreditException {
        boolean enoughCredit = controlEnoughBalance(transactionCard, totalCart);
        if (!enoughCredit) {
            logger.warn("Credit Card N° " + cardNumber + " has no enough credit for buy total cart " + totalCart + "€. Interrupting...");
            throw new InsufficientCreditException();
        } else {
            logger.warn("Credit Card is available. A Transaction is in progress.");
        }
    }
    private BigDecimal multiplyByMangaQuantity(Manga manga) {
        BigDecimal price = BigDecimal.valueOf(manga.getPrice());
        BigDecimal quantity = BigDecimal.valueOf(manga.getQuantity());
        return price.multiply(quantity);
    }
    private void checkShoppingCartOrCardsAreEmpty(Client client, ShoppingCart shoppingCartClient) {
        if(shoppingCartClient == null || shoppingCartClient.getManga().isEmpty()) {
            logger.warn("Client " + client.getName() + " " + client.getSurname() + " has shopping Cart Empty.");
        } else if (client.getCardQuantity() <= 0) {
            logger.warn("Client " + client.getName() + " " + client.getSurname() + " has no cards.");
        }
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
