package com.salesforceAgent.chatbot.model.liveagent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class LiveAgentCustomEventBody {
  private String type;
  private String data;
}
