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
import com.google.appengine.api.users.User;
import com.google.auto.value.AutoValue;
import java.util.Date;
import java.util.Optional;

/** Contains data related to a comment in a discussion. */
@AutoValue
public abstract class Comment {
  public static final String ENTITY_KIND = "Comment";
  public static final String PROP_LECTURE = "lecture";
  public static final String PROP_PARENT = "parent";
  public static final String PROP_TIMESTAMP = "timestamp";
  public static final String PROP_AUTHOR = "author";
  public static final String PROP_CONTENT = "content";
  public static final String PROP_CREATED = "created";

  private static Comment create(Key key, Key lecture, Optional<Key> parent, Date timestamp,
      User author, String content, Date created) {
    return new AutoValue_Comment(key, lecture, parent, timestamp, author, content, created);
  }

  /**
   * Creates a {@code Comment} from a datastore {@link com.google.appengine.api.datastore.Entity}
   * using the property names defined in this class.
   */
  public static Comment fromEntity(Entity entity) {
    Key key = entity.getKey();
    Key lecture = (Key) entity.getProperty(PROP_LECTURE);
    Optional<Key> parent = Optional.ofNullable((Key) entity.getProperty(PROP_PARENT));
    Date timestamp = (Date) entity.getProperty(PROP_TIMESTAMP);
    User author = (User) entity.getProperty(PROP_AUTHOR);
    String content = (String) entity.getProperty(PROP_CONTENT);
    Date created = (Date) entity.getProperty(PROP_CREATED);
    return Comment.create(key, lecture, parent, timestamp, author, content, created);
  }

  /** Returns the comment's Datastore entity key. */
  public abstract Key key();

  /** Every comment is about a lecture, this returns the lecture's Datastore entity key. */
  public abstract Key lecture();

  /**
   * Returns the key of the comment this is a reply to, or {@code Optional.empty()} if this
   * isn't a reply.
   */
  public abstract Optional<Key> parent();

  /** Returns the timestamp in the video this comment is referencing. */
  public abstract Date timestamp();

  /**
   * Returns the comment's author.
   */
  public abstract User author();

  /** Returns the comment's content. */
  public abstract String content();

  /** Returns the comment's creation date. */
  public abstract Date created();
}
