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

import com.google.appengine.api.datastore.Key;
import com.google.auto.value.AutoValue;

/** Contains data related to feedback left by clicking icons. */
@AutoValue
public abstract class Feedback {
  public static enum Type {
    GOOD,
    BAD,
    TOO_FAST,
    TOO_SLOW,
  }

  /** Returns feedback's datastore entity key. */
  public abstract Key feedbackKey();

  /** Returns lucture entity key associated with feedback. */
  public abstract Key lectureKey();

  /** Returns timestamps in milliseconds of when feedback icon was clicked. */
  public abstract Long timestampMs();

  /** Returns type of Feedback. */
  public abstract Type type();

  public static Builder builder() {
    return new AutoValue_Feedback.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setFeedbackKey(Key feedbackKey);
    public abstract Builder setLectureKey(Key lectureKey);
    public abstract Builder setTimestampMs(Long timestampMs);
    public abstract Builder setType(Type type);

    public abstract Feedback build();
  }
}
