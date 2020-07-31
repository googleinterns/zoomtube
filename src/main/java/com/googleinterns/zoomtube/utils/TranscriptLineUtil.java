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

import com.googleinterns.zoomtube.data.TranscriptLine;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** Provides methods to create TranscriptLine Entities and TranscriptLine objects. */
public class TranscriptLineUtil {
  private static final String TRANSCRIPT_XML_URL_TEMPLATE =
      "http://video.google.com/timedtext?lang=en&v=";
  public static final String ATTR_START = "start";
  public static final String ATTR_DURATION = "dur";
  public static final String TAG_TEXT = "text";
  public static final String PARAM_LECTURE = "lecture";
  public static final String PARAM_LECTURE_ID = "id";
  public static final String PARAM_VIDEO_ID = "video";
  public static final String ENTITY_KIND = "TranscriptLine";
  public static final String PROP_LECTURE = "lecture";
  public static final String PROP_START = "start";
  public static final String PROP_DURATION = "duration";
  public static final String PROP_CONTENT = "content";
  public static final String PROP_END = "end";

  /**
   * Creates and returns a TranscriptLine from a datastore {@code entity} using
   * the property names defined in this class.
   */
  public static TranscriptLine fromLineEntity(Entity entity) {
    Key key = entity.getKey();
    Key lecture = (Key) entity.getProperty(PROP_LECTURE);
    Date start = (Date) entity.getProperty(PROP_START);
    Date duration = (Date) entity.getProperty(PROP_DURATION);
    Date end = (Date) entity.getProperty(PROP_END);
    String content = (String) entity.getProperty(PROP_CONTENT);
    return TranscriptLine.create(key, lecture, start, duration, end, content);
  }

  /**
   * Creates a line entity using the attributes from {@code node} and {@code lectureId}.
   */
  private static Entity createEntity(Node node, long lectureId) {
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