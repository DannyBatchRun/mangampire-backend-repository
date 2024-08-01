package org.altervista.mangampire.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.altervista.mangampire.dto.*;
import org.altervista.mangampire.request.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }
    @Bean
    public HttpHeaders headers() { return new HttpHeaders(); }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public SearchClient searchClient() {
        return new SearchClient();
    }
    @Bean
    public SearchManga searchManga() {
        return new SearchManga();
    }

}
