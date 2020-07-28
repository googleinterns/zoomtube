package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.AuthenticationStatus;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;

public class DiscussionServletTest {
  private DiscussionServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private LocalServiceTestHelper authService;
  private LocalServiceTestHelper datastoreService;

  @Before
  public void setUp() throws ServletException {
    servlet = new DiscussionServlet();
    servlet.init();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    authService = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    authService.setUp();
    datastoreService = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    datastoreService.setUp();
  }

  @After
  public void cleanUp() {
    authService.tearDown();
    datastoreService.tearDown();
  }
}
