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
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.Line;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides the transcript for the lecture.
 */
@WebServlet("/transcript")
public class TranscriptServlet extends HttpServlet {
  private static final String TRANSCRIPT_XML_URL_TEMPLATE =
      "http://video.google.com/timedtext?lang=en&v=";
  private static final String START_ATTRIBUTE = "start";
  private static final String DURATION_ATTRIBUTE = "dur";
  private static final String TEXT_TAG = "text";
  private static final String TEST_VIDEO_ID = "3ymwOvzhwHs";
  private static final String PARAM_LECTURE = "Lecture";

  private static DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    this.datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: Use the client-provided lectureId.
    long lectureId = Long.parseLong(request.getParameter("id"));

    try {
      // Later, the video ID will be passed in from another servlet.
      String transcriptXMLUrl = TRANSCRIPT_XML_URL_TEMPLATE + TEST_VIDEO_ID;
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new URL(transcriptXMLUrl).openStream());
      doc.getDocumentElement().normalize();

      NodeList nodeList = doc.getElementsByTagName(TEXT_TAG);
      // A for loop is used because NodeList is not an Iterable.
      for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
        // TODO: Remove, currently here temporarily for testing purposes.
        if (nodeIndex == 5) {
          break;
        }
        Node node = nodeList.item(nodeIndex);
        createEntity(node, lectureId);
      }
    } catch (ParserConfigurationException | SAXException e) {
      // TODO: alert the user.
      System.out.println("XML parsing error");
    }
    // For testing purposes, will delete later.
    String testParamsString = "id=123456789&video=3ymwOvzhwHs";
    response.setStatus(200);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long lectureId = Long.parseLong(request.getParameter("id"));
    Key lecture = KeyFactory.createKey(PARAM_LECTURE, lectureId);

    Filter lectureFilter = new FilterPredicate(Line.PROP_LECTURE, FilterOperator.EQUAL, lecture);

    Query query = new Query(Line.ENTITY_KIND)
                      .setFilter(lectureFilter)
                      .addSort(Line.PROP_START, SortDirection.ASCENDING);
    PreparedQuery pq = datastore.prepare(query);

    ImmutableList.Builder<Line> lineBuilder = new ImmutableList.Builder<>();
    for (Entity entity : pq.asQueryResultIterable()) {
      lineBuilder.add(Line.fromEntity(entity));
    }
    ImmutableList<Line> lines = lineBuilder.build();

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(lines));
  }

  private void createEntity(Node node, long lectureId) {
    // TODO: Reorder.
    Element element = (Element) node;
    String lineStart = element.getAttribute(START_ATTRIBUTE);
    String lineDuration = element.getAttribute(DURATION_ATTRIBUTE);
    String lineContent = node.getTextContent();

    Entity lineEntity = new Entity(Line.ENTITY_KIND);
    lineEntity.setProperty(Line.PROP_LECTURE, KeyFactory.createKey(PARAM_LECTURE, lectureId));
    lineEntity.setProperty(Line.PROP_START, lineStart);
    lineEntity.setProperty(Line.PROP_DURATION, lineDuration);
    lineEntity.setProperty(Line.PROP_CONTENT, lineContent);

    // TODO: Remove print statement. It is currently here for display purposes.
    System.out.println(lineStart + " " + lineDuration + " "
        + " " + lineContent);

    this.datastore.put(lineEntity);
  }
}
