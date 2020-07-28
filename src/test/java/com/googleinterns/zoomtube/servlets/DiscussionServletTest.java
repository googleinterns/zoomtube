package com.googleinterns.zoomtube.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.googleinterns.zoomtube.data.Comment;
import com.googleinterns.zoomtube.mocks.MockRequest;
import com.googleinterns.zoomtube.mocks.MockResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscussionServletTest {
  private DiscussionServlet servlet;
  private MockRequest request;
  private MockResponse response;
  private LocalServiceTestHelper serviceHelper = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(),
      new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

  @Before
  public void setUp() throws ServletException {
    serviceHelper.setUp();
    servlet = new DiscussionServlet();
    servlet.init();
    response = new MockResponse();
    request = new MockRequest();
  }

  @After
  public void cleanUp() {
    serviceHelper.tearDown();
  }

  @Test
  public void doPost_loggedOut_postForbidden() throws ServletException, IOException {
    // authService.setEnvIsLoggedIn(false);
    String LECTURE_ID = "1";
    request.setParameter(DiscussionServlet.PARAM_LECTURE, LECTURE_ID);

    servlet.doPost(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertThat(ds.prepare(new Query(Comment.ENTITY_KIND)).countEntities(withLimit(1))).isEqualTo(0);
  }
}
