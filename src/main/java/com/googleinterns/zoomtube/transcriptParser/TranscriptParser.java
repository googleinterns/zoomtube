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
import com.google.appengine.api.datastore.Transaction;
import com.googleinterns.zoomtube.utils.TranscriptLineUtil;
import java.io.IOException;
import java.net.MalformedURLException;
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

/**
 * Fetches and parses English transcript XML from the Google Video Timedtext API, and
 * stores the lines in datastore.
 */
public final class TranscriptParser {
  /** Transcripts are generated using the Google Video Timedtext API. */
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
    URL url = getTranscriptUrlForVideo(videoId);
    Document document = fetchUrlAsXmlDocument(url);
    putTranscriptLinesInDatastore(lectureKey, document);
  }

  private URL getTranscriptUrlForVideo(String videoId) throws IOException {
    try {
      URIBuilder urlBuilder = new URIBuilder(API_URL);
      urlBuilder.addParameter(API_PARAM_LANG, API_LANG_ENGLISH);
      urlBuilder.addParameter(API_PARAM_VIDEO, videoId);
      return urlBuilder.build().toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new IOException(e.getCause());
    }
  }

  /**
   * Returns a Document for the XML at {@code url}.
   *
   * @param url Indicates the url to fetch and parse.
   * @throws IOException if there is an error parsing the transcript.
   */
  private Document fetchUrlAsXmlDocument(URL url) throws IOException {
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(url.openStream());
      document.getDocumentElement().normalize();
      return document;
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException(e.getCause());
    }
  }

  /**
   * Puts each transcript line from {@code document} in datastore as its own entity.
   *
   * @param lectureKey Indicates the lecture key to group the transcript lines under.
   * @param document The XML file containing the transcript lines.
   */
  private void putTranscriptLinesInDatastore(Key lectureKey, Document document) {
    Transaction transaction = datastore.beginTransaction();

    try {
      NodeList transcriptNodes = document.getElementsByTagName(TAG_TEXT);
      for (int nodeIndex = 0; nodeIndex < transcriptNodes.getLength(); nodeIndex++) {
        Element transcriptElement = (Element) transcriptNodes.item(nodeIndex);
        Entity transcriptLineEntity =
            createTranscriptLineFromElement(lectureKey, transcriptElement);
        datastore.put(transaction, transcriptLineEntity);
      }

      transaction.commit();
    } finally {
      // If the transaction was interruped, we should make sure it is rolled back
      // to avoid a partial datastore commit.
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Creates a Transcript Line entity from the XML {@code transcriptLineElement} as part of the
   * transcript for the lecture referenced by {@code lectureKey}.
   */
  private Entity createTranscriptLineFromElement(Key lectureKey, Element transcriptLineElement) {
    String lineContent = StringEscapeUtils.unescapeXml(transcriptLineElement.getTextContent());

    float lineStartSeconds = Float.parseFloat(transcriptLineElement.getAttribute(ATTR_START));
    float lineDurationSeconds = Float.parseFloat(transcriptLineElement.getAttribute(ATTR_DURATION));
    // I couldn't find any official way to convert a float seconds to long milliseconds without
    // losing precision.
    long lineStartMs = Math.round(lineStartSeconds * MILLISECONDS_PER_SECOND);
    long lineDurationMs = Math.round(lineDurationSeconds * MILLISECONDS_PER_SECOND);
    long lineEndMs = lineStartMs + lineDurationMs;

    return TranscriptLineUtil.createEntity(
        lectureKey, lineContent, lineStartMs, lineDurationMs, lineEndMs);
  }
}
