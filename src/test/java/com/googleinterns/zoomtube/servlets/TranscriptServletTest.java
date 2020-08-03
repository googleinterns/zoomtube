// Copyright 2019 Google LLC
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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.TranscriptLine;
import com.googleinterns.zoomtube.utils.TranscriptLineUtil;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class TranscriptServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private TranscriptServlet servlet;

  private LocalDatastoreServiceTestConfig datastoreConfig =
      (new LocalDatastoreServiceTestConfig()).setNoStorage(true);
  private final LocalServiceTestHelper localServiceHelper =
      new LocalServiceTestHelper(datastoreConfig);
  private DatastoreService datastore;
  private Gson gson;
  StringWriter content;

  private static final String LECTURE_ID_A = "123";
  private static final String LECTURE_ID_B = "345";
  private static final String LECTURE_ID_C = "234";
  private static final String SHORT_VIDEO_ID = "Obgnr9pc820";
  private static final String LONG_VIDEO_ID = "jNQXAC9IVRw";
  private static final String SHORT_VIDEO_JSON =
      "[{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":"
      + "1},\"lectureKey\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"Jan 1, 1970 12:00:00 AM\","
      + "\"duration\":\"Jan 1, 1970 12:00:01 AM\",\"end\":\"Jan 1, 1970 12:00:01 AM\",\"content\":"
      + "\" \"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":2},\"lectureKey\":{\"kind\":\"lecture\","
      + "\"id\":123},\"start\":\"Jan 1, 1970 12:00:02 AM\",\"duration\":\"Jan 1, 1970 12:00:01 AM\","
      + "\"end\":\"Jan 1, 1970 12:00:03 AM\",\"content\":\"Hi\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\","
      + "\"id\":3},\"lectureKey\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"Jan 1, 1970 12:00:05 AM\","
      + "\"duration\":\"Jan 1, 1970 12:00:01 AM\",\"end\":\"Jan 1, 1970 12:00:06 AM\",\"content\":\"Okay\"}]";
  private static final String LONG_VIDEO_JSON =
      "[{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":1}"
      + ",\"lectureKey\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"Jan 1, 1970 12:00:01 AM\","
      + "\"duration\":\"Jan 1, 1970 12:00:03 AM\",\"end\":\"Jan 1, 1970 12:00:04 AM\",\"content\""
      + ":\"All right, so here we are\\nin front of the elephants,\"},{\"transcriptKey\":{\"kind\":"
      + "\"TranscriptLine\",\"id\":2},\"lectureKey\":{\"kind\":\"lecture\",\"id\":123}"
      + ",\"start\":\"Jan 1, 1970 12:00:04 AM\",\"duration\":\"Jan 1, 1970 12:00:04 AM\","
      + "\"end\":\"Jan 1, 1970 12:00:09 AM\",\"content\":\"the cool thing about these "
      + "guys\\nis that they have really,\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":3},"
      + "\"lectureKey\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"Jan 1, 1970 12:00:09 AM\","
      + "\"duration\":\"Jan 1, 1970 12:00:03 AM\",\"end\":\"Jan 1, 1970 12:00:12 AM\",\"content\":"
      + "\"really, really long trunks,\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":4},\"lectureKey\""
      + ":{\"kind\":\"lecture\",\"id\":123},\"start\":\"Jan 1, 1970 12:00:12 AM\",\"duration\":\"Jan "
      + "1, 1970 12:00:04 AM\",\"end\":\"Jan 1, 1970 12:00:17 AM\",\"content\":\"and that&#39;s, "
      + "that&#39;s cool.\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":5},\"lectureKey\""
      + ":{\"kind\":\"lecture\",\"id\":123},\"start\":\"Jan 1, 1970 12:00:17 AM\",\"duration\":"
      + "\"Jan 1, 1970 12:00:01 AM\",\"end\":\"Jan 1, 1970 12:00:18 AM\",\"content\""
      + ":\"And that&#39;s pretty much all there is to say.\"}]";

  @Before
  public void setUp() throws ServletException, IOException {
    localServiceHelper.setUp();
    servlet = new TranscriptServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
    servlet.init(datastore);
    gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    content = new StringWriter();
    PrintWriter writer = new PrintWriter(content);
    when(response.getWriter()).thenReturn(writer);
  }

  @After
  public void tearDown() {
    localServiceHelper.tearDown();
  }

  @Test
  public void doGet_getDataInDatastoreForShortVideo() throws ServletException, IOException {
    putJsonInDatastore(SHORT_VIDEO_JSON, LECTURE_ID_A);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_A);

    servlet.doGet(request, response);
    String actualJson = content.toString();
    List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(SHORT_VIDEO_JSON);
    List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(actualJsonArray.size()).isEqualTo(expectedArrayList.size());
  }

  @Test
  public void doPost_persistDataInDatastoreForShortVideo() throws ServletException, IOException {
    when(request.getParameter(TranscriptLineUtil.PARAM_VIDEO_ID)).thenReturn(SHORT_VIDEO_ID);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_B);

    servlet.doPost(request, response);
    int actualQueryCount = countEntitiesInDatastore(LECTURE_ID_B);
    int expectedQueryCount = (extractJsonAsArrayList(SHORT_VIDEO_JSON)).size();

    assertThat(actualQueryCount).isEqualTo(expectedQueryCount);
  }

  @Test
  public void doGet_doPost_StoreAndRetrieveShortVideo() throws ServletException, IOException {
    when(request.getParameter(TranscriptLineUtil.PARAM_VIDEO_ID)).thenReturn(SHORT_VIDEO_ID);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_A);

    servlet.doPost(request, response);
    servlet.doGet(request, response);
    String actualJson = content.toString();
    List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(SHORT_VIDEO_JSON);
    List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(actualJsonArray).isEqualTo(expectedArrayList);
  }

  @Test
  public void doGet_doPost_StoreAndRetrieveLongVideo() throws ServletException, IOException {
    when(request.getParameter(TranscriptLineUtil.PARAM_VIDEO_ID)).thenReturn(LONG_VIDEO_ID);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_A);

    servlet.doPost(request, response);
    servlet.doGet(request, response);
    String actualJson = content.toString();
    List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(LONG_VIDEO_JSON);
    List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(actualJsonArray).isEqualTo(expectedArrayList);
  }

  @Test
  public void doGet_getDataInDatastoreForLongVideo() throws ServletException, IOException {
    putJsonInDatastore(LONG_VIDEO_JSON, LECTURE_ID_A);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_A);

    servlet.doGet(request, response);
    String actualJson = content.toString();
    List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(LONG_VIDEO_JSON);
    List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(actualJsonArray.size()).isEqualTo(expectedArrayList.size());
  }

  @Test
  public void doPost_persistDataInDatastoreForLongVideo() throws ServletException, IOException {
    when(request.getParameter(TranscriptLineUtil.PARAM_VIDEO_ID)).thenReturn(LONG_VIDEO_ID);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_C);

    servlet.doPost(request, response);
    int actualQueryCount = countEntitiesInDatastore(LECTURE_ID_C);
    int expectedQueryCount = (extractJsonAsArrayList(LONG_VIDEO_JSON)).size();

    assertThat(actualQueryCount).isEqualTo(expectedQueryCount);
  }

  @Test
  public void doGet_OnlyOtherLecturesInDatastore_GetNoLectures()
      throws ServletException, IOException {
    putJsonInDatastore(SHORT_VIDEO_JSON, LECTURE_ID_B);
    putJsonInDatastore(LONG_VIDEO_JSON, LECTURE_ID_A);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_C);

    servlet.doGet(request, response);
    String actualJson = content.toString();
    List<TranscriptLine> expectedArrayList = new ArrayList<>();
    List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(actualJsonArray).isEqualTo(expectedArrayList);
  }

  @Test
  public void doGet_TwoLecturesInDatastore_GetOneLecture() throws ServletException, IOException {
    putJsonInDatastore(SHORT_VIDEO_JSON, LECTURE_ID_B);
    putJsonInDatastore(LONG_VIDEO_JSON, LECTURE_ID_A);
    when(request.getParameter(TranscriptLineUtil.PARAM_LECTURE_ID)).thenReturn(LECTURE_ID_A);

    servlet.doGet(request, response);
    String actualJson = content.toString();
    List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(LONG_VIDEO_JSON);
    List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(actualJsonArray.size()).isEqualTo(expectedArrayList.size());
  }

  private List<TranscriptLine> extractJsonAsArrayList(String json) {
    return (ArrayList<TranscriptLine>) gson.fromJson(
        json, (new ArrayList<List<TranscriptLine>>().getClass()));
  }

  private void putJsonInDatastore(String json, String lectureKeyId) {
    List<TranscriptLine> transcriptLineArray = extractJsonAsArrayList(json);
    Key lectureKey =
        KeyFactory.createKey(TranscriptLineUtil.PARAM_LECTURE, Long.parseLong(lectureId));
    for (int i = 0; i < transcriptLineArray.size(); i++) {
      Entity lineEntity = new Entity(TranscriptLineUtil.ENTITY_KIND);
      lineEntity.setProperty(TranscriptLineUtil.PROP_LECTURE, lectureKey);
      // Set dummy values because AutoValue needs all the values to create a TranscriptLine object.
      lineEntity.setProperty(TranscriptLineUtil.PROP_START, new Date());
      lineEntity.setProperty(TranscriptLineUtil.PROP_DURATION, new Date());
      lineEntity.setProperty(TranscriptLineUtil.PROP_END, new Date());
      lineEntity.setProperty(TranscriptLineUtil.PROP_CONTENT, "test content");
      datastore.put(lineEntity);
    }
  }

  private int countEntitiesInDatastore(String lectureKeyId) {
    return datastore.prepare(filteredQuery(lectureKeyId)).countEntities(withLimit(100));
  }

  private Query filteredQuery(String lectureId) {
    Key lectureKey = KeyFactory.createKey(TranscriptLineUtil.PARAM_LECTURE, Long.parseLong(lectureId));
    Filter lectureFilter =
        new FilterPredicate(TranscriptLineUtil.PROP_LECTURE, FilterOperator.EQUAL, lectureKey);
    return new Query(TranscriptLineUtil.ENTITY_KIND).setFilter(lectureFilter);
  }
}
