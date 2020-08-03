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
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
  private static final String TRANSCRIPT_XML_URL_TEMPLATE =
      "http://video.google.com/timedtext?lang=en&v=";
  public static final String ATTR_START = "start";
  public static final String ATTR_DURATION = "dur";
  public static final String TAG_TEXT = "text";
  public static final String PARAM_LECTURE = "lecture";
  public static final String PARAM_LECTURE_ID = "id";
  public static final String PARAM_VIDEO_ID = "video";

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
    Document document = getTranscriptXmlAsDocument(request).get();
    putTranscriptLinesInDatastore(request, document);
  }

  /**
   * Returns the transcript for a video as a document. Otherwise, returns Optional.empty()
   * if there is a parsing error.
   *
   * @param request Indicates the video to extract the transcript from.
   */
  private Optional<Document> getTranscriptXmlAsDocument(HttpServletRequest request)
      throws IOException {
    String videoId = request.getParameter(PARAM_VIDEO_ID);
    String transcriptXMLUrl = TRANSCRIPT_XML_URL_TEMPLATE + videoId;

    final Document document;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      document = documentBuilder.parse(new URL(transcriptXMLUrl).openStream());
      document.getDocumentElement().normalize();
    } catch (ParserConfigurationException | SAXException e) {
      // TODO: Alert the user.
      System.out.println("XML parsing error");
      return Optional.empty();
    }
    return Optional.of(document);
  }

  /**
   * Puts each transcript line from {@code document} in datastore as its own entity.
   *
   * @param request Indicates the lecture key to group the transcript lines under.
   * @param document The XML file containing the transcript lines.
   */
  private void putTranscriptLinesInDatastore(HttpServletRequest request, Document document) {
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE_ID));
    NodeList nodeList = document.getElementsByTagName(TAG_TEXT);
    for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
      Node node = nodeList.item(nodeIndex);
      datastore.put(createLineEntity(node, lectureId));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PreparedQuery preparedQuery = getLectureTranscriptQuery(request);
    ImmutableList<TranscriptLine> transcriptLines = getTranscriptLines(preparedQuery);
    writeResponseAsJson(response, transcriptLines);
  }

  /**
   * Returns the query for the lecture transcripts based on lecture id indicated in {@code request}.
   */
  private PreparedQuery getLectureTranscriptQuery(HttpServletRequest request) {
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE_ID));
    Key lectureKey = KeyFactory.createKey(PARAM_LECTURE, lectureId);
    Filter lectureFilter =
        new FilterPredicate(TranscriptLine.PROP_LECTURE, FilterOperator.EQUAL, lectureKey);

    Query query = new Query(TranscriptLine.ENTITY_KIND)
                      .setFilter(lectureFilter)
                      .addSort(TranscriptLine.PROP_START, SortDirection.ASCENDING);
    return datastore.prepare(query);
  }

  private ImmutableList<TranscriptLine> getTranscriptLines(PreparedQuery preparedQuery) {
    ImmutableList.Builder<TranscriptLine> lineBuilder = new ImmutableList.Builder<>();
    for (Entity entity : preparedQuery.asQueryResultIterable()) {
      lineBuilder.add(TranscriptLine.fromLineEntity(entity));
    }
    return lineBuilder.build();
  }

  /**
   * Writes {@code list} as Json to {@code response}.
   */
  private void writeResponseAsJson(HttpServletResponse response, ImmutableList<TranscriptLine> list)
      throws IOException {
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(list));
  }

  /**
   * Creates a line entity using the attributes from {@code node} and {@code lectureId}.
   */
  private Entity createLineEntity(Node node, long lectureId) {
    Element element = (Element) node;
    String lineContent = node.getTextContent();
    Float lineStart = Float.parseFloat(element.getAttribute(ATTR_START));
    Float lineDuration = Float.parseFloat(element.getAttribute(ATTR_DURATION));
    Float lineEnd = lineStart.floatValue() + lineDuration.floatValue();
    Entity lineEntity = new Entity(TranscriptLine.ENTITY_KIND);
    // TODO: Change PARAM_LECTURE to Lecture.ENTITY_KIND once lectureServlet is
    // merged to this branch.
    lineEntity.setProperty(
        TranscriptLine.PROP_LECTURE, KeyFactory.createKey(PARAM_LECTURE, lectureId));
    lineEntity.setProperty(TranscriptLine.PROP_CONTENT, lineContent);
    lineEntity.setProperty(
        TranscriptLine.PROP_START, new Date(TimeUnit.SECONDS.toMillis(lineStart.longValue())));
    lineEntity.setProperty(TranscriptLine.PROP_DURATION,
        new Date(TimeUnit.SECONDS.toMillis(lineDuration.longValue())));
    lineEntity.setProperty(
        TranscriptLine.PROP_END, new Date(TimeUnit.SECONDS.toMillis(lineEnd.longValue())));
    return lineEntity;
  }
}
