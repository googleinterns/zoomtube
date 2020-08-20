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
import com.googleinterns.zoomtube.data.IconFeedback;

/** Utility for creating Feedback Entities and Feedback. */
public final class IconFeedbackUtil {
  public static final String KIND = "Feedback";
  public static final String LECTURE = "lecture";
  public static final String TIMESTAMP_MS = "timestampMs";
  public static final String TYPE = "type";

  /** Creates and returns a Feedback from {@code entity}. */
  public static IconFeedback createIconFeedback(Entity entity) {
    Key iconFeedbackKey = entity.getKey();
    Key lectureKey = (Key) entity.getProperty(LECTURE);
    long timestampMs = (long) entity.getProperty(TIMESTAMP_MS);
    IconFeedback.Type type = IconFeedback.Type.valueOf((String) entity.getProperty(TYPE));

    IconFeedback.Builder builder = IconFeedback.builder()
                                       .setIconFeedbackKey(iconFeedbackKey)
                                       .setLectureKey(lectureKey)
                                       .setTimestampMs(timestampMs)
                                       .setType(type);
    return builder.build();
  }

  /**
   * Creates and returns an IconFeedback entity using {@code lectureKey},
   * {@code timestampMs}, and {@code type}.
   */
  public static Entity createEntity(Key lectureKey, long timestampMs, IconFeedback.Type type) {
    Entity entity = new Entity(KIND, lectureKey);
    entity.setProperty(LECTURE, lectureKey);
    entity.setProperty(TIMESTAMP_MS, timestampMs);
    entity.setProperty(TYPE, type.toString());
    return entity;
  }

  private IconFeedbackUtil(){};
}
