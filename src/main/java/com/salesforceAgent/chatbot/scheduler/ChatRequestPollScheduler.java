package com.salesforceAgent.chatbot.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChatRequestPollScheduler {

  @Autowired
  private MessagePollTask messagePollThread;

  @Scheduled(initialDelay = 10000, fixedDelay = 10000)
  public void executeAsynchronously() {

    messagePollThread.findMessages();

  }
}
