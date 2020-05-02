package com.salesforceAgent.chatbot.model.liveagent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class LiveAgentPresence {
  private String organizationId;
  @JsonProperty("sfdcSessionId")
  private String accessToken;
  @JsonProperty("channelIdsWithParam")
  private List<LiveAgentChannelId> channelIds;
  private String statusId;
}
