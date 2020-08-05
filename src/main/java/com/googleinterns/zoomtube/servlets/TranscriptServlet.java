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
import com.googleinterns.zoomtube.utils.TranscriptLineUtil;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides the transcript for a given lecture.
 */
@WebServlet("/transcript")
public class TranscriptServlet extends HttpServlet {
  public static final String XML_URL_TEMPLATE = "http://video.google.com/timedtext?lang=en&v=";
  public static final String PARAM_LECTURE = "lecture";
  public static final String PARAM_LECTURE_ID = "id";
  public static final String PARAM_VIDEO_ID = "video";
  public static final String ATTR_START = "start";
  public static final String ATTR_DURATION = "dur";
  public static final String TAG_TEXT = "text";

  private DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Initializes the servlet with {@code testDatastore} created during testing.
   *
   * <p>The unit tests need access to the datastore to check that doPost() puts
   * the entities in datastore.
   */
  @VisibleForTesting
  void init(DatastoreService testDatastore) {
    datastore = testDatastore;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String videoId = request.getParameter(PARAM_VIDEO_ID);
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE_ID));
    Document document = getTranscriptXmlAsDocument(videoId).get();
    putTranscriptLinesInDatastore(lectureId, document);
  }

  /**
   * Returns the transcript for a video as a document. Otherwise, returns Optional.empty()
   * if there is a parsing error.
   *
   * @param videoId Indicates the video to extract the transcript from.
   */
  private Optional<Document> getTranscriptXmlAsDocument(String videoId) throws IOException {
    String transcriptXMLUrl = XML_URL_TEMPLATE + videoId;

    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(new URL(transcriptXMLUrl).openStream());
      document.getDocumentElement().normalize();
      return Optional.of(document);
    } catch (ParserConfigurationException | SAXException e) {
      // TODO: Alert the user.
      System.out.println("XML parsing error");
      return Optional.empty();
    }
  }

  /**
   * Puts each transcript line from {@code document} in datastore as its own entity.
   *
   * @param lectureId Indicates the lecture id to group the transcript lines under.
   * @param document The XML file containing the transcript lines.
   */
  private void putTranscriptLinesInDatastore(long lectureId, Document document) {
    NodeList nodeList = document.getElementsByTagName(TAG_TEXT);
    for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
      Node node = nodeList.item(nodeIndex);
      Element element = (Element) node;
      String lineContent = node.getTextContent();
      Float lineStart = Float.parseFloat(element.getAttribute(ATTR_START));
      Float lineDuration = Float.parseFloat(element.getAttribute(ATTR_DURATION));
      Float lineEnd = lineStart.floatValue() + lineDuration.floatValue();
      datastore.put(TranscriptLineUtil.createEntity(
          lectureId, lineContent, lineStart, lineDuration, lineEnd));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE_ID));
    PreparedQuery preparedQuery = getLectureTranscriptQuery(lectureId);
    ImmutableList<TranscriptLine> transcriptLines = getTranscriptLines(preparedQuery);
    writeTranscriptList(response, transcriptLines);
  }

  /**
   * Returns the query for the lecture transcripts based on lecture id indicated in {@code
   * lectureId}.
   */
  private PreparedQuery getLectureTranscriptQuery(long lectureId) {
    Key lectureKey = KeyFactory.createKey(PARAM_LECTURE, lectureId);
    Filter lectureFilter =
        new FilterPredicate(TranscriptLineUtil.LECTURE, FilterOperator.EQUAL, lectureKey);

    Query query = new Query(TranscriptLineUtil.KIND)
                      .setFilter(lectureFilter)
                      .addSort(TranscriptLineUtil.START, SortDirection.ASCENDING);
    return datastore.prepare(query);
  }

  /**
   * Returns the transcript lines in {@code preparedQuery}.
   */
  private ImmutableList<TranscriptLine> getTranscriptLines(PreparedQuery preparedQuery) {
    ImmutableList.Builder<TranscriptLine> lineBuilder = new ImmutableList.Builder<>();
    for (Entity entity : preparedQuery.asQueryResultIterable()) {
      lineBuilder.add(TranscriptLineUtil.createTranscriptLine(entity));
    }
    return lineBuilder.build();
  }

  /**
   * Writes {@code transcriptLines} as Json to {@code response}.
   */
  private void writeTranscriptList(HttpServletResponse response,
      ImmutableList<TranscriptLine> transcriptLines) throws IOException {
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(transcriptLines));
  }
}
