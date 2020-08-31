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
import static org.mockito.Mockito.verify;
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
public final class TranscriptLanguageServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private TranscriptLanguageServlet transcriptLanguageServlet;
  private StringWriter transcriptLanguages;

  @Before
  public void setUp() throws ServletException, IOException {
    localServiceHelper.setUp();
    transcriptLanguageServlet = new TranscriptLanguageServlet();
    transcriptLanguageServlet.init();
    transcriptLanguages = new StringWriter();
    PrintWriter writer = new PrintWriter(transcriptLanguages);
    when(response.getWriter()).thenReturn(writer);
  }

  @After
  public void tearDown() {
    localServiceHelper.tearDown();
  }

  @Test
  public void doGet_missingId_badRequest() throws ServletException, IOException {
    transcriptLanguageServlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing id parameter.");
  }

  @Test
  public void doGet_getDataInDatastoreForShortVideo() throws ServletException, IOException {
    putTranscriptLinesInDatastore(shortVideoTranscriptLines, lectureKeyA);
    when(request.getParameter(TranscriptLanguageServlet.PARAM_ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = shortVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(transcriptLanguages.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(expectedTranscriptLines.size());
  }

  @Test
  public void doGet_returnsLectureForLongVideoFromDatastore() throws ServletException, IOException {
    putTranscriptLinesInDatastore(longVideoTranscriptLines, lectureKeyA);
    when(request.getParameter(TranscriptLanguageServlet.PARAM_ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = longVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(transcriptLanguages.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(expectedTranscriptLines.size());
  }

  @Test
  public void doGet_onlyOtherLecturesInDatastore_GetNoLectures()
      throws ServletException, IOException {
    putTranscriptLinesInDatastore(shortVideoTranscriptLines, lectureKeyB);
    putTranscriptLinesInDatastore(longVideoTranscriptLines, lectureKeyA);
    when(request.getParameter(TranscriptLanguageServlet.PARAM_ID)).thenReturn(LECTURE_ID_C.toString());

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLine> actualTranscriptLines = transcriptLines(transcriptLanguages.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(0);
  }

  @Test
  public void doGet_twoLecturesInDatastore_returnsOneLecture()
      throws ServletException, IOException {
    putTranscriptLinesInDatastore(shortVideoTranscriptLines, lectureKeyB);
    putTranscriptLinesInDatastore(longVideoTranscriptLines, lectureKeyA);
    when(request.getParameter(TranscriptLanguageServlet.PARAM_ID)).thenReturn(LECTURE_ID_A.toString());

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLine> expectedTranscriptLines = longVideoTranscriptLines;
    List<TranscriptLine> actualTranscriptLines = transcriptLines(transcriptLanguages.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(expectedTranscriptLines.size());
  }

  private static List<TranscriptLine> transcriptLines(String transcriptLinesJson) {
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    return (ArrayList<TranscriptLine>) gson.fromJson(
        transcriptLinesJson, (new ArrayList<List<TranscriptLine>>().getClass()));
  }

  private void putTranscriptLinesInDatastore(List<TranscriptLine> transcriptLines, Key lectureKey) {
    for (int i = 0; i < transcriptLines.size(); i++) {
      Entity lineEntity = TranscriptLineUtil.createEntity(lectureKey, "test content",
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
