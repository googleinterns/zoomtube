package com.googleinterns.zoomtube.servlets.pages;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.io.FileNotFoundException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PageServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  private PageServlet servlet;
  @Mock private ServletConfig servletConfig;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private LocalServiceTestHelper testServices =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  @Before
  public void setUp() throws Exception {
    testServices.setUp();
    testServices.setEnvAuthDomain("example.com");
    testServices.setEnvEmail("test@example.com");
  }

  @After
  public void cleanUp() {
    testServices.tearDown();
  }

  @Test(expected = FileNotFoundException.class)
  public void doGet_loggedIn_expectFileNotFound() throws Exception {
    testServices.setEnvIsLoggedIn(true);
    servlet = new PageServlet("does_not_exist.html");
    servlet.init();

    servlet.doGet(request, response);

    // This test does not run in the correct directory to reference existing HTML
    // files, and reading disk during a test is probably a bad idea.
    // This should throw an error as it tries to read a file that doesn't exist.
  }

  @Test
  public void doGet_loggedOut_expectRedirect() throws Exception {
    testServices.setEnvIsLoggedIn(false);
    servlet = new PageServlet("does_not_exist.html");
    servlet.init();
    when(request.getRequestURI()).thenReturn("/some-page");

    servlet.doGet(request, response);

    // Format of redirect can vary.
    verify(response).sendRedirect(anyString());
  }
}
