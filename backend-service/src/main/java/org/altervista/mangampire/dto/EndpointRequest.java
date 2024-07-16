package org.altervista.mangampire.dto;

import lombok.Data;

@Data
public class EndpointRequest {

    private String endpoint;
    private String request;

    public EndpointRequest() {

    }

    public EndpointRequest(String endpoint, String request) {
        this.endpoint = endpoint;
        this.request = request;
    }
}
