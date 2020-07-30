package com.googleinterns.zoomtube.servlets.pages;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PageServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Rule public final TemporaryFolder testFolder = new TemporaryFolder();
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

  @Test
  public void doGet_loggedIn_expectFile() throws Exception {
    File createdFile = testFolder.newFile("test.html");
    testServices.setEnvIsLoggedIn(true);
    ServletOutputStream mockOutput = mock(ServletOutputStream.class);
    when(response.getOutputStream()).thenReturn(mockOutput);
    servlet = new PageServlet(createdFile.getPath());
    servlet.init();

    servlet.doGet(request, response);

    // The file is recognized as plain text, maybe because it is empty?
    verify(response).setContentType("text/plain;");
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
