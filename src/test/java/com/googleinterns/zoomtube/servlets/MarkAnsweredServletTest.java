package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.googleinterns.zoomtube.data.Comment;
import com.googleinterns.zoomtube.utils.CommentUtil;
import com.googleinterns.zoomtube.utils.LectureUtil;
import java.io.IOException;
import java.util.Date;
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

public class MarkAnsweredServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private static final LocalServiceTestHelper testServices = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());

  private MarkAnsweredServlet servlet;
  private DatastoreService datastore;

  @Before
  public void setUp() throws ServletException {
    testServices.setUp();
    testServices.setEnvEmail("test@example.com");
    testServices.setEnvAuthDomain("example.com");
    servlet = new MarkAnsweredServlet();
    servlet.init();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void cleanUp() {
    testServices.tearDown();
  }

  @Test
  public void doPost_loggedOut_postForbidden() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(false);
    Entity testComment = createTestCommentEntity(/* entityId = */ 34);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_COMMENT)).thenReturn("34");
    when(request.getParameter(MarkAnsweredServlet.PARAM_NEW_TYPE))
        .thenReturn("QUESTION_UNANSWERED");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_FORBIDDEN, /* message= */ "You are not logged in.");
  }

  @Test
  public void doPost_missingComment_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    Entity testComment = createTestCommentEntity(/* entityId = */ 34);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_NEW_TYPE))
        .thenReturn("QUESTION_UNANSWERED");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing comment parameter.");
  }

  @Test
  public void doPost_missingNewType_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    Entity testComment = createTestCommentEntity(/* entityId = */ 34);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_COMMENT)).thenReturn("34");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing new type parameter.");
  }

  @Test
  public void doPost_invalidCommentId_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    Entity testComment = createTestCommentEntity(/* entityId = */ 34);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_COMMENT)).thenReturn("123");
    when(request.getParameter(MarkAnsweredServlet.PARAM_NEW_TYPE))
        .thenReturn("QUESTION_UNANSWERED");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_FORBIDDEN, /* message= */ "Specified comment could not be found.");
  }

  @Test
  public void doPost_invalidNewType_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    Entity testComment = createTestCommentEntity(/* entityId = */ 34);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_COMMENT)).thenReturn("34");
    when(request.getParameter(MarkAnsweredServlet.PARAM_NEW_TYPE)).thenReturn("REPLY");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_FORBIDDEN, /* message= */ "Invalid new type.");
  }

  @Test
  public void doPost_wrongExistingCommentType_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, /* lectureId= */ 123);
    User author = new User(/* email= */ "test@example.com", /* authDomain= */ "example.com");
    Date dateNow = new Date();
    Entity testEntity = CommentUtil.createRootEntity(lectureKey, /* timestampMs= */ 2000, author,
        /* content= */ "Untested comment content", dateNow, Comment.Type.NOTE);
    Entity testComment = new Entity(CommentUtil.KIND, /* entityId = */ 34);
    testComment.setPropertiesFrom(testEntity);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_COMMENT)).thenReturn("34");
    when(request.getParameter(MarkAnsweredServlet.PARAM_NEW_TYPE))
        .thenReturn("QUESTION_UNANSWERED");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_FORBIDDEN, /* message= */ "Specified comment is not a question.");
  }

  @Test
  public void doPost_updatesTypeToAnswered()
      throws ServletException, IOException, EntityNotFoundException {
    testServices.setEnvIsLoggedIn(true);
    Entity testComment = createTestCommentEntity(/* entityId = */ 34);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_COMMENT)).thenReturn("34");
    // By default, the test comment is QUESTION_UNANSWERED.
    when(request.getParameter(MarkAnsweredServlet.PARAM_NEW_TYPE)).thenReturn("QUESTION_ANSWERED");

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);
    Entity modifiedComment = datastore.get(testComment.getKey());
    assertThat(modifiedComment.getProperty(CommentUtil.TYPE)).isEqualTo("QUESTION_ANSWERED");
  }

  @Test
  public void doPost_updatesTypeToUnanswered()
      throws ServletException, IOException, EntityNotFoundException {
    testServices.setEnvIsLoggedIn(true);
    Entity testComment = createTestCommentEntity(/* entityId = */ 34);
    datastore.put(testComment);
    when(request.getParameter(MarkAnsweredServlet.PARAM_COMMENT)).thenReturn("34");
    // By default, the test comment is QUESTION_UNANSWERED, so it shouldn't change.
    when(request.getParameter(MarkAnsweredServlet.PARAM_NEW_TYPE))
        .thenReturn("QUESTION_UNANSWERED");

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);
    Entity modifiedComment = datastore.get(testComment.getKey());
    assertThat(modifiedComment.getProperty(CommentUtil.TYPE)).isEqualTo("QUESTION_UNANSWERED");
  }

  private Entity createTestCommentEntity(int entityId) {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, 12341234);
    User author = new User(/* email= */ "test@example.com", /* authDomain= */ "example.com");
    Date dateNow = new Date();

    Entity tempEntity = CommentUtil.createRootEntity(lectureKey, /* timestampMs= */ 2000, author,
        /* content= */ "Untested comment content", dateNow, Comment.Type.QUESTION_UNANSWERED);

    Entity realEntity = new Entity(CommentUtil.KIND, entityId);
    realEntity.setPropertiesFrom(tempEntity);
    return realEntity;
  }
}
