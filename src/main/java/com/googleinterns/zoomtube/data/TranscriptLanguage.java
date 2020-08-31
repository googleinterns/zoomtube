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
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;

/** Contains data pertaining to a single line of transcript. */
@GenerateTypeAdapter
@AutoValue
public abstract class TranscriptLanguage {
  /** Returns the key for the transcript. */
  public abstract Key transcriptKey();

  /** Returns the key for the lecture. */
  public abstract Key lectureKey();

  /** Returns the starting timestamp for the transcript line in milliseconds. */
  public abstract long startTimestampMs();

  /** Returns the duration of the transcript line in milliseconds. */
  public abstract long durationMs();

  /** Returns the ending timestamp for the transcript line in milliseconds. */
  public abstract long endTimestampMs();

  /** Returns the text content of the transcript line. */
  public abstract String content();

  /**
   * Returns a builder instance that can be used to create TranscriptLines.
   */
  public static Builder builder() {
    return new AutoValue_TranscriptLanguage.Builder();
  }

  /**
   * Returns a builder instance that can be used to create TranscriptLines.
   */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setTranscriptKey(Key transcriptKey);
    public abstract Builder setLectureKey(Key lectureKey);
    public abstract Builder setStartTimestampMs(long startTimestampMs);
    public abstract Builder setDurationMs(long durationMs);
    public abstract Builder setEndTimestampMs(long endTimestampMs);
    public abstract Builder setContent(String content);
    public abstract TranscriptLine build();
  }
}
