package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.AuthenticationStatus;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AuthenticationServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private AuthenticationServlet servlet;
  private LocalServiceTestHelper testServices =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  private static final String EMAIL = "test@example.com";

  @Before
  public void setUp() throws Exception {
    testServices.setUp();
    testServices.setEnvAuthDomain("example.com");
    testServices.setEnvEmail(EMAIL);
    servlet = new AuthenticationServlet();
    servlet.init();
  }

  @After
  public void cleanUp() {
    testServices.tearDown();
  }

  @Test
  public void doGet_loggedIn_expectTrue() throws Exception {
    testServices.setEnvIsLoggedIn(true);
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json;");
    String json = content.toString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isTrue();
  }

  @Test
  public void doGet_loggedIn_expectFalse() throws Exception {
    testServices.setEnvIsLoggedIn(false);
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json;");
    String json = content.toString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loggedIn()).isFalse();
  }

  @Test
  public void doGet_returnsUserEmail() throws Exception {
    testServices.setEnvIsLoggedIn(true);
    testServices.setEnvEmail(EMAIL);
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json;");
    String json = content.toString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.user().get().getEmail()).isEqualTo(EMAIL);
  }

  @Test
  public void doGet_loggedIn_expectLogoutUrl() throws Exception {
    testServices.setEnvIsLoggedIn(true);
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json;");
    String json = content.toString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.loginUrl().isPresent()).isFalse();
    assertThat(status.logoutUrl().isPresent()).isTrue();
  }

  @Test
  public void doGet_loggedOut_expectLoginUrl() throws Exception {
    testServices.setEnvIsLoggedIn(false);
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json;");
    String json = content.toString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    AuthenticationStatus status = gson.fromJson(json, AuthenticationStatus.class);
    assertThat(status.logoutUrl().isPresent()).isFalse();
    assertThat(status.loginUrl().isPresent()).isTrue();
  }
}
