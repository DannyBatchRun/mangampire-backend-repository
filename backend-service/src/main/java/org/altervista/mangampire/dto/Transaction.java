package org.altervista.mangampire.dto;

import lombok.Data;
import lombok.ToString;
import org.altervista.mangampire.request.EndpointRequest;

import java.util.Map;

@Data
@ToString
public class Transaction {

    private String cardNumber;
    private SearchClient client;
    private Map<String, EndpointRequest> services;
    public Transaction() {

    }
    public Transaction(String cardNumber, SearchClient client, Map<String, EndpointRequest> services) {
        this.cardNumber = cardNumber;
        this.client = client;
        this.services = services;
    }
}
