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

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.googleinterns.zoomtube.data.Lecture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.mock.web.MockHttpServletRequest;

/** Provides information on a lecture. */
@RunWith(JUnit4.class)
public final class LectureServletTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  LectureServlet servlet = new LectureServlet();
  DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
  
  @Test
  public void checkUrlInDatabase_urlPresent_shouldReturnLecture() {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addParameter("name-input", "Test");
    mockRequest.addParameter("link-input", "https://www.youtube.com/watch?v=wXhTHyIgQ_U");
    Entity expected = servlet.createLectureEntity(mockRequest);
    ds.put(expected);

    Optional<Entity> result = servlet.checkUrlInDatabase("https://www.youtube.com/watch?v=wXhTHyIgQ_U");
    
    Assert.assertEquals(expected, result.get());
  }

  @Test
  public void checkUrlInDatabase_urlNotPresent_shouldReturnEmptyOptional() {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addParameter("name-input", "Test");
    mockRequest.addParameter("link-input", "https://www.youtube.com/watch?v=wXhTHyIgQ_U");
    Entity expected = servlet.createLectureEntity(mockRequest);
    ds.put(expected);

    Optional<Entity> result = servlet.checkUrlInDatabase("https://www.youtube.com/watch?v=YWN81V7ojOE");
    
    Assert.assertEquals(Optional.empty(), result);
  }

  @Test
  public void getParameter_allParamsPresent_shouldNotReturnDefaultValue() {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addParameter("name-input", "Test");
    mockRequest.addParameter("link-input", "https://www.youtube.com/watch?v=wXhTHyIgQ_U");

    String nameResult = servlet.getParameter(mockRequest, "name-input", "");
    String linkResult = servlet.getParameter(mockRequest, "link-input", "");

    Assert.assertEquals(nameResult, "Test");
    Assert.assertEquals(linkResult, "https://www.youtube.com/watch?v=wXhTHyIgQ_U");
  }

  @Test
  public void getParameter_noParamsPresent_shouldReturnDefaultValue() {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();

    String nameResult = servlet.getParameter(mockRequest, "name-input", "");
    String linkResult = servlet.getParameter(mockRequest, "link-input", "");

    Assert.assertEquals(nameResult, "");
    Assert.assertEquals(linkResult, "");
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

    Assert.assertEquals(servlet.getVideoId(video1), "dQw4w9WgXcQ");
    Assert.assertEquals(servlet.getVideoId(video2), "dQw4w9WgXcQ");
    Assert.assertEquals(servlet.getVideoId(video3), "dQw4w9WgXcQ");
    Assert.assertEquals(servlet.getVideoId(video4), "dQw4w9WgXcQ");
    Assert.assertEquals(servlet.getVideoId(video5), "dQw4w9WgXcQ");
    Assert.assertEquals(servlet.getVideoId(video6), "dQw4w9WgXcQ");
    Assert.assertEquals(servlet.getVideoId(video7), "dQw4w9WgXcQ");
    Assert.assertEquals(servlet.getVideoId(video8), "dQw4w9WgXcQ");
  }

  @Test
  public void buildRedirectUrl_shouldFindAllIds() {
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addParameter("name-input", "Test");
    mockRequest.addParameter("link-input", "https://www.youtube.com/watch?v=wXhTHyIgQ_U");
    Entity entity = servlet.createLectureEntity(mockRequest);
    
    String expectedUrl = "/lecture-view.html?id=0&video-id=wXhTHyIgQ_U";
    String resultUrl = servlet.buildRedirectUrl(entity);

    Assert.assertEquals(expectedUrl, resultUrl);
  }
}
