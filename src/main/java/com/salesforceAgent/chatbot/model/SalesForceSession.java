package com.salesforceAgent.chatbot.model;

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
public class SalesForceSession {
  private String id;
  @JsonProperty("issued_at")
  private long issued;
  @JsonProperty("instance_url")
  private String instanceUrl;
  private String signature;
  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("token_type")
  private String tokenType;
}
