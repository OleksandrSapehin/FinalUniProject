package org.example.questionsservise.сonfig;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class YouTubeConfig {

    static String YOUTUBE_V3_API = "https://www.googleapis.com/youtube/v3";

    @Bean
    public HttpTransport httpTransport() throws Exception {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public GsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager clientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2.setDefaultClientRegistrationId("google");

        return WebClient.builder()
                .baseUrl(YOUTUBE_V3_API)
                .apply(oauth2.oauth2Configuration())
                .build();
    }

  @Bean
  public YouTube youtube(HttpTransport httpTransport, GsonFactory gsonFactory, OAuth2AuthorizedClientService clientService) {
      return new YouTube.Builder(httpTransport, gsonFactory, request -> {
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
              OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                      oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
              if (client != null && client.getAccessToken() != null) {
                  request.getHeaders().setAuthorization("Bearer " + client.getAccessToken().getTokenValue());
              }
          }
      }).setApplicationName("question-api").build();
  }


}
