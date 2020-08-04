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

package com.googleinterns.zoomtube.utils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.googleinterns.zoomtube.data.TranscriptLine;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Provides methods to create TranscriptLine Entities and TranscriptLine objects. */
public class TranscriptLineUtil {
  public static final String XML_URL_TEMPLATE = "http://video.google.com/timedtext?lang=en&v=";
  public static final String ATTR_START = "start";
  public static final String ATTR_DURATION = "dur";
  public static final String TAG_TEXT = "text";

  public static final String PARAM_LECTURE = "lecture";
  public static final String PARAM_LECTURE_ID = "id";
  public static final String PARAM_VIDEO_ID = "video";
  public static final String ENTITY_KIND = "TranscriptLine";

  public static final String LECTURE = "lecture";
  public static final String START = "start";
  public static final String DURATION = "duration";
  public static final String CONTENT = "content";
  public static final String PROP_END = "end";

  /**
   * Creates and returns a TranscriptLine from a datastore {@code entity} using
   * the property names defined in this class.
   */
  public static TranscriptLine fromEntity(Entity entity) {
    Key transcriptKey = entity.getKey();
    Key lectureKey = (Key) entity.getProperty(LECTURE);
    Date start = (Date) entity.getProperty(START);
    Date duration = (Date) entity.getProperty(DURATION);
    Date end = (Date) entity.getProperty(PROP_END);
    String content = (String) entity.getProperty(CONTENT);
    return TranscriptLine.builder()
        .setTranscriptKey(transcriptKey)
        .setLectureKey(lectureKey)
        .setStart(start)
        .setDuration(duration)
        .setEnd(end)
        .setContent(content)
        .build();
  }

  /**
   * Creates a line entity using the attributes from {@code node} and {@code lectureId}.
   */
  public static Entity createEntity(Node node, long lectureId) {
    Element element = (Element) node;
    String lineContent = node.getTextContent();
    Float lineStart = Float.parseFloat(element.getAttribute(ATTR_START));
    Float lineDuration = Float.parseFloat(element.getAttribute(ATTR_DURATION));
    Float lineEnd = lineStart.floatValue() + lineDuration.floatValue();
    Entity lineEntity = new Entity(ENTITY_KIND);
    // TODO: Change PARAM_LECTURE to Lecture.ENTITY_KIND once lectureServlet is
    // merged to this branch.
    lineEntity.setProperty(LECTURE, KeyFactory.createKey(PARAM_LECTURE, lectureId));
    lineEntity.setProperty(CONTENT, lineContent);
    lineEntity.setProperty(START, new Date(TimeUnit.SECONDS.toMillis(lineStart.longValue())));
    lineEntity.setProperty(
        DURATION, new Date(TimeUnit.SECONDS.toMillis(lineDuration.longValue())));
    lineEntity.setProperty(PROP_END, new Date(TimeUnit.SECONDS.toMillis(lineEnd.longValue())));
    return lineEntity;
  }
}