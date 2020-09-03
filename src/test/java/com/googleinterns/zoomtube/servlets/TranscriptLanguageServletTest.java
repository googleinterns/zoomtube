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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.TranscriptLanguage;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
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
  public void setUp() throws Exception {
    transcriptLanguageServlet = new TranscriptLanguageServlet();
    transcriptLanguageServlet.init();
    transcriptLanguages = new StringWriter();
    PrintWriter writer = new PrintWriter(transcriptLanguages);
    when(response.getWriter()).thenReturn(writer);
  }

  @Test
  public void doGet_missingVideoLink_badRequest() throws Exception {
    transcriptLanguageServlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing link parameter.");
  }

  @Test
  public void doGet_invalidVideoLink_badRequest() throws Exception {
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK)).thenReturn("123456");

    transcriptLanguageServlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Invalid video link.");
  }

  @Test
  public void doGet_getListOfLanguages_videoWithManyLanguages() throws Exception {
<<<<<<< HEAD
    String videoWithManyLanguageOptions = "https://www.youtube.com/watch?v=fzQ6gRAEoy0";
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK))
        .thenReturn(videoWithManyLanguageOptions);
=======
    String videoWithManyLanguagesAvailable = "https://www.youtube.com/watch?v=fzQ6gRAEoy0";
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK))
        .thenReturn(videoWithManyLanguagesAvailable);
>>>>>>> master

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLanguage> actualTranscriptLanguages =
        transcriptLanguages(transcriptLanguages.toString());
    assertThat(actualTranscriptLanguages.size()).isEqualTo(38);
  }

  @Test
  public void doGet_getListOfLanguages_videoWithNoLanguages() throws Exception {
<<<<<<< HEAD
    String videoWithNoLanguageOptions = "https://www.youtube.com/watch?v=QJO3ROT-A4E";
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK))
        .thenReturn(videoWithNoLanguageOptions);
=======
    String videoWithNoLanguageAvailable = "https://www.youtube.com/watch?v=QJO3ROT-A4E";
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK))
        .thenReturn(videoWithNoLanguageAvailable);
>>>>>>> master

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLanguage> actualTranscriptLanguages =
        transcriptLanguages(transcriptLanguages.toString());
    assertThat(actualTranscriptLanguages.size()).isEqualTo(0);
  }

  @Test
  public void doGet_getListOfLanguages_videoWithOnlyOneLanguage() throws Exception {
<<<<<<< HEAD
    String videoWithOnlyOneLanguage = "https://www.youtube.com/watch?v=9DwzBICPhdM";
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK))
        .thenReturn(videoWithOnlyOneLanguage);
=======
    String videoWithOnlyOneLanguageAvailable = "https://www.youtube.com/watch?v=9DwzBICPhdM";
    when(request.getParameter(TranscriptLanguageServlet.PARAM_LINK))
        .thenReturn(videoWithOnlyOneLanguageAvailable);
>>>>>>> master

    transcriptLanguageServlet.doGet(request, response);

    List<TranscriptLanguage> actualTranscriptLanguages =
        transcriptLanguages(transcriptLanguages.toString());
    assertThat(actualTranscriptLanguages.size()).isEqualTo(1);
  }

  private static List<TranscriptLanguage> transcriptLanguages(String transcriptLanguagesJson) {
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    Class<? extends ArrayList> transcriptLanguageClass =
        (new ArrayList<List<TranscriptLanguage>>().getClass());
    return (ArrayList<TranscriptLanguage>) gson.fromJson(
        transcriptLanguagesJson, transcriptLanguageClass);
  }
<<<<<<< HEAD
}
=======
}
>>>>>>> master
