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
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;


/** Contains data pertaining to a single line of transcript. */
@GenerateTypeAdapter
@AutoValue
public abstract class TranscriptLine {
  public static final String ENTITY_KIND = "TranscriptLine";
  public static final String PROP_LECTURE = "lecture";
  public static final String PROP_START = "start";
  public static final String PROP_DURATION = "duration";
  public static final String PROP_CONTENT = "content";
  public static final String PROP_END = "end";

  /**
   * Creates a TranscriptLine object.
   *
   * @param key The key for the transcript.
   * @param lecture The key for the lecture.
   * @param start The starting time for the lecture line in seconds.
   * @param duration The number of seconds that the timestamp lasts for.
   * @param content The text content of the transcript line.
   */
  public static TranscriptLine create(
      Key key, Key lecture, Date start, Date duration, Date end, String content) {
    return new AutoValue_TranscriptLine(key, lecture, start, duration, end, content);
  }

  public abstract Key key();
  public abstract Key lecture();
  public abstract Date start();
  public abstract Date duration();
  public abstract Date end();
  public abstract String content();

  /**
   * Creates a {@code TranscriptLine} from a datastore {@code Entity} using the
   * property names defined in this class.
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
}
