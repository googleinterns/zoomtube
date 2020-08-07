// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googleinterns.zoomtube.data.Lecture;
import com.googleinterns.zoomtube.utils.LectureUtil;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class LectureServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private final LocalServiceTestHelper testServices =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastoreService;
  private LectureServlet servlet;

  private static final String LINK_INPUT = "link-input";
  private static final String TEST_LINK = "https://www.youtube.com/watch?v=wXhTHyIgQ_U";
  private static final String TEST_ID = "wXhTHyIgQ_U";

  @Before
  public void setUp() throws ServletException {
    testServices.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    servlet = new LectureServlet();
    servlet.init();
  }

  @After
  public void tearDown() {
    testServices.tearDown();
  }

  @Test
  public void doPost_urlAlreadyInDatabase_shouldReturnLecture() throws IOException {
    when(request.getParameter(LINK_INPUT)).thenReturn(TEST_LINK);
    datastoreService.put(LectureUtil.createEntity(/* lectureName= */ "", TEST_LINK, TEST_ID));
    servlet.doPost(request, response);

    assertThat(datastoreService.prepare(new Query(LectureUtil.KIND)).countEntities()).isEqualTo(1);
    verify(response).sendRedirect("/view/?id=1&video-id=wXhTHyIgQ_U");
  }

  @Test
  public void doPost_urlNotInDatabase_shouldAddToDatabaseAndReturnRedirect() throws IOException {
    when(request.getParameter(LINK_INPUT)).thenReturn(TEST_LINK);

    // No lecture in datastoreService.
    servlet.doPost(request, response);

    assertThat(datastoreService.prepare(new Query(LectureUtil.KIND)).countEntities()).isEqualTo(1);
    verify(response).sendRedirect("/view/?id=1&video-id=wXhTHyIgQ_U");
  }

  @Test
  public void doGet_emptyDatabase_shouldReturnNoLecture() throws IOException {
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String json = content.toString();
    assertThat(json).startsWith("[]");
  }

  @Test
  public void doGet_oneLectureInDatabase_shouldReturnOneLecture() throws IOException {
    when(request.getParameter(LINK_INPUT)).thenReturn(TEST_LINK);
    datastoreService.put(LectureUtil.createEntity(/* lectureName= */ "", TEST_LINK, TEST_ID));
    StringWriter content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);

    String json = content.toString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    Type listType = new TypeToken<ArrayList<Lecture>>() {}.getType();
    ArrayList<Lecture> lectures = gson.fromJson(json, listType);
    assertThat(lectures).hasSize(1);
    assertThat(lectures.get(0).videoUrl()).isEqualTo(TEST_LINK);
  }

  @Test
  public void getVideoId_shouldFindAllIds() {
    String video1 = "http://www.youtube.com/watch?v=dQw4w9WgXcQ&a=GxdCwVVULXctT2lYDEPllDR0LRTutYfW";
    String video2 = "http://www.youtube.com/watch?v=dQw4w9WgXcQ";
    String video3 = "http://youtu.be/dQw4w9WgXcQ";
    String video4 = "http://www.youtube.com/embed/dQw4w9WgXcQ";
    String video5 = "http://www.youtube.com/v/dQw4w9WgXcQ";
    String video6 = "http://www.youtube.com/watch?v=dQw4w9WgXcQ";
    String video7 = "http://www.youtube.com/watch?feature=player_embedded&v=dQw4w9WgXcQ";
    String video8 = "http://www.youtube-nocookie.com/v/dQw4w9WgXcQ?version=3&hl=en_US&rel=0";
    String id = "dQw4w9WgXcQ";

    assertThat(servlet.getVideoId(video1).get()).isEqualTo(id);
    assertThat(servlet.getVideoId(video2).get()).isEqualTo(id);
    assertThat(servlet.getVideoId(video3).get()).isEqualTo(id);
    assertThat(servlet.getVideoId(video4).get()).isEqualTo(id);
    assertThat(servlet.getVideoId(video5).get()).isEqualTo(id);
    assertThat(servlet.getVideoId(video6).get()).isEqualTo(id);
    assertThat(servlet.getVideoId(video7).get()).isEqualTo(id);
    assertThat(servlet.getVideoId(video8).get()).isEqualTo(id);
  }
}
