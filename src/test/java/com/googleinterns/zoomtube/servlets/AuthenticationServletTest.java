package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.AuthenticationStatus;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;

public class AuthenticationServletTest {
  private AuthenticationServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private LocalServiceTestHelper authServiceHelper;

  @Before
  public void setUp() throws ServletException {
    servlet = new AuthenticationServlet();
    servlet.init();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    authServiceHelper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    authServiceHelper.setUp();
  }

  @After
  public void cleanUp() {
    authServiceHelper.tearDown();
  }

  @Test
  public void doGet_loggedIn_expectTrue() throws ServletException, IOException {
    authServiceHelper.setEnvIsLoggedIn(true);
    authServiceHelper.setEnvAuthDomain("example.com");
    authServiceHelper.setEnvEmail("test@example.com");

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isTrue();
  }

  @Test
  public void doGet_loggedIn_expectFalse() throws ServletException, IOException {
    authServiceHelper.setEnvIsLoggedIn(false);
    authServiceHelper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isFalse();
  }

  @Test
  public void doGet_returnsUserEmail() throws ServletException, IOException {
    final String EMAIL = "test@example.com";
    authServiceHelper.setEnvIsLoggedIn(true);
    authServiceHelper.setEnvAuthDomain("example.com");
    authServiceHelper.setEnvEmail(EMAIL);
    authServiceHelper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.user().get().getEmail()).isEqualTo(EMAIL);
  }

  @Test
  public void doGet_urls_whenloggedIn() throws ServletException, IOException {
    authServiceHelper.setEnvIsLoggedIn(true);
    authServiceHelper.setEnvAuthDomain("example.com");
    authServiceHelper.setEnvEmail("test@example.com");
    authServiceHelper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loginUrl().isPresent());
    assertThat(!status.logoutUrl().isPresent());
  }

  @Test
  public void doGet_urls_whenloggedOut() throws ServletException, IOException {
    authServiceHelper.setEnvIsLoggedIn(false);
    authServiceHelper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.logoutUrl().isPresent());
    assertThat(!status.loginUrl().isPresent());
  }

}
