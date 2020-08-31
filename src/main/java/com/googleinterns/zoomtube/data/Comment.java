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
import com.google.appengine.api.users.User;
import com.google.auto.value.AutoValue;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.util.Date;
import java.util.Optional;

/** Contains data related to a comment in a discussion. */
@GenerateTypeAdapter
@AutoValue
public abstract class Comment {
  public static enum Type {
    QUESTION,
    NOTE,
    REPLY,
  }

  /** Returns the comment's Datastore entity key. */
  public abstract Key commentKey();

  /** Every comment is about a lecture, this returns the lecture's Datastore entity key. */
  public abstract Key lectureKey();

  /**
   * Returns the key of the comment this is a reply to, or {@code Optional.empty()} if this
   * isn't a reply.
   */
  public abstract Optional<Key> parentKey();

  /**
   * Returns the timestamp in milliseconds this comment is referencing in the video, or {@code
   * Optional.empty()} if this is a reply.
   */
  public abstract Optional<Long> timestampMs();

  /**
   * Returns the transcript line key for which this comment is for, or {@code Optional.empty()} if
   * it doesn't have one.
   */
  public abstract Optional<Key> transcriptLineKey();

  /**
   * Returns the comment's author.
   */
  public abstract User author();

  /** Returns the comment's content. */
  public abstract String content();

  /** Returns the comment's creation date. */
  public abstract Date created();

  /** Returns the type of the comment. */
  public abstract Type type();

  public static Builder builder() {
    return new AutoValue_Comment.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setCommentKey(Key commentKey);
    public abstract Builder setLectureKey(Key lectureKey);
    public abstract Builder setParentKey(Key parentKey);
    public abstract Builder setParentKey(Optional<Key> parentKey);
    public abstract Builder setTimestampMs(long timestampMs);
    public abstract Builder setTimestampMs(Optional<Long> timestampMs);
    public abstract Builder setTranscriptLineKey(Optional<Key> transcriptLineKey);
    public abstract Builder setAuthor(User author);
    public abstract Builder setContent(String content);
    public abstract Builder setCreated(Date created);
    public abstract Builder setType(Type type);

    public abstract Comment build();
  }
}
