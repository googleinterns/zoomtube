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

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googleinterns.zoomtube.data.Comment;
import java.io.IOException;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CommentUtilTest {
  // Needed for accessing datastore services while creating an Entity.
  private final LocalServiceTestHelper testServices = new LocalServiceTestHelper();

  @Before
  public void setUp() {
    testServices.setUp();
  }

  @After
  public void tearDown() {
    testServices.tearDown();
  }

  @Test
  public void createComment_shouldReturnCommentFromEntity() throws IOException {
    Key lecture = KeyFactory.createKey(LectureUtil.KIND, 12345);
    Key parent = KeyFactory.createKey(CommentUtil.KIND, 67890);
    Date timestamp = new Date(123);
    User author = new User("test@example.com", "example.com");
    String content = "Test content";
    Date dateNow = new Date();
    Entity entity = new Entity(CommentUtil.KIND);
    entity.setProperty(CommentUtil.LECTURE, lecture);
    entity.setProperty(CommentUtil.PARENT, parent);
    entity.setProperty(CommentUtil.TIMESTAMP, timestamp);
    entity.setProperty(CommentUtil.AUTHOR, author);
    entity.setProperty(CommentUtil.CONTENT, content);
    entity.setProperty(CommentUtil.CREATED, dateNow);

    Comment comment = CommentUtil.createComment(entity);

    assertThat(comment.commentKey()).isEqualTo(entity.getKey());
    assertThat(comment.lectureKey()).isEqualTo(lecture);
    assertThat(comment.parentKey().isPresent()).isTrue();
    assertThat(comment.parentKey().get()).isEqualTo(parent);
    assertThat(comment.timestamp()).isEqualTo(timestamp);
    assertThat(comment.author()).isEqualTo(author);
    assertThat(comment.content()).isEqualTo(content);
    assertThat(comment.created()).isEqualTo(dateNow);
  }

  @Test
  public void createEntity_noParent_shouldReturnEntityWithProperties_noParent() throws IOException {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, 12345);
    Date timestamp = new Date(123);
    User author = new User("test@example.com", "example.com");
    String content = "Test content";
    Date dateNow = new Date();

    Entity entity = CommentUtil.createEntity(lectureKey, timestamp, author, content, dateNow);

    assertThat(entity.getProperty(CommentUtil.LECTURE)).isEqualTo(lectureKey);
    assertThat(entity.getProperty(CommentUtil.TIMESTAMP)).isEqualTo(timestamp);
    assertThat(entity.getProperty(CommentUtil.AUTHOR)).isEqualTo(author);
    assertThat(entity.getProperty(CommentUtil.CONTENT)).isEqualTo(content);
    assertThat(entity.getProperty(CommentUtil.CREATED)).isEqualTo(dateNow);
  }

  @Test
  public void createEntity_withParent_shouldReturnEntityWithProperties() throws IOException {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, 12345);
    Key parentKey = KeyFactory.createKey(CommentUtil.KIND, 67890);
    Date timestamp = new Date(123);
    User author = new User("test@example.com", "example.com");
    String content = "Test content";
    Date dateNow = new Date();

    Entity entity =
        CommentUtil.createEntity(lectureKey, parentKey, timestamp, author, content, dateNow);

    assertThat(entity.getProperty(CommentUtil.LECTURE)).isEqualTo(lectureKey);
    assertThat(entity.getProperty(CommentUtil.PARENT)).isEqualTo(parentKey);
    assertThat(entity.getProperty(CommentUtil.TIMESTAMP)).isEqualTo(timestamp);
    assertThat(entity.getProperty(CommentUtil.AUTHOR)).isEqualTo(author);
    assertThat(entity.getProperty(CommentUtil.CONTENT)).isEqualTo(content);
    assertThat(entity.getProperty(CommentUtil.CREATED)).isEqualTo(dateNow);
  }
}
