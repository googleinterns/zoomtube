package com.googleinterns.zoomtube.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googleinterns.zoomtube.data.Comment;
import com.googleinterns.zoomtube.utils.CommentUtil;
import com.googleinterns.zoomtube.utils.LectureUtil;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

public class DiscussionServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private static final int LECTURE_ID = 1;
  private static final String LECTURE_ID_STR = "1";
  private static final LocalServiceTestHelper testServices = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());

  private DiscussionServlet servlet;
  private DatastoreService datastore;

  @Before
  public void setUp() throws ServletException {
    testServices.setUp();
    testServices.setEnvEmail("test@example.com");
    testServices.setEnvAuthDomain("example.com");
    servlet = new DiscussionServlet();
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
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    when(request.getParameter(DiscussionServlet.PARAM_TYPE))
        .thenReturn(Comment.Type.REPLY.toString());
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader("Something unique")));
    when(request.getParameter(DiscussionServlet.PARAM_PARENT)).thenReturn("123");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_FORBIDDEN, /* message= */ "You are not logged in.");
    assertThat(datastore.prepare(new Query(CommentUtil.KIND)).countEntities(withLimit(1)))
        .isEqualTo(0);
  }

  @Test
  public void doPost_missingLecture_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing lecture parameter.");
  }

  @Test
  public void doPost_missingType_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader("Untested Content")));

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing comment type parameter.");
  }

  @Test
  public void doPost_missingReplyParent_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    when(request.getParameter(DiscussionServlet.PARAM_TYPE))
        .thenReturn(Comment.Type.REPLY.toString());
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader("Untested Content")));

    servlet.doPost(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,
        /* message= */ "Missing parent parameter for reply comment.");
  }

  @Test
  public void doPost_missingRootTimestamp_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    when(request.getParameter(DiscussionServlet.PARAM_TYPE))
        .thenReturn(Comment.Type.QUESTION.toString());
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader("Untested Content")));

    servlet.doPost(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,
        /* message= */ "Missing timestamp parameter for root comment.");
  }

  @Test
  public void doPost_reply_storesCommentWithAllProperties() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    testServices.setEnvEmail("author@example.com");
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    when(request.getParameter(DiscussionServlet.PARAM_TYPE))
        .thenReturn(Comment.Type.REPLY.toString());
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader("Something unique")));
    when(request.getParameter(DiscussionServlet.PARAM_PARENT)).thenReturn("456");

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);
    PreparedQuery query = datastore.prepare(new Query(CommentUtil.KIND));
    assertThat(query.countEntities(withLimit(2))).isEqualTo(1);
    Comment comment = CommentUtil.createComment(query.asSingleEntity());
    assertThat(comment.lectureKey().getId()).isEqualTo(LECTURE_ID);
    assertThat(comment.author().getEmail()).isEqualTo("author@example.com");
    assertThat(comment.content()).isEqualTo("Something unique");
    assertThat(comment.parentKey().get().getId()).isEqualTo(456);
    assertThat(comment.type()).isEqualTo(Comment.Type.REPLY);
  }

  @Test
  public void doPost_rootComment_storesTypeAndTimestamp() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    when(request.getParameter(DiscussionServlet.PARAM_TIMESTAMP)).thenReturn("2000");
    when(request.getParameter(DiscussionServlet.PARAM_TYPE))
        .thenReturn(Comment.Type.QUESTION.toString());
    when(request.getReader())
        .thenReturn(new BufferedReader(new StringReader("Random untested content")));

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);
    PreparedQuery query = datastore.prepare(new Query(CommentUtil.KIND));
    Comment comment = CommentUtil.createComment(query.asSingleEntity());
    assertThat(comment.parentKey().isPresent()).isFalse();
    assertThat(comment.timestampMs().get()).isEqualTo(2000);
    assertThat(comment.type()).isEqualTo(Comment.Type.QUESTION);
  }

  @Test
  public void doGet_missingLecture_badRequest() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(true);

    servlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing lecture parameter.");
  }

  @Test
  public void doGet_returnsNothingForUnknownLecture() throws ServletException, IOException {
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    List<Comment> comments = getCommentsFromJson(content.toString());
    assertThat(comments.isEmpty()).isTrue();
  }

  @Test
  public void doGet_returnsComments() throws ServletException, IOException {
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE)).thenReturn(LECTURE_ID_STR);
    datastore.put(createTestCommentEntity(LECTURE_ID));
    datastore.put(createTestCommentEntity(LECTURE_ID));
    datastore.put(createTestCommentEntity(LECTURE_ID));
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    List<Comment> comments = getCommentsFromJson(content.toString());
    assertThat(comments.size()).isEqualTo(3);
  }

  @Test
  public void doGet_returnsCommentsForSpecificLecture() throws ServletException, IOException {
    final int lectureA = 1;
    final int lectureB = 2;
    final int lectureC = 3;
    // Looking for two comments under LECTURE_A.
    when(request.getParameter(DiscussionServlet.PARAM_LECTURE))
        .thenReturn(Integer.toString(lectureA));
    datastore.put(createTestCommentEntity(lectureB));
    datastore.put(createTestCommentEntity(lectureC));
    datastore.put(createTestCommentEntity(lectureA)); // Add the first.
    datastore.put(createTestCommentEntity(lectureB));
    datastore.put(createTestCommentEntity(lectureB));
    datastore.put(createTestCommentEntity(lectureC));
    datastore.put(createTestCommentEntity(lectureA)); // Add the second.
    datastore.put(createTestCommentEntity(lectureC));
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    List<Comment> comments = getCommentsFromJson(content.toString());
    assertThat(comments.size()).isEqualTo(2);
  }

  private Entity createTestCommentEntity(int lectureId) {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    User author = new User("test@example.com", "example.com");
    Date dateNow = new Date();

    return CommentUtil.createRootEntity(lectureKey, /* timestampMs= */ 2000, author,
        /* content= */ "Untested comment content", dateNow, Comment.Type.QUESTION);
  }

  private List<Comment> getCommentsFromJson(String json) {
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    Type listType = new TypeToken<ArrayList<Comment>>() {}.getType();
    return gson.fromJson(json, listType);
  }
}
