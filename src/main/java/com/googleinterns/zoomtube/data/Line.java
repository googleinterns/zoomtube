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
import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.auto.value.AutoValue;
import java.util.Date;
import javax.annotation.Nullable;

/** Contains data pertaining a single line of transcript. */
@AutoValue
public abstract class Line {
  public static final String ENTITY_KIND = "Line";
  public static final String PROP_LECTURE = "lecture";
  public static final String PROP_START = "start";
  public static final String PROP_DURATION = "duration";
  public static final String PROP_CONTENT = "content";

  /**
   * Creates a {@code Line} object.
   * @param key The key for the transcript.
   * @param lecture The key for the lecture.
   * @param start The starting timestamp for the lecture line in seconds.
   * @param duration The number of seconds that the timestamp lasts for.
   * @param content The text content of the transcript line.
   */
  public static Line create(Key key, Key lecture, String start, String duration, String content) {
    return new AutoValue_Line(key, lecture, start, duration, content);
  }
  public abstract Key key();

  public abstract Key lecture();

  public abstract String start();

  public abstract String duration();

  public abstract String content();

  /**
   * Creates a {@code Line} from a datastore {@link com.google.appengine.api.datastore.Entity}
   * using the property names defined in this class.
   */
  public static Line fromEntity(Entity entity) {
    Key key = entity.getKey();
    Key lecture = (Key) entity.getProperty(PROP_LECTURE);
    String start = (String) entity.getProperty(PROP_START);
    String duration = (String) entity.getProperty(PROP_DURATION);
    String content = (String) entity.getProperty(PROP_CONTENT);
    return Line.create(key, lecture, start, duration, content);
  }
}