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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googleinterns.zoomtube.data.IconFeedback;
import com.googleinterns.zoomtube.utils.IconFeedbackUtil;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
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
public final class IconFeedbackServletTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private final LocalServiceTestHelper testServices =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastoreService;
  private IconFeedbackServlet servlet;

  /* Writer where response is written. */
  private StringWriter content;

  private static final String TEST_LECTURE_ID = "123456789";
  private static final String TEST_TIMESTAMP = "987654321";
  private static final String TEST_ICON_TYPE = "GOOD";

  @Before
  public void setUp() throws ServletException, IOException {
    testServices.setUp();
    datastoreService = DatastoreServiceFactory.getDatastoreService();
    servlet = new IconFeedbackServlet();
    servlet.init();
    content = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(content));
  }

  @After
  public void tearDown() {
    testServices.tearDown();
  }

  @Test
  public void doPost_missingLectureId_ShouldRespondWithBadRequest()
      throws IOException, ServletException {
    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing lecture id parameter.");
  }

  @Test
  public void doPost_missingTimestamp_ShouldRespondWithBadRequest()
      throws IOException, ServletException {
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn(TEST_LECTURE_ID);

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing timestamp parameter.");
  }

  @Test
  public void doPost_missingIconType_ShouldRespondWithBadRequest()
      throws IOException, ServletException {
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn(TEST_LECTURE_ID);
    when(request.getParameter(IconFeedbackServlet.PARAM_TIMESTAMP)).thenReturn(TEST_TIMESTAMP);

    servlet.doPost(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_BAD_REQUEST, /* message= */ "Missing icon type parameter.");
  }

  @Test
  public void doPost_validRequest_ShouldStoreInDatabase() throws IOException, ServletException {
    when(request.getParameter(IconFeedbackServlet.PARAM_LECTURE_ID)).thenReturn(TEST_LECTURE_ID);
    when(request.getParameter(IconFeedbackServlet.PARAM_TIMESTAMP)).thenReturn(TEST_TIMESTAMP);
    when(request.getParameter(IconFeedbackServlet.PARAM_ICON_TYPE)).thenReturn(TEST_ICON_TYPE);

    servlet.doPost(request, response);

    assertThat(datastoreService.prepare(new Query(IconFeedbackUtil.KIND)).countEntities())
        .isEqualTo(1);
  }
}
