package org.altervista.mangampire.configuration;

import org.altervista.mangampire.model.Client;
import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.dto.SearchManga;
import org.altervista.mangampire.dto.EndpointRequest;
import org.altervista.mangampire.dto.SearchClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public Map<Client, List<Manga>> shoppingCartList() { return new HashMap<>();}
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    @Scope("prototype")
    public EndpointRequest endpointRequest() {
        return new EndpointRequest();
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
