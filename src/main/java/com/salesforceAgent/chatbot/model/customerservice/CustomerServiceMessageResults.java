package com.salesforceAgent.chatbot.model.customerservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CustomerServiceMessageResults {
  private List<CustomerServiceMessageResult> results;

}
