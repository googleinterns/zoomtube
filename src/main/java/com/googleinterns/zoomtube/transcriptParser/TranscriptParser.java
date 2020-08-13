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
import com.google.appengine.api.datastore.Key;
import com.googleinterns.zoomtube.utils.TranscriptLineUtil;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Parses the transcript XML and stores the lines in datastore. */
public final class TranscriptParser {
  private static final String API_URL = "http://video.google.com/timedtext";
  private static final String API_PARAM_LANG = "lang";
  private static final String API_LANG_ENGLISH = "en";
  private static final String API_PARAM_VIDEO = "v";
  private static final long MILLISECONDS_PER_SECOND = 1000;
  public static final String ATTR_START = "start";
  public static final String ATTR_DURATION = "dur";
  public static final String TAG_TEXT = "text";

  private static TranscriptParser uniqueParser;
  private DatastoreService datastore;

  /**
   * Creates a {@code TranscriptParser} instance with a datastore.
   */
  private TranscriptParser() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Returns the {@code TranscriptParser} instance if there is one. Else, creates
   * a new {@code TranscriptParser} and returns that.
   *
   * <p>Ensures that there is only one {@code TranscriptParser} is created.
   */
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
   * if there is a parsing or URL building error.
   *
   * @param videoId Indicates the video to extract the transcript from.
   */
  private Optional<Document> getTranscriptXmlAsDocument(String videoId) throws IOException {
    final URL url;
    try {
      URIBuilder urlBuilder = new URIBuilder(API_URL);
      urlBuilder.addParameter(API_PARAM_LANG, API_LANG_ENGLISH);
      urlBuilder.addParameter(API_PARAM_VIDEO, videoId);
      url = urlBuilder.build().toURL();
    } catch (URISyntaxException e) {
      // TODO: Alert the user.
      System.out.println("URL building error");
      return Optional.empty();
    }

    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(url.openStream());
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

      float lineStartSeconds = Float.parseFloat(transcriptElement.getAttribute(ATTR_START));
      float lineDurationSeconds = Float.parseFloat(transcriptElement.getAttribute(ATTR_DURATION));
      // I couldn't find any official way to convert a float seconds to long milliseconds without
      // losing precision.
      long lineStartMs = Math.round(lineStartSeconds * MILLISECONDS_PER_SECOND);
      long lineDurationMs = Math.round(lineDurationSeconds * MILLISECONDS_PER_SECOND);
      long lineEndMs = lineStartMs + lineDurationMs;

      datastore.put(TranscriptLineUtil.createEntity(
          lectureKey, lineContent, lineStartMs, lineDurationMs, lineEndMs));
    }
  }
}
