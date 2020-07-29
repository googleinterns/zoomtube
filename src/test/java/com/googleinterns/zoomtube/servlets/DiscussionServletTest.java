package com.googleinterns.zoomtube.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static com.google.common.truth.Truth.assertThat;

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
import com.googleinterns.zoomtube.mocks.MockRequest;
import com.googleinterns.zoomtube.mocks.MockResponse;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscussionServletTest {
  private DiscussionServlet servlet;
  private MockRequest request;
  private MockResponse response;
  private LocalServiceTestHelper testServices = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore;

  @Before
  public void setUp() throws ServletException {
    testServices.setUp();
    servlet = new DiscussionServlet();
    servlet.init();
    response = new MockResponse();
    request = new MockRequest();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void cleanUp() {
    testServices.tearDown();
  }

  @Test
  public void doPost_loggedOut_postForbidden() throws ServletException, IOException {
    testServices.setEnvIsLoggedIn(false);
    String LECTURE_ID = "1";
    request.setParameter(DiscussionServlet.PARAM_LECTURE, LECTURE_ID);

    servlet.doPost(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    assertThat(datastore.prepare(new Query(Comment.ENTITY_KIND)).countEntities(withLimit(1)))
        .isEqualTo(0);
  }

  @Test
  public void doPost_storesCommentWithProperties_noParent() throws ServletException, IOException {
    int LECTURE_ID = 1;
    String CONTENT = "Test content";
    String EMAIL = "test@example.com";
    testServices.setEnvIsLoggedIn(true);
    testServices.setEnvEmail(EMAIL);
    testServices.setEnvAuthDomain("example.com");
    request.setParameter(DiscussionServlet.PARAM_LECTURE, Integer.toString(LECTURE_ID));
    request.setContent(CONTENT);
    PreparedQuery query = datastore.prepare(new Query(Comment.ENTITY_KIND));
    assertThat(query.countEntities(withLimit(2))).isEqualTo(0);

    servlet.doPost(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_ACCEPTED);
    assertThat(query.countEntities(withLimit(2))).isEqualTo(1);
    Comment comment = Comment.fromEntity(query.asSingleEntity());
    assertThat(comment.lecture().getId()).isEqualTo(LECTURE_ID);
    assertThat(comment.author().getEmail()).isEqualTo(EMAIL);
    assertThat(comment.content()).isEqualTo(CONTENT);
    assertThat(comment.parent().isPresent()).isFalse();
  }

  @Test
  public void doPost_storesCommentParent() throws ServletException, IOException {
    int PARENT_ID = 32;
    testServices.setEnvIsLoggedIn(true);
    testServices.setEnvEmail("test@example.com");
    testServices.setEnvAuthDomain("example.com");
    request.setParameter(DiscussionServlet.PARAM_LECTURE, "1");
    request.setParameter(DiscussionServlet.PARAM_PARENT, Integer.toString(PARENT_ID));
    request.setContent("Test content");
    PreparedQuery query = datastore.prepare(new Query(Comment.ENTITY_KIND));
    assertThat(query.countEntities(withLimit(2))).isEqualTo(0);

    servlet.doPost(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_ACCEPTED);
    Comment comment = Comment.fromEntity(query.asSingleEntity());
    assertThat(comment.parent().isPresent()).isTrue();
    assertThat(comment.parent().get().getId()).isEqualTo(PARENT_ID);
  }

  @Test
  public void doGet_returnsNothingForUnknownLecture() throws ServletException, IOException {
    request.setParameter(DiscussionServlet.PARAM_LECTURE, "1");

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    assertThat(response.getContentAsString()).startsWith("[]");
  }

  @Test
  public void doGet_returnsComments() throws ServletException, IOException {
    request.setParameter(DiscussionServlet.PARAM_LECTURE, "1");
    datastore.put(createTestCommentEntity(1));
    datastore.put(createTestCommentEntity(1));
    datastore.put(createTestCommentEntity(1));

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    Type listType = new TypeToken<ArrayList<Comment>>() {}.getType();
    List<Comment> comments = gson.fromJson(json, listType);
    assertThat(comments.size()).isEqualTo(3);
  }

  @Test
  public void doGet_returnsCommentsForSpecificLecture() throws ServletException, IOException {
    request.setParameter(DiscussionServlet.PARAM_LECTURE, "2");
    datastore.put(createTestCommentEntity(1));
    datastore.put(createTestCommentEntity(1));
    datastore.put(createTestCommentEntity(1));
    datastore.put(createTestCommentEntity(2)); // this one
    datastore.put(createTestCommentEntity(2)); // and this one
    datastore.put(createTestCommentEntity(3));
    datastore.put(createTestCommentEntity(5));

    servlet.doGet(request, response);

    assertThat(response.getContentType()).isEqualTo("application/json;");
    String json = response.getContentAsString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    Type listType = new TypeToken<ArrayList<Comment>>() {}.getType();
    List<Comment> comments = gson.fromJson(json, listType);
    assertThat(comments.size()).isEqualTo(2);
  }

  private Entity createTestCommentEntity(int lectureId) {
    Entity commentEntity = new Entity(Comment.ENTITY_KIND);
    Key lecture = KeyFactory.createKey(/* kind= */ "Lecture", lectureId);
    commentEntity.setProperty(Comment.PROP_LECTURE, lecture);
    commentEntity.setProperty(Comment.PROP_PARENT, null);
    commentEntity.setProperty(Comment.PROP_TIMESTAMP, new Date(0));
    commentEntity.setProperty(Comment.PROP_AUTHOR, new User("test@example.com", "example.com"));
    commentEntity.setProperty(Comment.PROP_CONTENT, "Test Content");
    commentEntity.setProperty(Comment.PROP_CREATED, new Date(Clock.systemUTC().millis()));

    return commentEntity;
  }
}
