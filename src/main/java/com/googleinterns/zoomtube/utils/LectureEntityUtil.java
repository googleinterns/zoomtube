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

import com.googleinterns.zoomtube.data.Lecture;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/** Provides methods to create Lecture Entities and Lectures. */
public class LectureEntityUtil {
  /** Used to create a Lecture Entity and its fields. */
  public static final String KIND = "Lecture";
  public static final String NAME = "lectureName";
  public static final String VIDEO_URL = "videoUrl";
  public static final String VIDEO_ID = "video-id";
  public static final String ID = "id";

  /** Returns a Lecture from {@code entity}. */
  public static Lecture createLecture(Entity entity) {
    Key key = entity.getKey();
    String lectureName = (String) entity.getProperty(NAME);
    String videoUrl = (String) entity.getProperty(VIDEO_URL);
    String videoId = (String) entity.getProperty(VIDEO_ID);
    return Lecture.create(key, lectureName, videoUrl, videoId);
  }
  
  /** 
   * Creates and returns a Lecture Entity using {@code lectureName},
   * {@code videoUrl}, and {@code videoId}.
   */
  public static Entity createLectureEntity(String lectureName, String videoUrl, String videoId) {
    Entity lectureEntity = new Entity(KIND);
    lectureEntity.setProperty(NAME, lectureName);
    lectureEntity.setProperty(VIDEO_URL, videoUrl);
    lectureEntity.setProperty(VIDEO_ID, videoId);
    return lectureEntity;
  }
}