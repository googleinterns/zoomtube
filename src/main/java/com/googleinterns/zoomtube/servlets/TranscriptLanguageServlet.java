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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.TranscriptLanguage;
import com.googleinterns.zoomtube.transcriptParser.TranscriptParser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Provides information on a lecture. */
public class TranscriptLanguageServlet extends HttpServlet {
  private static final String API_URL = "http://video.google.com/timedtext";
  private static final String API_PARAM_TYPE = "type";
  private static final String API_TYPE_LIST = "list";
  private static final String API_PARAM_VIDEO = "v";
  private static final String ERROR_MISSING_LINK = "Missing link parameter.";
  private static final String ERROR_INVALID_LINK = "Invalid video link.";
  private static final String TAG_TRACK = "track";
  private static final String ATTR_LANG_CODE = "lang_code";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_LANG_TRANSLATED = "lang_translated";

  /* Name of input field used for lecture video link in lecture selection page. */
  @VisibleForTesting static final String PARAM_LINK = "link-input";
  @VisibleForTesting static final String PARAM_ID = "id";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> error = validateGetRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
      return;
    }

    String videoUrl = request.getParameter(PARAM_LINK);
    Optional<String> videoId = LectureServlet.getVideoId(videoUrl);
    if (!videoId.isPresent()) {
      // TODO: Display a message in the client.
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_LINK);
      return;
    }

    URL transcriptLanguagesUrl = getTranscriptLanguagesUrlForVideo(videoId.get());
    Document transcriptLanguagesDocument =
        TranscriptParser.fetchUrlAsXmlDocument(transcriptLanguagesUrl);
    List<TranscriptLanguage> transcriptLanguages =
        parseTranscriptLanguages(transcriptLanguagesDocument);
    writeTranscriptLanguages(response, transcriptLanguages);
  }

  private Optional<String> validateGetRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_LINK) == null) {
      return Optional.of(ERROR_MISSING_LINK);
    }
    return Optional.empty();
  }

  private URL getTranscriptLanguagesUrlForVideo(String videoId) throws IOException {
    try {
      URIBuilder urlBuilder = new URIBuilder(API_URL);
      urlBuilder.addParameter(API_PARAM_TYPE, API_TYPE_LIST);
      urlBuilder.addParameter(API_PARAM_VIDEO, videoId);
      return urlBuilder.build().toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new IOException(e.getCause());
    }
  }

  private ImmutableList<TranscriptLanguage> parseTranscriptLanguages(Document document) {
    NodeList transcriptNodes = document.getElementsByTagName(TAG_TRACK);
    ImmutableList.Builder<TranscriptLanguage> transcriptLanguagesBuilder =
        new ImmutableList.Builder<>();
    for (int nodeIndex = 0; nodeIndex < transcriptNodes.getLength(); nodeIndex++) {
      Element transcriptElement = (Element) transcriptNodes.item(nodeIndex);
      transcriptLanguagesBuilder.add(createTranscriptLanguageFromElement(transcriptElement));
    }
    return transcriptLanguagesBuilder.build();
  }

  private TranscriptLanguage createTranscriptLanguageFromElement(Element transcriptLineElement) {
    String languageName = transcriptLineElement.getAttribute(ATTR_NAME);
    String languageCode = transcriptLineElement.getAttribute(ATTR_LANG_CODE);
    String languageTranslatedName = transcriptLineElement.getAttribute(ATTR_LANG_TRANSLATED);
    return TranscriptLanguage.builder()
        .setLanguageName(languageName)
        .setLanguageCode(languageCode)
        .setLanguageTranslatedName(languageTranslatedName)
        .build();
  }

  /**
   * Writes {@code transcriptLanguages} as Json to {@code response}.
   */
  private void writeTranscriptLanguages(HttpServletResponse response,
      List<TranscriptLanguage> transcriptLanguages) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(transcriptLanguages));
  }
}
