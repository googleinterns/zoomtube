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
import com.googleinterns.zoomtube.data.Lecture;
import java.util.Date;
import java.util.Optional;

/** Provides methods to create Comment Entities and Comments. */
public class CommentUtil {
  public static final String KIND = "Comment";
  public static final String LECTURE = "lecture";
  public static final String PARENT = "parent";
  public static final String TIMESTAMP = "timestamp";
  public static final String AUTHOR = "author";
  public static final String CONTENT = "content";
  public static final String CREATED = "created";

  /**
   * Returns a Comment using the properties of {@code entity}.
   */
  public static Comment fromEntity(Entity entity) {
    Key key = entity.getKey();
    Key lecture = (Key) entity.getProperty(LECTURE);
    Optional<Key> parent = Optional.ofNullable((Key) entity.getProperty(PARENT));
    Date timestamp = (Date) entity.getProperty(TIMESTAMP);
    User author = (User) entity.getProperty(AUTHOR);
    String content = (String) entity.getProperty(CONTENT);
    Date created = (Date) entity.getProperty(CREATED);
    return Comment.create(key, lecture, parent, timestamp, author, content, created);
  }

  /**
   * Creates and returns an entity for {@code comment}.
   */
  public static Entity createEntity(Key lecture, Optional<Key> parent, Date timestamp, User author,
      String content, Date created) {
    Entity entity = new Entity(KIND);
    entity.setProperty(LECTURE, lecture);
    if (parent.isPresent()) {
      entity.setProperty(PARENT, parent.get());
    }
    entity.setProperty(TIMESTAMP, timestamp);
    entity.setProperty(AUTHOR, author);
    entity.setProperty(CONTENT, content);
    entity.setProperty(CREATED, created);
    return entity;
  }
}
