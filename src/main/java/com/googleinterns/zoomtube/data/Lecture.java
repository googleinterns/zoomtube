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
import com.googleinterns.zoomtube.utils.LectureEntityFields;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;

/** Stores data for lectures added to site. */
@GenerateTypeAdapter
@AutoValue
public abstract class Lecture {
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
  public static Lecture fromLectureEntity(Entity entity) {
    Key key = entity.getKey();
    String lectureName = (String) entity.getProperty(LectureEntityFields.NAME);
    String videoUrl = (String) entity.getProperty(LectureEntityFields.VIDEO_URL);
    String videoId = (String) entity.getProperty(LectureEntityFields.VIDEO_ID);
    return Lecture.create(key, lectureName, videoUrl, videoId);
  }
}