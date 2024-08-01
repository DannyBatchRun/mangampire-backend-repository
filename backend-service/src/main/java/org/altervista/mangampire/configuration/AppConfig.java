package org.altervista.mangampire.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.altervista.mangampire.dto.SearchManga;
import org.altervista.mangampire.request.EndpointRequest;
import org.altervista.mangampire.dto.SearchClient;
import org.springframework.http.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.*;

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
    public Map<String, EndpointRequest> services() {
        return new HashMap<>();
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
