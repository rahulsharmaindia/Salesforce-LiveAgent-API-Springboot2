package com.salesforceAgent.chatbot.model.customerservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CustomerServiceMessage {
  private String type;
  private CustomerServiceMessageResults message;
}
