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
  public void createComment_reply_shouldReturnCommentFromEntity() throws IOException {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, /* id= */ 12345);
    User author = new User("test@example.com", "example.com");
    Key parentKey = KeyFactory.createKey(CommentUtil.KIND, /* id= */ 67890);
    String content = "Test content";
    Date dateNow = new Date();
    Entity entity = new Entity(CommentUtil.KIND);
    Comment.Type type = Comment.Type.REPLY;
    entity.setProperty(CommentUtil.LECTURE, lectureKey);
    entity.setProperty(CommentUtil.PARENT, parentKey);
    entity.setProperty(CommentUtil.AUTHOR, author);
    entity.setProperty(CommentUtil.CONTENT, content);
    entity.setProperty(CommentUtil.CREATED, dateNow);
    entity.setProperty(CommentUtil.TYPE, type.toString());

    Comment comment = CommentUtil.createComment(entity);

    assertThat(comment.commentKey()).isEqualTo(entity.getKey());
    assertThat(comment.lectureKey()).isEqualTo(lectureKey);
    assertThat(comment.parentKey().isPresent()).isTrue();
    assertThat(comment.parentKey().get()).isEqualTo(parentKey);
    assertThat(comment.author()).isEqualTo(author);
    assertThat(comment.content()).isEqualTo(content);
    assertThat(comment.created()).isEqualTo(dateNow);
    assertThat(comment.type()).isEqualTo(type);
  }

  @Test
  public void createComment_root_shouldReturnCommentFromEntity() throws IOException {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, /* id= */ 12345);
    long timestamp = 123;
    User author = new User(/* email= */ "test@example.com", /* authDomain= */ "example.com");
    String content = "Test content";
    Date dateNow = new Date();
    Entity entity = new Entity(CommentUtil.KIND);
    Comment.Type type = Comment.Type.QUESTION;
    entity.setProperty(CommentUtil.LECTURE, lectureKey);
    entity.setProperty(CommentUtil.TIMESTAMP_MS, timestamp);
    entity.setProperty(CommentUtil.AUTHOR, author);
    entity.setProperty(CommentUtil.CONTENT, content);
    entity.setProperty(CommentUtil.CREATED, dateNow);
    entity.setProperty(CommentUtil.TYPE, type.toString());

    Comment comment = CommentUtil.createComment(entity);

    assertThat(comment.commentKey()).isEqualTo(entity.getKey());
    assertThat(comment.lectureKey()).isEqualTo(lectureKey);
    assertThat(comment.parentKey().isPresent()).isFalse();
    assertThat(comment.timestampMs().isPresent()).isTrue();
    assertThat(comment.timestampMs().get()).isEqualTo(timestamp);
    assertThat(comment.author()).isEqualTo(author);
    assertThat(comment.content()).isEqualTo(content);
    assertThat(comment.created()).isEqualTo(dateNow);
    assertThat(comment.type()).isEqualTo(type);
  }

  @Test
  public void createRootEntity_shouldReturnEntityWithProperties() throws IOException {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, /* id= */ 12345);
    long timestampMs = 123;
    User author = new User(/* email= */ "test@example.com", /* authDomain= */ "example.com");
    String content = "Test content";
    Date dateNow = new Date();
    Comment.Type type = Comment.Type.QUESTION;

    Entity entity =
        CommentUtil.createRootEntity(lectureKey, timestampMs, author, content, dateNow, type);

    assertThat(entity.getProperty(CommentUtil.LECTURE)).isEqualTo(lectureKey);
    assertThat(entity.getProperty(CommentUtil.TIMESTAMP_MS)).isEqualTo(timestampMs);
    assertThat(entity.getProperty(CommentUtil.PARENT)).isNull();
    assertThat(entity.getProperty(CommentUtil.AUTHOR)).isEqualTo(author);
    assertThat(entity.getProperty(CommentUtil.CONTENT)).isEqualTo(content);
    assertThat(entity.getProperty(CommentUtil.CREATED)).isEqualTo(dateNow);
    assertThat(Comment.Type.valueOf((String) entity.getProperty(CommentUtil.TYPE))).isEqualTo(type);
  }

  @Test
  public void createReplyEntity_shouldReturnEntityWithProperties() throws IOException {
    Key lectureKey = KeyFactory.createKey(LectureUtil.KIND, /* id= */ 12345);
    Key parentKey = KeyFactory.createKey(CommentUtil.KIND, /* id= */ 67890);
    User author = new User(/* email= */ "test@example.com", /* authDomain= */ "example.com");
    String content = "Test content";
    Date dateNow = new Date();

    Entity entity = CommentUtil.createReplyEntity(lectureKey, parentKey, author, content, dateNow);

    assertThat(entity.getProperty(CommentUtil.LECTURE)).isEqualTo(lectureKey);
    assertThat(entity.getProperty(CommentUtil.TIMESTAMP_MS)).isNull();
    assertThat(entity.getProperty(CommentUtil.PARENT)).isEqualTo(parentKey);
    assertThat(entity.getProperty(CommentUtil.AUTHOR)).isEqualTo(author);
    assertThat(entity.getProperty(CommentUtil.CONTENT)).isEqualTo(content);
    assertThat(entity.getProperty(CommentUtil.CREATED)).isEqualTo(dateNow);
    assertThat(Comment.Type.valueOf((String) entity.getProperty(CommentUtil.TYPE)))
        .isEqualTo(Comment.Type.REPLY);
  }
}
