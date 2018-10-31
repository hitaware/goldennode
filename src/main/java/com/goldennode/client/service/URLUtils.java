package com.goldennode.client.service;

import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class URLUtils {
    private static final String BASE_URL = "https://thingabled.com";

    public <K> ResponseEntity<K> post(String url, Object request, Class<K> responseClass) throws IOException {
        SimpleClientHttpRequestFactory fac=new SimpleClientHttpRequestFactory();
        fac.setConnectTimeout(0);
        fac.setReadTimeout(0);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<K> foo =  restTemplate.postForEntity(url, request, responseClass);
        return foo;
    }
    
    
}
