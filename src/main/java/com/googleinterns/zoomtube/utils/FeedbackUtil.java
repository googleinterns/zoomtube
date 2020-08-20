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

/** Provides methods to create Comment Entities and Comments. */
public final class FeedbackUtil {
  public static final String KIND = "Feedback";
  public static final String LECTURE = "lecture";
  public static final String TIMESTAMP_SECONDS = "timestamp_seconds";
  public static final String TYPE = "type";

  /**
   * Creates and returns a Feedback using the properties of {@code entity}.
   */
  public static Feedback createFeedback(Entity entity) {
    Key feedbackKey = entity.getKey();
    Key lectureKey = (Key) entity.getProperty(LECTURE);
    long timestampSeconds = (long) entity.getProperty(TIMESTAMP_SECONDS);
    Feedback.Type type = Feedback.Type.valueOf((String) entity.getProperty(TYPE));

    Feedback.Builder builder = Feedback.builder()
                                   .setFeedbackKey(feedbackKey)
                                   .setLectureKey(lectureKey)
                                   .setTimestampSeconds(timestampSeconds)
                                   .setType(type);
    return builder.build();
  }

  /**
   * Creates and returns an entity with the specified properties.
   */
  public static Entity createFeedbackEntity(
      Key lectureKey, long timestampSeconds, Feedback.Type type) {
    Entity entity = new Entity(KIND);
    entity.setProperty(LECTURE, lectureKey);
    entity.setProperty(TIMESTAMP_SECONDS, timestampSeconds);
    entity.setProperty(TYPE, type.toString());
    return entity;
  }

  private FeedbackUtil(){};
}
