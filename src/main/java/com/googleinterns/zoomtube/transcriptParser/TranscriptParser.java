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

package com.googleinterns.zoomtube.transcriptParser;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.googleinterns.zoomtube.data.TranscriptLine;
import com.googleinterns.zoomtube.utils.TranscriptLineUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TranscriptParser {
  private static final String XML_URL_TEMPLATE = "http://video.google.com/timedtext?lang=en&v=";
  public static final String ATTR_START = "start";
  public static final String ATTR_DURATION = "dur";
  public static final String TAG_TEXT = "text";

  private static TranscriptParser uniqueParser;
  private DatastoreService datastore;

  private TranscriptParser() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  // TODO: Refactor getParser() to use double-checked locking if
  // synchronization is expensive.
  public static synchronized TranscriptParser getParser() {
    if (uniqueParser == null) {
      uniqueParser = new TranscriptParser();
    }
    return uniqueParser;
  }

  /**
   * Parses and stores the transcript lines in datastore given its {@code videoId}
   * and {@code lectureKey}.
   *
   * <p>This method is called from the {@code LectureServlet} upon adding a lecture to
   * datastore.
   */
  public void parseAndStoreTranscript(String videoId, Key lectureKey) throws IOException {
    Document document = getTranscriptXmlAsDocument(videoId).get();
    putTranscriptLinesInDatastore(lectureKey, document);
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
   * @param lectureKey Indicates the lecture key to group the transcript lines under.
   * @param document The XML file containing the transcript lines.
   */
  private void putTranscriptLinesInDatastore(Key lectureKey, Document document) {
    NodeList transcriptNodes = document.getElementsByTagName(TAG_TEXT);
    for (int nodeIndex = 0; nodeIndex < transcriptNodes.getLength(); nodeIndex++) {
      Node transcriptNode = transcriptNodes.item(nodeIndex);
      Element transcriptElement = (Element) transcriptNode;
      String lineContent = StringEscapeUtils.unescapeXml(transcriptNode.getTextContent());
      Float lineStart = Float.parseFloat(transcriptElement.getAttribute(ATTR_START));
      Float lineDuration = Float.parseFloat(transcriptElement.getAttribute(ATTR_DURATION));
      Float lineEnd = lineStart.floatValue() + lineDuration.floatValue();
      datastore.put(TranscriptLineUtil.createEntity(
          lectureKey, lineContent, lineStart, lineDuration, lineEnd));
    }
  }
}