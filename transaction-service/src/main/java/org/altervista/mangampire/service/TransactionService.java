package org.altervista.mangampire.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.altervista.mangampire.dto.SearchClient;
import org.altervista.mangampire.dto.SearchManga;
import org.altervista.mangampire.exception.InsufficientCreditException;
import org.altervista.mangampire.exception.NoCartItemsException;
import org.altervista.mangampire.model.*;
import org.altervista.mangampire.request.EndpointRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.*;

@Service
public class TransactionService {
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HttpHeaders headers;
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
        Boolean enoughCredit = verifySufficientCredit(cardNumber,transactionCard,totalCart);
        if(Boolean.TRUE.equals(enoughCredit)) {
            subtractBalanceCardWithTotalCart(transactionCard,totalCart);
            buyed = finalizeTransaction(services, transactionCard, shoppingCartClient.getManga(), totalCart);
        }
        clearCartClientAfterPurchased(services.get("shoppingCartDatabase"),clientFound,cardNumber,buyed);
        return buyed;
    }
    private boolean finalizeTransaction(Map<String, EndpointRequest> services, Card card, List<Manga> manga, BigDecimal totalCart) {
        boolean buyed = false;
        try {
            headers.setContentType(MediaType.APPLICATION_JSON);
            BigDecimal newImport = subtractTotalAndIncreaseCashRegisters(services.get("transactionDatabase"),card,totalCart,manga);
            Card cardUpdated = updateNewImportOfCard(services.get("clientDatabase"),card,newImport);
            logger.info("Full card updated is " + cardUpdated);
            logger.info("Card updated with newImport : " + newImport);
            List<SearchManga> mangaToRemoveFromStock = convertMangaToSearchManga(manga);
            services.get("storehouseDatabase").setRequest("/stock/remove");
            removeMangaFromStock(services.get("storehouseDatabase"),mangaToRemoveFromStock);
            buyed = true;
        } catch (HttpStatusCodeException e) {
            getNotFoundWarningParameter(e, "Something");
        } catch (Exception e) {
            launchAnErrorStatement(e, "Something");
        }
        return buyed;
    }
    private Card takeACardFromDatabase(EndpointRequest clientDatabase) {
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
    private Client getAClientFromDatabase(EndpointRequest clientDatabase, SearchClient searchClient) {
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
    private Boolean verifySufficientCredit(String cardNumber, Card transactionCard, BigDecimal totalCart) throws InsufficientCreditException {
        Boolean enoughCredit = false;
        enoughCredit = controlEnoughBalance(transactionCard, totalCart);
        if (!enoughCredit) {
            logger.warn("Credit Card N° " + cardNumber + " has no enough credit for buy total cart " + totalCart + "€. Interrupting...");
            throw new InsufficientCreditException();
        } else {
            logger.info("Credit Card is available. A Transaction is in progress.");
        }
        return enoughCredit;
    }
    private Boolean controlEnoughBalance(Card card, BigDecimal totalCart) {
        BigDecimal credit = card.getBalance();
        return credit.compareTo(totalCart) >= 0;
    }
    private BigDecimal subtractTotalAndIncreaseCashRegisters(EndpointRequest transactionDatabase, Card card, BigDecimal totalCart, List<Manga> manga) {
        BigDecimal newImport = subtractBalanceCardWithTotalCart(card,totalCart);
        transactionDatabase.setRequest("/register/add");
        String registerUrl = transactionDatabase.getEndpoint() + transactionDatabase.getRequest();
        List<SearchManga> searchMangaList = convertMangaToSearchManga(manga);
        for(Manga m : manga) {
            String response = restTemplate.postForObject(registerUrl, m, String.class);
            logger.info("Response from Transaction Service : " + response);
        }
        return newImport;
    }
    private BigDecimal subtractBalanceCardWithTotalCart(Card card, BigDecimal totalCart) {
        BigDecimal balance = card.getBalance();
        logger.info("Current balance is " + balance);
        return balance.subtract(totalCart);
    }
    private Card updateNewImportOfCard(EndpointRequest clientDatabase, Card card, BigDecimal newImport) {
        clientDatabase.setRequest("/card/update?newImport=" + newImport);
        String transactionUrl = clientDatabase.getEndpoint() + clientDatabase.getRequest();
        return restTemplate.postForObject(transactionUrl, card, Card.class);
    }
    private List<SearchManga> convertMangaToSearchManga(List<Manga> manga) {
        List<SearchManga> searchMangaList = new ArrayList<>();
        for(Manga m : manga) {
            searchMangaList.add(new SearchManga(m.getName(), m.getVolume()));
        }
        return searchMangaList;
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
    private BigDecimal multiplyByMangaQuantity(Manga manga) {
        BigDecimal price = BigDecimal.valueOf(manga.getPrice());
        BigDecimal quantity = BigDecimal.valueOf(manga.getQuantity());
        return price.multiply(quantity);
    }
    private void checkShoppingCartOrCardsAreEmpty(Client client, ShoppingCart shoppingCartClient) {
        if (shoppingCartClient == null || shoppingCartClient.getManga().isEmpty()) {
            logger.warn("Client " + client.getName() + " " + client.getSurname() + " has shopping Cart Empty.");
        } else if (client.getCardQuantity() <= 0) {
            logger.warn("Client " + client.getName() + " " + client.getSurname() + " has no cards.");
        }
    }
    private void removeMangaFromStock(EndpointRequest storehouseDatabase, List<SearchManga> searchMangaList) throws JsonProcessingException {
        for(SearchManga sm : searchMangaList) {
            String requestBody = objectMapper.writeValueAsString(sm);
            String stockRemoveUrl = storehouseDatabase.getEndpoint() + storehouseDatabase.getRequest();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(stockRemoveUrl, HttpMethod.DELETE, requestEntity, String.class);
            String response = responseEntity.toString(); response = response.substring(0, response.indexOf("IDManga"));
            logger.info("Response from Storehouse Service : " + response.trim());
        }
    }
    private void clearCartClientAfterPurchased(EndpointRequest shoppingCartDatabase, Client client, String cardNumber, boolean buyed) {
        if(buyed) {
            shoppingCartDatabase.setRequest("/cart/clear?idClient=" + client.getIdClient());
            String cartDeletionUrl = shoppingCartDatabase.getEndpoint() + shoppingCartDatabase.getRequest();
            restTemplate.delete(cartDeletionUrl);
            logger.info("Shopping cart of " + client.getName() + " " + client.getSurname() + " removed successfully from database");
            logger.info("Shopping cart bought successful and credit has been scaled. Card used " + cardNumber);
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
