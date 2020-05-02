package com.salesforceAgent.chatbot.model.liveagent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class LiveAgentPresenceStatus {
  @JsonProperty("Id")
  private String id;
  @JsonProperty("DeveloperName")
  private String deleloperName;
}
