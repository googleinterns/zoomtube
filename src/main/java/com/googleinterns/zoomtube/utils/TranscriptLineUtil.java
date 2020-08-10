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

/** Provides methods to create TranscriptLine Entities and TranscriptLine objects. */
public final class TranscriptLineUtil {
  public static final String KIND = "TranscriptLine";
  public static final String LECTURE = "lecture";
  public static final String START_TIMESTAMP_MILLISECONDS = "start_milliseconds";
  public static final String DURATION_MILLISECONDS = "duration_milliseconds";
  public static final String CONTENT = "content";
  public static final String END_TIMESTAMP_MILLISECONDS = "end_milliseconds";

  /**
   * Creates and returns a TranscriptLine from a datastore {@code entity} using
   * the property names defined in this class.
   */
  public static TranscriptLine createTranscriptLine(Entity entity) {
    Key transcriptKey = entity.getKey();
    Key lectureKey = (Key) entity.getProperty(LECTURE);
    long start = (long) entity.getProperty(START_TIMESTAMP_MILLISECONDS);
    long duration = (long) entity.getProperty(DURATION_MILLISECONDS);
    long end = (long) entity.getProperty(END_TIMESTAMP_MILLISECONDS);
    String content = (String) entity.getProperty(CONTENT);
    return TranscriptLine.builder()
        .setTranscriptKey(transcriptKey)
        .setLectureKey(lectureKey)
        .setStartTimestampMilliseconds(start)
        .setDurationMilliseconds(duration)
        .setEndTimestampMilliseconds(end)
        .setContent(content)
        .build();
  }

  /**
   * Creates a transcript line entity.
   *
   * @param lectureId The id of the lecture that the transcript line is a part of.
   * @param lineContent The text content of the transcript line.
   * @param lineStart The starting timestamp for the transcript line in milliseconds.
   * @param lineDuration The duration for the transcript line in milliseconds.
   * @param lineEnd The ending timestamp for the transcript line in milliseconds.
   */
  public static Entity createEntity(
      long lectureId, String lineContent, long lineStart, long lineDuration, long lineEnd) {
    Entity lineEntity = new Entity(KIND);
    lineEntity.setProperty(LECTURE, KeyFactory.createKey(LectureUtil.KIND, lectureId));
    lineEntity.setProperty(CONTENT, lineContent);
    lineEntity.setProperty(START_TIMESTAMP_MILLISECONDS, lineStart);
    lineEntity.setProperty(DURATION_MILLISECONDS, lineDuration);
    lineEntity.setProperty(END_TIMESTAMP_MILLISECONDS, lineEnd);
    return lineEntity;
  }

  private TranscriptLineUtil() {}
}
