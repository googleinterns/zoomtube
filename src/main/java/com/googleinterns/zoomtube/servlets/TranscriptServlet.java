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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.TranscriptLine;
import com.googleinterns.zoomtube.utils.LectureUtil;
import com.googleinterns.zoomtube.utils.TranscriptLineUtil;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides the transcript for a given lecture.
 */
public class TranscriptServlet extends HttpServlet {
  private DatastoreService datastore;
  @VisibleForTesting static final String PARAM_ID = "id";
  private static final String ERROR_MISSING_ID = "Missing id parameter.";

  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> error = validateGetRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
      return;
    }

    long lectureId = Long.parseLong(request.getParameter(PARAM_ID));
    PreparedQuery preparedQuery = getLectureTranscriptQuery(lectureId);
    ImmutableList<TranscriptLine> transcriptLines = getTranscriptLines(preparedQuery);
    writeTranscriptLines(response, transcriptLines);
  }

  private Optional<String> validateGetRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_ID) == null) {
      return Optional.of(ERROR_MISSING_ID);
    }
    return Optional.empty();
  }

  /**
   * Returns the query for the lecture transcripts based on lecture id indicated in {@code
   * lectureId}.
   */
  private PreparedQuery getLectureTranscriptQuery(long lectureId) {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    Filter lectureFilter =
        new FilterPredicate(TranscriptLineUtil.LECTURE, FilterOperator.EQUAL, lectureKey);

    Query query = new Query(TranscriptLineUtil.KIND)
                      .setFilter(lectureFilter)
                      .addSort(TranscriptLineUtil.START_TIMESTAMP_MS, SortDirection.ASCENDING);
    return datastore.prepare(query);
  }

  /**
   * Returns the transcript lines in {@code preparedQuery}.
   */
  private ImmutableList<TranscriptLine> getTranscriptLines(PreparedQuery preparedQuery) {
    ImmutableList.Builder<TranscriptLine> lineBuilder = new ImmutableList.Builder<>();
    for (Entity transcriptLine : preparedQuery.asQueryResultIterable()) {
      lineBuilder.add(TranscriptLineUtil.createTranscriptLine(transcriptLine));
    }
    return lineBuilder.build();
  }

  /**
   * Writes {@code transcriptLines} as Json to {@code response}.
   */
  private void writeTranscriptLines(HttpServletResponse response,
      ImmutableList<TranscriptLine> transcriptLines) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(transcriptLines));
  }
}
