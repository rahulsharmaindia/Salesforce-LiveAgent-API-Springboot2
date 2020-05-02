package com.salesforceAgent.chatbot.scheduler;

import com.salesforceAgent.chatbot.model.liveagent.LiveAgentMessageSequence;
import com.salesforceAgent.chatbot.model.liveagent.LiveAgentMessageType;
import com.salesforceAgent.chatbot.model.liveagent.LiveAgentSession;
import com.salesforceAgent.chatbot.service.SalesForceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class MessagePollTask {

  private static final Logger logger = LoggerFactory.getLogger(MessagePollTask.class);

  private static final String TRANSFER_TO_CUSTOMER_SERVICE_MESSAGE = "Siirrä asiakaspalveluun";

  private int liveAgentMessageSequence = -1;
  private long sendSequence = 2;
  private String chatId;

  private LiveAgentSession liveAgentSession;
  private LiveAgentMessageSequence liveAgentMessageSequences;

  @Autowired
  private SalesForceService force;

  @PostConstruct
  private void postConstruct() {
    liveAgentSession = force.initiateSalesForceSession();
  }

  public void findMessages() {
    liveAgentMessageSequences = force.getMessages(liveAgentSession,
            Integer.toString(liveAgentMessageSequence));

    if (liveAgentMessageSequences != null && liveAgentMessageSequences.getMessages() != null) {

      liveAgentMessageSequence = liveAgentMessageSequences.getSequence();

      List<LiveAgentMessageType> assignedWork = findMessagesByType(liveAgentMessageSequences,
              "Presence/WorkAssigned");

      if (assignedWork != null && assignedWork.size() > 0) {
        logger.info("Found new work: " + assignedWork.get(0).getMessage().toString());
        logger.info("Assigned work found, accepting it...");
        assignedWork.forEach(workItem -> force.acceptAssignedWork(liveAgentSession, workItem.getMessage()));

        liveAgentMessageSequences = force.getMessages(liveAgentSession,
                Integer.toString(liveAgentMessageSequence));

        List<LiveAgentMessageType> chatEstablished = findMessagesByType(liveAgentMessageSequences,
                "Agent/ChatEstablished");

        if (chatEstablished != null) {
          chatId = chatEstablished.get(0).getMessage().getChatId();
        }

      } else {
        logger.info("No new work assigned, searching for chat messages...");
        List<LiveAgentMessageType> chatMessages = findMessagesByType(liveAgentMessageSequences,
                "Conversational/ConversationMessage");
        if (chatMessages != null && chatMessages.size() > 0) {
          chatMessages.forEach(chatMessage -> {
            String chatMessageBody = chatMessage.getMessage().toString();
            logger.info(chatMessageBody);
            if (chatMessage.getMessage() != null && chatMessage.getMessage().getText().equals(TRANSFER_TO_CUSTOMER_SERVICE_MESSAGE)) {
              force.sendMessage(liveAgentSession, "Checking customer service availability",
                      sendSequence++, chatMessage.getMessage().getWorkId());
              boolean isCustomerServiceAvailable = force.isCustomerServiceAvailable();
              if (isCustomerServiceAvailable) {
                force.sendMessage(liveAgentSession, "Transfering the conversation to customer service",
                        sendSequence++, chatMessage.getMessage().getWorkId());
                force.transferChatToCustomerService(liveAgentSession, sendSequence++, chatId);
              } else {
                force.sendMessage(liveAgentSession, "Customer service is not currently available",
                        sendSequence++, chatMessage.getMessage().getWorkId());
              }
            } else {
              force.sendMessage(liveAgentSession, "http://www.google.com",
                      sendSequence++, chatMessage.getMessage().getWorkId());
            }
          });
        }
      }
    }
  }

  private List<LiveAgentMessageType> findMessagesByType(LiveAgentMessageSequence liveAgentMessageSequences, String searchedMessageType) {
    Predicate<LiveAgentMessageType> byWorkMessageType = messageType -> messageType.getType().equals(searchedMessageType);

    return liveAgentMessageSequences.getMessages().stream()
            .filter(byWorkMessageType).collect(Collectors.toList());
  }
}
