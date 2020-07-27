package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import javax.servlet.ServletException;
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

  @Before
  public void setUp() throws ServletException {
    servlet = new AuthenticationServlet();
    servlet.init();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  public void doGet_loggedIn_true() throws ServletException, IOException {
    LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvIsLoggedIn(true)
          .setEnvAuthDomain("example.com")
          .setEnvEmail("test@example.com");
    helper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isTrue();

    helper.tearDown();
  }

  @Test
  public void doGet_loggedIn_false() throws ServletException, IOException {
    LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvIsLoggedIn(false);
    helper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isFalse();

    helper.tearDown();
  }

  @Test
  public void doGet_user_email() throws ServletException, IOException {
    final String EMAIL = "test@example.com";
    LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsLoggedIn(true)
        .setEnvAuthDomain("example.com")
        .setEnvEmail(EMAIL);
    helper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.user().get().getEmail()).isEqualTo(EMAIL);

    helper.tearDown();
  }

  @Test
  public void doGet_loggedInUrls() throws ServletException, IOException {
    LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsLoggedIn(true)
        .setEnvAuthDomain("example.com")
        .setEnvEmail("test@example.com");
    helper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loginUrl().isPresent());
    assertThat(status.logoutUrl().isEmpty());

    helper.tearDown();
  }

  @Test
  public void doGet_loggedOutUrls() throws ServletException, IOException {
    LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
        .setEnvIsLoggedIn(false);
    helper.setUp();

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
        .create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.logoutUrl().isPresent());
    assertThat(status.loginUrl().isEmpty());

    helper.tearDown();
  }

}
