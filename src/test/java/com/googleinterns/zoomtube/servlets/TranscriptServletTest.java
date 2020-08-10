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
import com.googleinterns.zoomtube.utils.LectureUtil;
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
import org.junit.BeforeClass;
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

  private TranscriptServlet transcriptServlet;
  private DatastoreService datastore;
  private StringWriter lectureTranscript;

  private static final LocalDatastoreServiceTestConfig datastoreConfig =
      (new LocalDatastoreServiceTestConfig()).setNoStorage(true);
  private static final LocalServiceTestHelper localServiceHelper =
      new LocalServiceTestHelper(datastoreConfig);
  private static final Long LECTURE_ID_A = 123L;
  private static final Long LECTURE_ID_B = 345L;
  private static final Long LECTURE_ID_C = 234L;
  private static final String SHORT_VIDEO_ID = "Obgnr9pc820";
  private static final String LONG_VIDEO_ID = "jNQXAC9IVRw";
  // TODO: Find a way to reprsent this differently.
  private static final String SHORT_VIDEO_JSON =
      "[{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":1},\"lectureKey\":{\"kind\":\"Lect"
      + "ure\",\"id\":123},\"startTimestampMilliseconds\":400,\"durationMilliseconds\":1000,\"end"
      + "TimestampMilliseconds\":1400,\"content\":\" \"},{\"transcriptKey\":{\"kind\":\"Transcrip"
      + "tLine\",\"id\":2},\"lectureKey\":{\"kind\":\"Lecture\",\"id\":123},\"startTimestampMilli"
      + "seconds\":2280,\"durationMilliseconds\":1000,\"endTimestampMilliseconds\":3280,\"content"
      + "\":\"Hi\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":3},\"lectureKey\":{\"k"
      + "ind\":\"Lecture\",\"id\":123},\"startTimestampMilliseconds\":5040,\"durationMilliseconds"
      + "\":1600,\"endTimestampMilliseconds\":6640,\"content\":\"Okay\"}]";
  private static final String LONG_VIDEO_JSON =
      "[{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":1},\"lectureKey\":{\"kind\":\"Lect"
      + "ure\",\"id\":123},\"startTimestampMilliseconds\":1300,\"durationMilliseconds\":3100,\"en"
      + "dTimestampMilliseconds\":4400,\"content\":\"All right, so here we are\\nin front of the "
      + "elephants,\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":2},\"lectureKey\":{"
      + "\"kind\":\"Lecture\",\"id\":123},\"startTimestampMilliseconds\":4400,\"durationMilliseco"
      + "nds\":4766,\"endTimestampMilliseconds\":9166,\"content\":\"the cool thing about these gu"
      + "ys\\nis that they have really,\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\""
      + ":3},\"lectureKey\":{\"kind\":\"Lecture\",\"id\":123},\"startTimestampMilliseconds\":9166"
      + ",\"durationMilliseconds\":3534,\"endTimestampMilliseconds\":12700,\"content\":\"really, "
      + "really long trunks,\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":4},\"lectu"
      + "reKey\":{\"kind\":\"Lecture\",\"id\":123},\"startTimestampMilliseconds\":12700,\"duratio"
      + "nMilliseconds\":4300,\"endTimestampMilliseconds\":17000,\"content\":\"and that\\u0027s, "
      + "that\\u0027s cool.\"},{\"transcriptKey\":{\"kind\":\"TranscriptLine\",\"id\":5},\"lectur"
      + "eKey\":{\"kind\":\"Lecture\",\"id\":123},\"startTimestampMilliseconds\":17000,\"duration"
      + "Milliseconds\":1767,\"endTimestampMilliseconds\":18767,\"content\":\"And that\\u0027s pr"
      + "etty much all there is to say.\"}]";

  private static List<TranscriptLine> shortVideoTranscriptLines;
  private static List<TranscriptLine> longVideoTranscriptLines;

  @BeforeClass
  public static void createTranscriptLineLists() {
    shortVideoTranscriptLines = transcriptLines(SHORT_VIDEO_JSON);
    longVideoTranscriptLines = transcriptLines(LONG_VIDEO_JSON);
  }

  @Before
  public void setUp() throws ServletException, IOException {
    localServiceHelper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    transcriptServlet = new TranscriptServlet();
    transcriptServlet.init();
    lectureTranscript = new StringWriter();
    PrintWriter writer = new PrintWriter(lectureTranscript);
    when(response.getWriter()).thenReturn(writer);
  }

  @After
  public void tearDown() {
    localServiceHelper.tearDown();
  }

  @Test
  public void doGet_getDataInDatastoreForShortVideo() throws ServletException, IOException {
    putTranscriptLinesInDatastore(shortVideoTranscriptLines, LECTURE_ID_A);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = shortVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(lectureTranscript.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(expectedTranscriptLines.size());
  }

  @Test
  public void doPost_persistDataInDatastoreForShortVideo() throws ServletException, IOException {
    when(request.getParameter(LectureUtil.VIDEO_ID)).thenReturn(SHORT_VIDEO_ID);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_B.toString());

    transcriptServlet.doPost(request, response);

    int actualQueryCount = entitiesInDatastoreCount(LECTURE_ID_B);
    int expectedQueryCount = (shortVideoTranscriptLines).size();
    assertThat(actualQueryCount).isEqualTo(expectedQueryCount);
  }

  @Test
  public void doGet_doPost_storeAndRetrieveShortVideo() throws ServletException, IOException {
    when(request.getParameter(LectureUtil.VIDEO_ID)).thenReturn(SHORT_VIDEO_ID);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptServlet.doPost(request, response);
    transcriptServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = shortVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(lectureTranscript.toString());
    assertThat(actualTranscriptLines).isEqualTo(expectedTranscriptLines);
  }

  @Test
  public void doGet_doPost_storeAndRetrieveLongVideo() throws ServletException, IOException {
    when(request.getParameter(LectureUtil.VIDEO_ID)).thenReturn(LONG_VIDEO_ID);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptServlet.doPost(request, response);
    transcriptServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = longVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(lectureTranscript.toString());
    assertThat(actualTranscriptLines).isEqualTo(expectedTranscriptLines);
  }

  @Test
  public void doGet_returnsLectureForLongVideoFromDatastore() throws ServletException, IOException {
    putTranscriptLinesInDatastore(longVideoTranscriptLines, LECTURE_ID_A);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = longVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(lectureTranscript.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(expectedTranscriptLines.size());
  }

  @Test
  public void doPost_persistDataInDatastoreForLongVideo() throws ServletException, IOException {
    when(request.getParameter(LectureUtil.VIDEO_ID)).thenReturn(LONG_VIDEO_ID);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_C.toString());

    transcriptServlet.doPost(request, response);

    int actualQueryCount = entitiesInDatastoreCount(LECTURE_ID_C);
    int expectedQueryCount = (transcriptLines(LONG_VIDEO_JSON)).size();
    assertThat(actualQueryCount).isEqualTo(expectedQueryCount);
  }

  @Test
  public void doGet_onlyOtherLecturesInDatastore_GetNoLectures()
      throws ServletException, IOException {
    putTranscriptLinesInDatastore(shortVideoTranscriptLines, LECTURE_ID_B);
    putTranscriptLinesInDatastore(longVideoTranscriptLines, LECTURE_ID_A);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_C.toString());

    transcriptServlet.doGet(request, response);

    List<TranscriptLine> actualTranscriptLines = transcriptLines(lectureTranscript.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(0);
  }

  @Test
  public void doGet_twoLecturesInDatastore_returnsOneLecture()
      throws ServletException, IOException {
    putTranscriptLinesInDatastore(shortVideoTranscriptLines, LECTURE_ID_B);
    putTranscriptLinesInDatastore(longVideoTranscriptLines, LECTURE_ID_A);
    when(request.getParameter(LectureUtil.ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = longVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(lectureTranscript.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(expectedTranscriptLines.size());
  }

  private static List<TranscriptLine> transcriptLines(String transcriptLinesJson) {
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    return (ArrayList<TranscriptLine>) gson.fromJson(
        transcriptLinesJson, (new ArrayList<List<TranscriptLine>>().getClass()));
  }

  private void putTranscriptLinesInDatastore(List<TranscriptLine> transcriptLines, long lectureId) {
    for (int i = 0; i < transcriptLines.size(); i++) {
      Entity lineEntity = TranscriptLineUtil.createEntity(lectureId, "test content",
          /* start= */ 0, /* duration= */ 0, /* end= */ 0);
      datastore.put(lineEntity);
    }
  }

  private int entitiesInDatastoreCount(long lectureId) {
    // A limit of 100 for the maximum number of entities counted is used because
    // we can assume that for this test datastore, there won't be more than 100 entities
    // for a lecture key.
    return datastore.prepare(filteredQueryOfTranscriptLinesByLectureId(lectureId))
        .countEntities(withLimit(100));
  }

  private Query filteredQueryOfTranscriptLinesByLectureId(long lectureId) {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    Filter lectureFilter =
        new FilterPredicate(TranscriptLineUtil.LECTURE, FilterOperator.EQUAL, lectureKey);
    return new Query(TranscriptLineUtil.KIND).setFilter(lectureFilter);
  }
}
