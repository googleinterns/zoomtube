
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

/** Stores data for lectures added to site. */
@AutoValue
public abstract class Lecture {
  /** Used to create an entity and its fields. */
  public static final String ENTITY_KIND = "Lecture";
  public static final String PROP_NAME = "lectureName";
  public static final String PROP_URL = "videoUrl";
  public static final String PROP_ID = "id";
  public static final String PROP_VIDEO_ID = "videoId";

  public abstract Key key();
  public abstract String lectureName();
  public abstract String videoUrl();
  public abstract String videoId();

  /**
   * Creates a Lecture.
   *
   * @param key Key of object stored in database.
   * @param lectureName Name of lecture.
   * @param videoUrl YouTube link where video is hosted.
   * @param videoId YouTube id of lecture video.
   */
  public static Lecture create(Key key, String lectureName, String videoUrl, String videoId) {
    return new AutoValue_Lecture(key, lectureName, videoUrl, videoId);
  }

  /** Returns a Lecture from {@code entity}. */
  public static Lecture fromEntity(Entity entity) {
    Key key = entity.getKey();
    String lectureName = (String) entity.getProperty(PROP_NAME);
    String videoUrl = (String) entity.getProperty(PROP_URL);
    String videoId = (String) entity.getProperty(PROP_VIDEO_ID);
    return Lecture.create(key, lectureName, videoUrl, videoId);
  }
}
