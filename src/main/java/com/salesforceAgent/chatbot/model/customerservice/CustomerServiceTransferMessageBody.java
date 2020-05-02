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
public class CustomerServiceTransferMessageBody {
  private String agentWhisper;
  private String chatId;
  private String destination;
  private String type;
}
