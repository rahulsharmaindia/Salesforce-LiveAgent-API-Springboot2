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
public class LiveAgentMessage {
  private String text;
  private long sequence;
  private String workId;
  private String workTargetId;
  private String messageId;
  private String type;
  private String chatId;
}
