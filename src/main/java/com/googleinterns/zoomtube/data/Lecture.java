 
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

package com.google.sps.data;

import com.google.auto.value.AutoValue;

/** Stores data for lectures added to site. */
@AutoValue 
public abstract class Lecture {
  public abstract long id();
  public abstract String lectureName();
  public abstract String videoUrl();
  public abstract String videoId();

  /** 
   * Creates a Lecture.
   * @param id Id of object stored in database
   * @param lectureName Name of lecture
   * @param videoUrl YouTube link where video is hosted
   * @param videoId YouTube id of lecture video
   */
  public static Lecture create(long id, String lectureName, String videoUrl, String videoId) {
    return new AutoValue_Lecture(id, lectureName, videoUrl, videoId);
  }
}