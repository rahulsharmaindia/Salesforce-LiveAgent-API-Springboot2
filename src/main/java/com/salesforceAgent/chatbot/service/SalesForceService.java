package com.salesforceAgent.chatbot.service;

import com.salesforceAgent.chatbot.model.SalesForceSession;
import com.salesforceAgent.chatbot.model.customerservice.CustomerServiceAvailabilityMessages;
import com.salesforceAgent.chatbot.model.customerservice.CustomerServiceTransferMessageBody;
import com.salesforceAgent.chatbot.model.liveagent.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SalesForceService {
  private static final String REST_VERSION = "46.0";
  private static final String LIVEAGENT_API_VERSION = "43";
  private static final String LIVEAGENT_AFFINITY_HEADER_NAME = "X-LIVEAGENT-AFFINITY";
  private static final String LIVEAGENT_API_VERSION_HEADER_NAME = "X-LIVEAGENT-API-VERSION";
  private static final String LIVEAGENT_SESSION_KEY_HEADER_NAME = "X-LIVEAGENT-SESSION-KEY";
  private static final String LIVEAGENT_SEQUENCE_HEADER_NAME = "X-LIVEAGENT-SEQUENCE";

  @Value("${security.oauth2.client.accessTokenUri}")
  private String accessTokenUrl;
  @Value("${security.oauth2.client.grantType}")
  private String grantType;
  @Value("${security.oauth2.client.clientId}")
  private String clientId;
  @Value("${security.oauth2.client.clientSecret}")
  private String clientSecret;
  @Value("${security.oauth2.client.username}")
  private String username;
  @Value("${security.oauth2.client.password}")
  private String password;

  @Value("${salesforce.liveagent.deploymentUrl}")
  private String liveAgentDeploymentUrl;
  @Value("${salesforce.liveagent.presenceStatusKey}")
  private String liveAgentPresenceStatusKey;
  @Value("${salesforce.liveagent.presenceStatusId}")
  private String liveAgentPresenceStatusId;
  @Value("${salesforce.liveagent.organisationId}")
  private String liveAgentOrganisationId;
  @Value("${salesforce.customerService.deploymentId}")
  private String customerServiceDeploymentId;
  @Value("${salesforce.customerService.availabilityId}")
  private String customerServiceAvailabilityId;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private RestTemplate restTemplate;

  private LiveAgentSession createLiveAgentSession() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(LIVEAGENT_AFFINITY_HEADER_NAME, "null");
    headers.set(LIVEAGENT_API_VERSION_HEADER_NAME, LIVEAGENT_API_VERSION);

    HttpEntity<String> entity = new HttpEntity<String>("body", headers);

    ResponseEntity<LiveAgentSession> liveAgentSession = restTemplate.exchange(liveAgentDeploymentUrl +
                    "/chat/rest/System/SessionId", HttpMethod.GET, entity, LiveAgentSession.class);

    return liveAgentSession.getBody();
  }

  private LiveAgentPresenceStatus getLiveAgentPresenceStatus(String instanceUrl, String accessToken) {
    String url = instanceUrl + "/services/data/v{version}/query/?q={q}";
    url = url.replace("{version}", REST_VERSION);
    String statusSearchString = "SELECT Id, DeveloperName FROM ServicePresenceStatus WHERE DeveloperName = '{name}'";

    Map<String, String> params = new HashMap<>();

    params.put("q", statusSearchString.replace("{name}", liveAgentPresenceStatusKey));

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    ResponseEntity<LiveAgentPresenceStatus> status = restTemplate.exchange(url, HttpMethod.GET, entity,
            LiveAgentPresenceStatus.class, params);

    return status.getBody();
  }

  private SalesForceSession loginIntoSalesForce() {
    Map<String, String> headers = new HashMap<>();
    headers.put("grant_type", grantType);
    headers.put("client_id", clientId);
    headers.put("client_secret", clientSecret);
    headers.put("username", username);
    headers.put("password", password);

    String url = accessTokenUrl + "?grant_type={grant_type}&client_id={client_id}&client_secret={client_secret}&username={username}&password={password}";
    SalesForceSession session = restTemplate.postForObject(url, null, SalesForceSession.class, headers);
    return session;
  }

  private void setLiveAgentPresenceStatus(LiveAgentSession liveAgentSession, String liveAgentAccessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(LIVEAGENT_AFFINITY_HEADER_NAME, liveAgentSession.getAffinityToken());
    headers.set(LIVEAGENT_API_VERSION_HEADER_NAME, LIVEAGENT_API_VERSION);
    headers.set(LIVEAGENT_SESSION_KEY_HEADER_NAME, liveAgentSession.getKey());

    List<LiveAgentChannelId> channelIds = new ArrayList<>();
    channelIds.add(0, new LiveAgentChannelId("agent"));
    channelIds.add(1, new LiveAgentChannelId("conversational"));

    LiveAgentPresence presence = new LiveAgentPresence(liveAgentOrganisationId, liveAgentAccessToken, channelIds,
            liveAgentPresenceStatusId);

    HttpEntity<LiveAgentPresence> entity = new HttpEntity<>(presence, headers);

    String presenceAcknowledged = restTemplate.postForObject(liveAgentDeploymentUrl + "/chat/rest/Presence/PresenceLogin",
            entity, String.class);
  }

  public LiveAgentSession initiateSalesForceSession() {
    SalesForceSession salesForceSession = loginIntoSalesForce();
    LiveAgentSession liveAgentSession = createLiveAgentSession();
    String liveAgentAccessToken = salesForceSession.getAccessToken();

    setLiveAgentPresenceStatus(liveAgentSession, liveAgentAccessToken);

    return liveAgentSession;
  }

  public LiveAgentMessageSequence getMessages(LiveAgentSession liveAgentSession, String messageSequence) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(LIVEAGENT_AFFINITY_HEADER_NAME, liveAgentSession.getAffinityToken());
    headers.set(LIVEAGENT_API_VERSION_HEADER_NAME, LIVEAGENT_API_VERSION);
    headers.set(LIVEAGENT_SESSION_KEY_HEADER_NAME, liveAgentSession.getKey());

    HttpEntity<String> entity = new HttpEntity<String>(null, headers);

    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("ack", messageSequence);
    queryParameters.put("pc", "0");

    String url = liveAgentDeploymentUrl + "/chat/rest/System/Messages?ack={ack}&pc={pc}";

    ResponseEntity<LiveAgentMessageSequence> workMessages = restTemplate.exchange(url, HttpMethod.GET, entity,
            LiveAgentMessageSequence.class, queryParameters);

    return workMessages.getBody();
  }

  public void acceptAssignedWork(LiveAgentSession liveAgentSession, LiveAgentMessage liveAgentWorkMessage) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(LIVEAGENT_AFFINITY_HEADER_NAME, liveAgentSession.getAffinityToken());
    headers.set(LIVEAGENT_API_VERSION_HEADER_NAME, LIVEAGENT_API_VERSION);
    headers.set(LIVEAGENT_SESSION_KEY_HEADER_NAME, liveAgentSession.getKey());
    headers.set(LIVEAGENT_SEQUENCE_HEADER_NAME, "1");

    LiveAgentWorkDetails workDetails = new LiveAgentWorkDetails(liveAgentWorkMessage.getWorkId(),
            liveAgentWorkMessage.getWorkTargetId());

    HttpEntity<LiveAgentWorkDetails> entity = new HttpEntity<>(workDetails, headers);

    String presenceAcknowledged = restTemplate.postForObject(liveAgentDeploymentUrl + "/chat/rest/Presence/AcceptWork",
            entity, String.class);
  }

  public void sendMessage(LiveAgentSession liveAgentSession, String message, long sequence, String workId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(LIVEAGENT_AFFINITY_HEADER_NAME, liveAgentSession.getAffinityToken());
    headers.set(LIVEAGENT_API_VERSION_HEADER_NAME, LIVEAGENT_API_VERSION);
    headers.set(LIVEAGENT_SESSION_KEY_HEADER_NAME, liveAgentSession.getKey());
    headers.set(LIVEAGENT_SEQUENCE_HEADER_NAME, Long.toString(sequence));
    log.info("Afffinity token------"+liveAgentSession.getAffinityToken());
    log.info("session key ------"+liveAgentSession.getKey());

    LiveAgentMessageBody messageBody = new LiveAgentMessageBody("agent", message, workId);

    HttpEntity<LiveAgentMessageBody> entity = new HttpEntity<>(messageBody, headers);

    String sendAcknowledged = restTemplate.postForObject(liveAgentDeploymentUrl + "/chat/rest/Conversational/ConversationMessage",
            entity, String.class);

    log.info("Sent a message, response: " + sendAcknowledged);
  }

  public boolean isCustomerServiceAvailable() {
    boolean isAvailable = false;
    HttpHeaders headers = new HttpHeaders();
    headers.set(LIVEAGENT_API_VERSION_HEADER_NAME, LIVEAGENT_API_VERSION);

    HttpEntity<String> entity = new HttpEntity<String>(null, headers);

    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("org_id", liveAgentOrganisationId);
    queryParameters.put("deployment_id", customerServiceDeploymentId);
    queryParameters.put("button_id", customerServiceAvailabilityId);

    String url = liveAgentDeploymentUrl + "/chat/rest/Visitor/Availability?org_id={org_id}&deployment_id=" +
            "{deployment_id}&Availability.ids={button_id}";

    ResponseEntity<CustomerServiceAvailabilityMessages> customerServiceAvailability = restTemplate.exchange(url, HttpMethod.GET, entity,
            CustomerServiceAvailabilityMessages.class, queryParameters);

    return customerServiceAvailability.getBody().getMessages().get(0).getMessage().getResults().get(0).getIsAvailable();
  }

  public void transferChatToCustomerService(LiveAgentSession liveAgentSession, long sequence, String chatId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(LIVEAGENT_AFFINITY_HEADER_NAME, liveAgentSession.getAffinityToken());
    headers.set(LIVEAGENT_API_VERSION_HEADER_NAME, LIVEAGENT_API_VERSION);
    headers.set(LIVEAGENT_SESSION_KEY_HEADER_NAME, liveAgentSession.getKey());
    headers.set(LIVEAGENT_SEQUENCE_HEADER_NAME, Long.toString(sequence));

    CustomerServiceTransferMessageBody messageBody = new CustomerServiceTransferMessageBody("", chatId,
            customerServiceAvailabilityId, "button");

    HttpEntity<CustomerServiceTransferMessageBody> entity = new HttpEntity<>(messageBody, headers);

    String sendAcknowledged = restTemplate.postForObject(liveAgentDeploymentUrl + "/chat/rest/Agent/TransferRequest",
            entity, String.class);

    log.info("Transferred the chat to customer service, response: " + sendAcknowledged);
  }
}
