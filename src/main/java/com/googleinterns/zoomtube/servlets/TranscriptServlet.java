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

import java.io.IOException;
import java.net.URL;
import javax.annotation.Nullable;
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
  private static final String START_ATTRIBUTE = "start";
  private static final String DURATION_ATTRIBUTE = "dur";
  private static final String TEXT_TAG = "text";
  private static final String TEST_VIDEO_ID = "3ymwOvzhwHs";

  @Override
  public void init() throws ServletException {
    // TODO: Implement Transcript.
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: Pass the video ID from another servlet.
    String transcriptXMLUrl = TRANSCRIPT_XML_URL_TEMPLATE + TEST_VIDEO_ID;
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder documentBuilder;
    final Document document;
    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      document = documentBuilder.parse(new URL(transcriptXMLUrl).openStream());
      document.getDocumentElement().normalize();
    } catch (ParserConfigurationException | SAXException e) {
      // TODO: Alert the user.
      System.out.println("XML parsing error");
      return;
    }
    NodeList nodeList = document.getElementsByTagName(TEXT_TAG);
    for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
      Node node = nodeList.item(nodeIndex);
      Element element = (Element) node;
      String lineStart = element.getAttribute(START_ATTRIBUTE);
      String lineDuration = element.getAttribute(DURATION_ATTRIBUTE);
      String lineContent = node.getTextContent();
      // TODO: Remove print statement. It is currently here for display purposes.
      System.out.println(lineStart + " " + lineDuration + " "
          + " " + lineContent);
    }
  }
}
