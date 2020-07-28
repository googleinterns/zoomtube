package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.AuthenticationStatus;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.IOException;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AuthenticationServletTest {
  private AuthenticationServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private LocalServiceTestHelper authService;

  @Before
  public void setUp() throws ServletException {
    servlet = new AuthenticationServlet();
    servlet.init();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    authService = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    authService.setUp();
  }

  @After
  public void cleanUp() {
    authService.tearDown();
  }

  @Test
  public void doGet_loggedIn_expectTrue() throws ServletException, IOException {
    authService.setEnvIsLoggedIn(true);
    authService.setEnvAuthDomain("example.com");
    authService.setEnvEmail("test@example.com");

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isTrue();
  }

  @Test
  public void doGet_loggedIn_expectFalse() throws ServletException, IOException {
    authService.setEnvIsLoggedIn(false);
    authService.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isFalse();
  }

  @Test
  public void doGet_returnsUserEmail() throws ServletException, IOException {
    final String EMAIL = "test@example.com";
    authService.setEnvIsLoggedIn(true);
    authService.setEnvAuthDomain("example.com");
    authService.setEnvEmail(EMAIL);
    authService.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.user().get().getEmail()).isEqualTo(EMAIL);
  }

  @Test
  public void doGet_loggedIn_expectLogoutUrl() throws ServletException, IOException {
    authService.setEnvIsLoggedIn(true);
    authService.setEnvAuthDomain("example.com");
    authService.setEnvEmail("test@example.com");
    authService.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loginUrl().isPresent()).isFalse();
    assertThat(status.logoutUrl().isPresent()).isTrue();
  }

  @Test
  public void doGet_loggedOut_expectLoginUrl() throws ServletException, IOException {
    authService.setEnvIsLoggedIn(false);
    authService.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.logoutUrl().isPresent()).isFalse();
    assertThat(status.loginUrl().isPresent()).isTrue();
  }
}
