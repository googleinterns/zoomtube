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
import com.googleinterns.zoomtube.data.TranscriptLanguage;
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
    transcriptLanguageServlet = new TranscriptLanguageServlet();
    transcriptLanguageServlet.init();
    transcriptLanguages = new StringWriter();
    PrintWriter writer = new PrintWriter(transcriptLanguages);
    when(response.getWriter()).thenReturn(writer);
  }

  @Test
  public void doGet_missingVideoLink_badRequest() throws ServletException, IOException {
    transcriptLanguageServlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing link parameter.");
  }

  @Test
  public void doGet_invalidVideoLink_badRequest() throws ServletException, IOException {
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK)).thenReturn(/* Invalid video id= */ "123456");

    transcriptLanguageServlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Invalid video link.");
  }

  @Test
  public void doGet_getListOfLanguages_videoWithManyLanguages() throws ServletException, IOException {
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK)).thenReturn("https://www.youtube.com/watch?v=fzQ6gRAEoy0");

    transcriptLanguageServlet.doGet(request, response);
    List<TranscriptLanguage> actualTranscriptLines = transcriptLanguages(transcriptLanguages.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(38);
  }

  @Test
  public void doGet_getListOfLanguages_videoWithNoLanguages() throws ServletException, IOException {
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK)).thenReturn("https://www.youtube.com/watch?v=QJO3ROT-A4E");

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLanguage> actualTranscriptLines = transcriptLines(transcriptLanguages.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(0);
  }

  @Test
  public void doGet_onlyOtherLecturesInDatastore_GetNoLectures()
      throws ServletException, IOException {
    putTranscriptLinesInDatastore(shortVideoTranscriptLines, lectureKeyB);
    putTranscriptLinesInDatastore(longVideoTranscriptLines, lectureKeyA);
    when(request.getParameter(TranscriptLanguageServlet.PARAM_ID)).thenReturn(LECTURE_ID_C.toString());

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLine> actualTranscriptLines = transcriptLanguages(transcriptLanguages.toString());
    assertThat(actualTranscriptLines.size()).isEqualTo(0);
  }

  private static List<TranscriptLanguage> transcriptLanguages(String transcriptLinesJson) {
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    return (ArrayList<TranscriptLanguage>) gson.fromJson(
        transcriptLanguagesJson, (new ArrayList<List<TranscriptLanguage>>().getClass()));
  }
}
