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

package com.googleinterns.zoomtube.servlets;

import static com.google.common.truth.Truth.assertThat;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googleinterns.zoomtube.data.Lecture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(JUnit4.class)
public final class LectureServletTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  LectureServlet servlet;
  DatastoreService datastoreService;
  MockHttpServletRequest mockRequest;

  private static final String NAME_INPUT = "name-input";
  private static final String LINK_INPUT = "link-input";
  private static final String DEFAULT_VALUE = "";

  private static final String TEST_NAME = "test";
  private static final String TEST_LINK = "https://www.youtube.com/watch?v=wXhTHyIgQ_U";

  @Before
  public void setUp() throws ServletException {
    helper.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    mockRequest = new MockHttpServletRequest();
    servlet = new LectureServlet();
    servlet.init();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void checkUrlInDatabase_urlPresent_shouldReturnLecture() {
    mockRequest.addParameter(LINK_INPUT, TEST_LINK);
    Entity expected = servlet.createLectureEntity(mockRequest);
    datastoreService.put(expected);

    Optional<Entity> result = servlet.checkUrlInDatabase(TEST_LINK);

    Assert.assertEquals(expected, result.get());
  }

  @Test
  public void checkUrlInDatabase_urlNotPresent_shouldReturnEmptyOptional() {
    mockRequest.addParameter(LINK_INPUT, TEST_LINK);
    Entity expected = servlet.createLectureEntity(mockRequest);
    datastoreService.put(expected);

    Optional<Entity> result =
        servlet.checkUrlInDatabase("https://www.youtube.com/watch?v=YWN81V7ojOE");

    Assert.assertEquals(Optional.empty(), result);
  }

  @Test
  public void getParameter_allParamsPresent_shouldNotReturnDefaultValue() {
    mockRequest.addParameter(NAME_INPUT, TEST_NAME);
    mockRequest.addParameter(LINK_INPUT, TEST_LINK);

    String nameResult = servlet.getParameter(mockRequest, NAME_INPUT, DEFAULT_VALUE);
    String linkResult = servlet.getParameter(mockRequest, LINK_INPUT, DEFAULT_VALUE);

    assertThat(nameResult).isEqualTo(TEST_NAME);
    assertThat(linkResult).isEqualTo(TEST_LINK);
  }

  @Test
  public void getParameter_noParamsPresent_shouldReturnDefaultValue() {
    String nameResult = servlet.getParameter(mockRequest, NAME_INPUT, DEFAULT_VALUE);
    String linkResult = servlet.getParameter(mockRequest, LINK_INPUT, DEFAULT_VALUE);

    assertThat(nameResult).isEmpty();
    assertThat(linkResult).isEmpty();
  }

  @Test
  public void getVideoId_shouldFindAllIds() {
    String video1 = "http://www.youtube.com/watch?v=dQw4w9WgXcQ&a=GxdCwVVULXctT2lYDEPllDR0LRTutYfW";
    String video2 = "http://www.youtube.com/watch?v=dQw4w9WgXcQ";
    String video3 = "http://youtu.be/dQw4w9WgXcQ";
    String video4 = "http://www.youtube.com/embed/dQw4w9WgXcQ";
    String video5 = "http://www.youtube.com/v/dQw4w9WgXcQ";
    String video6 = "http://www.youtube.com/watch?v=dQw4w9WgXcQ";
    String video7 = "http://www.youtube.com/watch?feature=player_embedded&v=dQw4w9WgXcQ";
    String video8 = "http://www.youtube-nocookie.com/v/dQw4w9WgXcQ?version=3&hl=en_US&rel=0";
    String id = "dQw4w9WgXcQ";

    assertThat(servlet.getVideoId(video1)).isEqualTo(id);
    assertThat(servlet.getVideoId(video2)).isEqualTo(id);
    assertThat(servlet.getVideoId(video3)).isEqualTo(id);
    assertThat(servlet.getVideoId(video4)).isEqualTo(id);
    assertThat(servlet.getVideoId(video5)).isEqualTo(id);
    assertThat(servlet.getVideoId(video6)).isEqualTo(id);
    assertThat(servlet.getVideoId(video7)).isEqualTo(id);
    assertThat(servlet.getVideoId(video8)).isEqualTo(id);
  }

  @Test
  public void buildRedirectUrl_shouldFindAllIds() {
    mockRequest.addParameter(LINK_INPUT, TEST_LINK);
    Entity entity = servlet.createLectureEntity(mockRequest);

    String expectedUrl = "/lecture-view.html?id=0&video-id=wXhTHyIgQ_U";
    String resultUrl = servlet.buildRedirectUrl(entity);

    assertThat(expectedUrl).isEqualTo(resultUrl);
  }

  @Test
  public void getLectures_emptyDatabase_shouldReturnNoLectures() {
    List<Lecture> result = servlet.getLectures();

    assertThat(result.isEmpty()).isTrue();
  }

  @Test
  public void getLectures_oneLectureInDatabase_shouldReturnAllLectures() {
    mockRequest.addParameter(LINK_INPUT, TEST_LINK);
    Entity entity = servlet.createLectureEntity(mockRequest);
    datastoreService.put(entity);

    Lecture newLecture = Lecture.fromLectureEntity(entity);
    List<Lecture> expected = new ArrayList<>();
    expected.add(newLecture);

    assertThat(servlet.getLectures()).isEqualTo(expected);
  }
}
