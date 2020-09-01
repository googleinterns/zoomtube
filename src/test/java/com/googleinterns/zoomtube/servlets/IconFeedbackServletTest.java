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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googleinterns.zoomtube.data.IconFeedback;
import com.googleinterns.zoomtube.utils.IconFeedbackUtil;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class IconFeedbackServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private final LocalServiceTestHelper testServices =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastoreService;
  private IconFeedbackServlet servlet;

  /* Writer where response is written. */
  private StringWriter content;

  @Before
  public void setUp() throws Exception {
    testServices.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    servlet = new IconFeedbackServlet();
    servlet.init();
    content = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(content));
  }

  @After
  public void tearDown() {
    testServices.tearDown();
  }

  @Test
  public void doPost_missingLectureId_shouldRespondWithBadRequest() throws Exception {
    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing lecture id parameter.");
  }

  @Test
  public void doPost_missingTimestamp_shouldRespondWithBadRequest() throws Exception {
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn("123");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing timestamp parameter.");
  }

  @Test
  public void doPost_missingIconType_shouldRespondWithBadRequest() throws Exception {
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn("123");
    when(request.getParameter(IconFeedbackServlet.PARAM_TIMESTAMP)).thenReturn("456");

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing icon type parameter.");
  }

  @Test
  public void doPost_validRequest_shouldStoreInDatabase() throws Exception {
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn("123");
    when(request.getParameter(IconFeedbackServlet.PARAM_TIMESTAMP)).thenReturn("456");
    when(request.getParameter(IconFeedbackServlet.PARAM_ICON_TYPE)).thenReturn("GOOD");

    servlet.doPost(request, response);

    PreparedQuery query = datastoreService.prepare(new Query(IconFeedbackUtil.KIND));
    assertThat(query.countEntities()).isEqualTo(1);
    IconFeedback iconFeedback = IconFeedbackUtil.createIconFeedback(query.asSingleEntity());
    assertThat(iconFeedback.lectureKey().getId()).isEqualTo(123);
    assertThat(iconFeedback.timestampMs()).isEqualTo(456);
    assertThat(iconFeedback.type().toString()).isEqualTo("GOOD");
  }

  @Test
  public void doGet_missingLectureId_shouldRespondWithBadRequest() throws Exception {
    servlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing lecture id parameter.");
  }

  @Test
  public void doGet_noFeedback_shouldReturnNoIconFeedback() throws Exception {
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn("123");

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    List<IconFeedback> result = getIconFeedbackFromJson(content.toString());
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void doGet_oneIconFeedback_shouldReturnOneIconFeedback() throws Exception {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, /* lectureId= */ 123);
    datastoreService.put(
        IconFeedbackUtil.createEntity(lectureKey, /* timestampMs= */ 456L, IconFeedback.Type.GOOD));
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn("123");

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    List<IconFeedback> result = getIconFeedbackFromJson(content.toString());
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).lectureKey().getId()).isEqualTo(123);
    assertThat(result.get(0).timestampMs()).isEqualTo(456);
    assertThat(result.get(0).type()).isEqualTo(IconFeedback.Type.GOOD);
  }

  @Test
  public void doGet_oneIconFeedbackLectureIdDoesntMatch_shouldReturnNoIconFeedback()
      throws Exception {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, /* lectureId= */ 123);
    datastoreService.put(
        IconFeedbackUtil.createEntity(lectureKey, /* timestampMs= */ 456L, IconFeedback.Type.GOOD));
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn("789");

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    List<IconFeedback> result = getIconFeedbackFromJson(content.toString());
    assertThat(result.size()).isEqualTo(0);
  }

  private List<IconFeedback> getIconFeedbackFromJson(String json) {
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    Type listType = new TypeToken<ArrayList<IconFeedback>>() {}.getType();
    return gson.fromJson(json, listType);
  }
}
