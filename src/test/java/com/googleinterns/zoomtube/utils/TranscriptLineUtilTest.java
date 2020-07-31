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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googleinterns.zoomtube.data.TranscriptLine;
import com.googleinterns.zoomtube.utils.TranscriptLineUtil;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TranscriptLineUtilTest {
  private final LocalServiceTestHelper testServices = new LocalServiceTestHelper();
  private static final String TEST_LECTURE = "test lecture";
  private static final String TEST_CONTENT = "test content";
  private static final Date TEST_DATE = new Date(0);

  @Before
  public void setUp() {
    testServices.setUp();
  }

  @After
  public void tearDown() {
    testServices.tearDown();
  }

  @Test
  public void fromEntity_sanityCheck() throws IOException {
    Entity lineEntity = new Entity(TranscriptLineUtil.ENTITY_KIND);
    lineEntity.setProperty(TranscriptLineUtil.PROP_LECTURE, TEST_LECTURE);
    lineEntity.setProperty(TranscriptLineUtil.PROP_CONTENT, TEST_CONTENT);
    lineEntity.setProperty(TranscriptLineUtil.PROP_START, TEST_DATE);
    lineEntity.setProperty(TranscriptLineUtil.PROP_DURATION, TEST_DATE);
    lineEntity.setProperty(TranscriptLineUtil.PROP_END, TEST_DATE);

    TranscriptLine actualLine = TranscriptLineUtil.fromEntity(lineEntity);

    assertThat(actualLine.lecture()).isEqualTo(TEST_LECTURE);
    assertThat(actualLine.start()).isEqualTo(TEST_DATE);
    assertThat(actualLine.duration()).isEqualTo(TEST_DATE);
    assertThat(actualLine.end()).isEqualTo(TEST_DATE);
    assertThat(actualLine.content()).isEqualTo(TEST_CONTENT);
  }

  @Test
  public void createEntity_sanityCheck() throws IOException {
    // TODO: Create a mock for node
  }
}