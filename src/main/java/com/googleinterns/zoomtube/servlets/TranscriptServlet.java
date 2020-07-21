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
import com.google.gson.Gson;
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
import com.googleinterns.zoomtube.data.Line;
import com.google.common.collect.ImmutableList;

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
    long lectureId = 123456789; //Long.parseLong(request.getParameter(PARAM_LECTURE));

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
        Node node = nodeList.item(nodeIndex);
        Element element = (Element) node;
        String lineStart = element.getAttribute(START_ATTRIBUTE);
        String lineDuration = element.getAttribute(DURATION_ATTRIBUTE);
        String lineContent = node.getTextContent();
        createEntity(lectureId, lineStart, lineDuration, lineContent);

        // TODO: Remove print statement. It is currently here for display purposes.
        System.out.println(lineStart + " " + lineDuration + " "
            + " " + lineContent);
      }
    } catch (ParserConfigurationException | SAXException e) {
      // TODO: alert the user.
      System.out.println("XML parsing error");
    }

    response.setStatus(200);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long lectureId = 123456789; //Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(PARAM_LECTURE, lectureId);
    Filter lectureFilter = new FilterPredicate(Comment.PROP_LECTURE, FilterOperator.EQUAL, lecture);

    Query query = new Query(Comment.ENTITY_KIND)setFilter(lectureFilter);
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

  private void createEntity(
      long lectureId, String lineStart, String lineDuration, String lineContent) {
    Entity lineEntity = new Entity(Line.ENTITY_KIND);
    lineEntity.setProperty(Line.PROP_LECTURE, KeyFactory.createKey(PARAM_LECTURE, lectureId));
    lineEntity.setProperty(LINE.PROP_START, lineStart);
    lineEntity.setProperty(LINE.PROP_DURATION, lineDuration);
    lineEntity.setProperty(LINE.PROP_CONTENT, lineContent);

    this.datastore.put(lineEntity);
  }
}
