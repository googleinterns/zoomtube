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
import java.util.Optional;

/** Provides methods to create Comment Entities and Comments. */
public final class CommentUtil {
  public static final String KIND = "Comment";
  public static final String LECTURE = "lecture";
  public static final String PARENT = "parent";
  public static final String TIMESTAMP = "timestamp";
  public static final String AUTHOR = "author";
  public static final String CONTENT = "content";
  public static final String CREATED = "created";

  private CommentUtil(){};

  /**
   * Returns a Comment using the properties of {@code entity}.
   */
  public static Comment createComment(Entity entity) {
    Key commentKey = entity.getKey();
    Key lectureKey = (Key) entity.getProperty(LECTURE);
    Optional<Key> parentKey = Optional.ofNullable((Key) entity.getProperty(PARENT));
    Date timestamp = (Date) entity.getProperty(TIMESTAMP);
    User author = (User) entity.getProperty(AUTHOR);
    String content = (String) entity.getProperty(CONTENT);
    Date created = (Date) entity.getProperty(CREATED);
    return Comment.create(commentKey, lectureKey, parentKey, timestamp, author, content, created);
  }

  /**
   * Creates and returns an entity with the specified properties.
   */
  public static Entity createEntity(Key lectureKey, Optional<Key> parentKey, Date timestamp,
      User author, String content, Date created) {
    Entity entity = new Entity(KIND);
    entity.setProperty(LECTURE, lectureKey);
    if (parentKey.isPresent()) {
      entity.setProperty(PARENT, parentKey.get());
    }
    entity.setProperty(TIMESTAMP, timestamp);
    entity.setProperty(AUTHOR, author);
    entity.setProperty(CONTENT, content);
    entity.setProperty(CREATED, created);
    return entity;
  }
}
