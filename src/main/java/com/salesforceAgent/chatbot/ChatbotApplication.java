package com.salesforceAgent.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ChatbotApplication {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  //Override timeouts in request factory
  @Bean
  public SimpleClientHttpRequestFactory getClientHttpRequestFactory()
  {
    SimpleClientHttpRequestFactory clientHttpRequestFactory
            = new SimpleClientHttpRequestFactory();
    //Connect timeout
    clientHttpRequestFactory.setConnectTimeout(40_000);

    //Read timeout
    clientHttpRequestFactory.setReadTimeout(40_000);
    return clientHttpRequestFactory;
  }

  public static void main(String[] args) {
    SpringApplication.run(ChatbotApplication.class, args);
  }

}