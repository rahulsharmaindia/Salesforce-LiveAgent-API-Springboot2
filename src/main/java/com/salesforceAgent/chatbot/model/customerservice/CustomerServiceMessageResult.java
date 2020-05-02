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
public class CustomerServiceMessageResult {
  private String id;
  private Boolean isAvailable;
}
