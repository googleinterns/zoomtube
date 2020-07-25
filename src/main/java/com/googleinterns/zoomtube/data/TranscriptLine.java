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

package com.googleinterns.zoomtube.data;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.auto.value.AutoValue;
import java.util.Date;

/** Contains data pertaining a single line of transcript. */
@AutoValue
public abstract class TranscriptLine {
  public static final String ENTITY_KIND = "TranscriptLine";
  public static final String PROP_LECTURE = "lecture";
  public static final String PROP_START = "start";
  public static final String PROP_DURATION = "duration";
  public static final String PROP_CONTENT = "content";
  public static final String PROP_END = "end";

  /**
   * Creates a {@code TranscriptLine} object.
   * @param key The key for the transcript.
   * @param lecture The key for the lecture.
   * @param start The starting time for the lecture line in seconds.
   * @param duration The number of seconds that the timestamp lasts for.
   * @param content The text content of the transcript line.
   */
  // TODO: Update start to be a Date object and duration to be a long.
  public static TranscriptLine create(
      Key key, Key lecture, Date start, double duration, Date end, String content) {
    return new AutoValue_TranscriptLine(key, lecture, start, duration, end, content);
  }

  public abstract Key key();

  public abstract Key lecture();

  public abstract Date start();

  public abstract double duration();

  public abstract Date end();

  public abstract String content();

  /**
   * Creates a {@code TranscriptLine} from a datastore {@link
   * com.google.appengine.api.datastore.Entity} using the property names defined in this class.
   */
  public static TranscriptLine fromEntity(Entity entity) {
    Key key = entity.getKey();
    Key lecture = (Key) entity.getProperty(PROP_LECTURE);
    Date start = (Date) entity.getProperty(PROP_START);
    double duration = (double) entity.getProperty(PROP_DURATION);
    Date end = (Date) entity.getProperty(PROP_END);
    String content = (String) entity.getProperty(PROP_CONTENT);
    return TranscriptLine.create(key, lecture, start, duration, end, content);
  }
}