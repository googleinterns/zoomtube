package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.AuthenticationStatus;
import com.googleinterns.zoomtube.mocks.MockRequest;
import com.googleinterns.zoomtube.mocks.MockResponse;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.IOException;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthenticationServletTest {
  private AuthenticationServlet servlet;
  private MockRequest request;
  private MockResponse response;
  private LocalServiceTestHelper testServices;

  @Before
  public void setUp() throws ServletException, IOException {
    servlet = new AuthenticationServlet();
    servlet.init();
    testServices = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    testServices.setUp();
    response = new MockResponse();
    request = new MockRequest();
  }

  @After
  public void cleanUp() {
    testServices.tearDown();
  }

  @Test
  public void doGet_loggedIn_expectTrue() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    testServices.setEnvAuthDomain("example.com");
    testServices.setEnvEmail("test@example.com");

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isTrue();
  }

  @Test
  public void doGet_loggedIn_expectFalse() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(false);

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
    testServices.setEnvIsLoggedIn(true);
    testServices.setEnvAuthDomain("example.com");
    testServices.setEnvEmail(EMAIL);

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.user().get().getEmail()).isEqualTo(EMAIL);
  }

  @Test
  public void doGet_loggedIn_expectLogoutUrl() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    testServices.setEnvAuthDomain("example.com");
    testServices.setEnvEmail("test@example.com");

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
    testServices.setEnvIsLoggedIn(false);

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.logoutUrl().isPresent()).isFalse();
    assertThat(status.loginUrl().isPresent()).isTrue();
  }
}
