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
import java.util.Date;

/** Contains data pertaining to a single line of transcript. */
@GenerateTypeAdapter
@AutoValue
public abstract class TranscriptLine {
  public abstract Key key();
  public abstract Key lecture();
  public abstract Date start();
  public abstract Date duration();
  public abstract Date end();
  public abstract String content();

  /**
   * Returns a builder instance that can be used to create TranscriptLines.
   */
  public static Builder builder() {
    return new AutoValue_TranscriptLine.Builder();
  }

  /**
   * Returns a builder instance that can be used to create TranscriptLines.
   */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setKey(Key key);
    public abstract Builder setLecture(Key lecture);
    public abstract Builder setStart(Date start);
    public abstract Builder setDuration(Date duration);
    public abstract Builder setEnd(Date end);
    public abstract Builder setContent(String content);
    public abstract TranscriptLine build();
  }
}