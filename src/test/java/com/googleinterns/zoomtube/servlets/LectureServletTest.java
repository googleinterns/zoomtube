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
import com.google.appengine.api.datastore.Query;
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
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(JUnit4.class)
public final class LectureServletTest {
  private final LocalServiceTestHelper datastoreServiceHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  DatastoreService datastoreService;
  LectureServlet servlet;
  MockHttpServletRequest mockRequest;
  MockHttpServletResponse mockResponse;

  private static final String NAME_INPUT = "name-input";
  private static final String LINK_INPUT = "link-input";
  private static final String DEFAULT_VALUE = "";

  private static final String TEST_NAME = "test";
  private static final String TEST_LINK = "https://www.youtube.com/watch?v=wXhTHyIgQ_U";

  @Before
  public void setUp() throws ServletException {
    datastoreServiceHelper.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    mockRequest = new MockHttpServletRequest();
    mockResponse = new MockHttpServletResponse();
    servlet = new LectureServlet();
    servlet.init();
  }

  @After
  public void tearDown() {
    datastoreServiceHelper.tearDown();
  }

  @Test
  public void doPost_urlAlreadyInDatabase_shouldReturnLecture() throws IOException {
    mockRequest.addParameter(LINK_INPUT, TEST_LINK);
    datastoreService.put(servlet.createLectureEntity(mockRequest));
    assertThat(datastoreService.prepare(new Query("Lecture")).countEntities()).isEqualTo(1);

    servlet.doPost(mockRequest, mockResponse);
    
    assertThat(datastoreService.prepare(new Query("Lecture")).countEntities()).isEqualTo(1);
    assertThat(mockResponse.getRedirectedUrl()).isEqualTo("/lecture-view.html?id=1&video-id=wXhTHyIgQ_U");
  }

  @Test
  public void doPost_urlNotInDatabase_shouldAddToDatabaseAndReturnRedirect() throws IOException {
    mockRequest.addParameter(LINK_INPUT, TEST_LINK);
    System.out.println("Query database: " + datastoreService.prepare(new Query("Lecture")).countEntities());
    assertThat(datastoreService.prepare(new Query("Lecture")).countEntities()).isEqualTo(0);
   
    servlet.doPost(mockRequest, mockResponse);
    
    assertThat(datastoreService.prepare(new Query("Lecture")).countEntities()).isEqualTo(1);
    assertThat(mockResponse.getRedirectedUrl()).isEqualTo("/lecture-view.html?id=1&video-id=wXhTHyIgQ_U");
  }

  // Test empty database
  @Test
  public void doGet_emptyDatabase_shouldReturnNoLecture() throws IOException {
    assertThat(datastoreService.prepare(new Query("Lecture")).countEntities()).isEqualTo(0);

    servlet.doGet(mockRequest, mockResponse);
    
    assertThat(mockResponse.getContentAsString()).isEqualTo("[]\n");
  }

  // Test database that has one lecture

  // Keep this
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
}
