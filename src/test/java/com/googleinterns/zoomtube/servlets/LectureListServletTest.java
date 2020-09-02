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
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googleinterns.zoomtube.data.Lecture;
import com.googleinterns.zoomtube.utils.LectureUtil;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class LectureListServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private final LocalServiceTestHelper testServices =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastoreService;
  private LectureListServlet servlet;

  /* Response is written here. */
  private StringWriter content;

  private static final String TEST_LINK = "https://www.youtube.com/watch?v=wXhTHyIgQ_U";
  private static final String TEST_ID = "wXhTHyIgQ_U";

  @Before
  public void setUp() throws Exception {
    testServices.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    servlet = new LectureListServlet();
    servlet.init();
    content = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(content));
  }

  @After
  public void tearDown() {
    testServices.tearDown();
  }

  @Test
  public void doGet_emptyDatabase_shouldReturnNoLecture() throws Exception {
    servlet.doGet(request, response);

    String json = content.toString();
    assertThat(json).startsWith("[]");
  }

  @Test
  public void doGet_oneLectureInDatabase_shouldReturnOneLecture() throws Exception {
    datastoreService.put(LectureUtil.createEntity(/* lectureName= */ "", TEST_LINK, TEST_ID));

    servlet.doGet(request, response);

    String json = content.toString();
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY).create();
    Type listType = new TypeToken<ArrayList<Lecture>>() {}.getType();
    ArrayList<Lecture> lectures = gson.fromJson(json, listType);
    assertThat(lectures).hasSize(1);
    assertThat(lectures.get(0).videoUrl()).isEqualTo(TEST_LINK);
  }
}
