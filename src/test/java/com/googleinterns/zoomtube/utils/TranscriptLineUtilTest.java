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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googleinterns.zoomtube.data.TranscriptLine;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.w3c.dom.Element;

@RunWith(JUnit4.class)
public final class TranscriptLineUtilTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private Element node;

  private final LocalServiceTestHelper localServiceHelper =
      new LocalServiceTestHelper((new LocalDatastoreServiceTestConfig()).setNoStorage(true));
  private static final String TEST_CONTENT = "test content";
  private static final long LECTURE_ID_A = 1234;

  @Before
  public void setUp() {
    localServiceHelper.setUp();
  }

  @After
  public void tearDown() {
    localServiceHelper.tearDown();
  }

  @Test
  public void createTranscriptLine_transcriptLineSuccessfullyCreated() throws IOException {
    final Date testDate = new Date();
    final Key testLectureKey = KeyFactory.createKey(LectureUtil.KIND, LECTURE_ID_A);
    final Entity lineEntity = new Entity(TranscriptLineUtil.KIND);
    lineEntity.setProperty(TranscriptLineUtil.LECTURE, testLectureKey);
    lineEntity.setProperty(TranscriptLineUtil.CONTENT, TEST_CONTENT);
    lineEntity.setProperty(TranscriptLineUtil.START, testDate);
    lineEntity.setProperty(TranscriptLineUtil.DURATION, testDate);
    lineEntity.setProperty(TranscriptLineUtil.END, testDate);

    TranscriptLine actualLine = TranscriptLineUtil.createTranscriptLine(lineEntity);

    assertThat(actualLine.lectureKey()).isEqualTo(testLectureKey);
    assertThat(actualLine.start()).isEqualTo(testDate);
    assertThat(actualLine.duration()).isEqualTo(testDate);
    assertThat(actualLine.end()).isEqualTo(testDate);
    assertThat(actualLine.content()).isEqualTo(TEST_CONTENT);
  }

  @Test
  public void createEntity_entityAndPropertiesSuccessfullyCreated() throws IOException {
    final Float startDate = 23.32F;
    final Float duration = 23.32F;
    final Float endDate = startDate.floatValue() + duration.floatValue();
    Key lectureKeyA = KeyFactory.createKey(LectureUtil.KIND, LECTURE_ID_A);
    Entity actualEntity =
        TranscriptLineUtil.createEntity(lectureKeyA, TEST_CONTENT, startDate, duration, endDate);

    assertThat(actualEntity.getProperty(TranscriptLineUtil.CONTENT)).isEqualTo(TEST_CONTENT);
    Key actualKey = KeyFactory.createKey(LectureUtil.KIND, LECTURE_ID_A);
    assertThat(actualEntity.getProperty(TranscriptLineUtil.LECTURE)).isEqualTo(actualKey);
    Date actualStart = new Date(TimeUnit.SECONDS.toMillis(startDate.longValue()));
    assertThat(actualEntity.getProperty(TranscriptLineUtil.START)).isEqualTo(actualStart);
    Date actualDuration = new Date(TimeUnit.SECONDS.toMillis(startDate.longValue()));
    assertThat(actualEntity.getProperty(TranscriptLineUtil.DURATION)).isEqualTo(actualDuration);
    Date actualEnd =
        new Date(TimeUnit.SECONDS.toMillis(startDate.longValue() + duration.longValue()));
    // The end time is calculated as start time + duration.
    assertThat(actualEntity.getProperty(TranscriptLineUtil.END)).isEqualTo(actualEnd);
  }
}
