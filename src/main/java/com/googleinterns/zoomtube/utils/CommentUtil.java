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
import com.google.appengine.api.users.User;
import com.googleinterns.zoomtube.data.Comment;
import java.util.Date;
import javax.annotation.Nullable;

/** Provides methods to create Comment Entities and Comments. */
public final class CommentUtil {
  public static final String KIND = "Comment";
  public static final String LECTURE = "lecture";
  public static final String PARENT = "parent";
  public static final String TIMESTAMP_MS = "timestamp_ms";
  public static final String AUTHOR = "author";
  public static final String CONTENT = "content";
  public static final String CREATED = "created";

  /**
   * Creates and returns a Comment using the properties of {@code entity}.
   */
  public static Comment createComment(Entity entity) {
    Key commentKey = entity.getKey();
    Key lectureKey = (Key) entity.getProperty(LECTURE);
    @Nullable Key parentKey = (Key) entity.getProperty(PARENT);
    long timestampMs = (long) entity.getProperty(TIMESTAMP_MS);
    User author = (User) entity.getProperty(AUTHOR);
    String content = (String) entity.getProperty(CONTENT);
    Date created = (Date) entity.getProperty(CREATED);

    Comment.Builder builder = Comment.builder()
                                  .setCommentKey(commentKey)
                                  .setLectureKey(lectureKey)
                                  .setTimestampMs(timestampMs)
                                  .setAuthor(author)
                                  .setContent(content)
                                  .setCreated(created);
    if (parentKey != null) {
      builder.setParentKey(parentKey);
    }
    return builder.build();
  }

  /**
   * Creates and returns an entity with the specified properties and no parent comment.
   */
  public static Entity createEntity(
      Key lectureKey, long timestampMs, User author, String content, Date created) {
    Entity entity = new Entity(KIND);
    entity.setProperty(LECTURE, lectureKey);
    entity.setProperty(TIMESTAMP_MS, timestampMs);
    entity.setProperty(AUTHOR, author);
    entity.setProperty(CONTENT, content);
    entity.setProperty(CREATED, created);
    return entity;
  }

  /**
   * Creates and returns an entity with the specified properties, including a parent comment.
   */
  public static Entity createEntity(
      Key lectureKey, Key parentKey, long timestampMs, User author, String content, Date created) {
    Entity entity = new Entity(KIND);
    entity.setProperty(LECTURE, lectureKey);
    entity.setProperty(PARENT, parentKey);
    entity.setProperty(TIMESTAMP_MS, timestampMs);
    entity.setProperty(AUTHOR, author);
    entity.setProperty(CONTENT, content);
    entity.setProperty(CREATED, created);
    return entity;
  }

  private CommentUtil(){};
}
