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
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googleinterns.zoomtube.data.IconFeedback;
import com.googleinterns.zoomtube.utils.IconFeedbackUtil;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class IconFeedbackUtilTest {
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
  public void createIconFeedback_shouldReturnFeedbackFromEntity() throws IOException {
    Entity iconFeedbackEntity = new Entity(IconFeedbackUtil.KIND);
    Key lectureKey = KeyFactory.createKey("Lecture", /*lectureId=*/123);
    iconFeedbackEntity.setProperty(IconFeedbackUtil.LECTURE, lectureKey);
    iconFeedbackEntity.setProperty(IconFeedbackUtil.TIMESTAMP_MS, 456L);
    iconFeedbackEntity.setProperty(IconFeedbackUtil.TYPE, IconFeedback.Type.GOOD.toString());

    IconFeedback result = IconFeedbackUtil.createIconFeedback(iconFeedbackEntity);

    assertThat(result.lectureKey()).isEqualTo(lectureKey);
    assertThat(result.timestampMs()).isEqualTo(456);
    assertThat(result.type()).isEqualTo(IconFeedback.Type.GOOD);
  }

  @Test
  public void createEntity_shouldReturnEntityWithInputs() throws IOException {
    Key lectureKey = KeyFactory.createKey("Lecture", /*lectureId=*/123);

    Entity result = IconFeedbackUtil.createEntity(lectureKey, 456L, IconFeedback.Type.GOOD);

    assertThat(result.getProperty(IconFeedbackUtil.LECTURE)).isEqualTo(lectureKey);
    assertThat(result.getProperty(IconFeedbackUtil.TIMESTAMP_MS)).isEqualTo(456);
    assertThat(result.getProperty(IconFeedbackUtil.TYPE))
        .isEqualTo(IconFeedback.Type.GOOD.toString());
  }
}
