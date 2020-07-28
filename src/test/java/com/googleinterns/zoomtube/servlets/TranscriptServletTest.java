// Copyright 2019 Google LLC
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalServiceTestConfig;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.TranscriptLine;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.google.common.reflect.TypeToken;

import static org.junit.Assert.assertTrue;
// TODO: Fix astrik imports.
import static org.mockito.Mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.apache.commons.io.FileUtils;
import static com.google.common.truth.Truth.assertThat;
import java.io.IOException;
import javax.servlet.ServletException;
import com.google.gson.GsonBuilder;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;

/** */
@RunWith(JUnit4.class)
public final class TranscriptServletTest {
  private TranscriptServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  LocalDatastoreServiceTestConfig help = (new  LocalDatastoreServiceTestConfig()).setNoStorage(true);
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(help);

  @Before
  public void setUp() throws ServletException {
    helper.setUp();
    servlet = new TranscriptServlet();
    servlet.init();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }
  
  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_doPost_ParseShortVideo() throws ServletException, IOException {
    //TODO: order test file, add constants
    DatastoreService dd = LocalDatastoreServiceTestConfig.getLocalDatastoreService();
    DatastoreService ds = LocalServiceTestHelper.getLocalService(LocalDatastoreService.PACKAGE);
    request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, "Obgnr9pc820");
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, "123");  
    servlet.doPost(request, response);
    servlet.doGet(request, response);

      assertThat(response.getContentType()).isEqualTo("application/json;");
      String json = response.getContentAsString();
      System.out.println(json);
      Gson gson = new GsonBuilder()
          .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
          .create();
      String expectedJson = "[{\"key\":{\"kind\":\"TranscriptLine\",\"id\":1},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"0.4\",\"duration\":\"1\",\"content\":\" \"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":2},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"2.28\",\"duration\":\"1\",\"content\":\"Hi\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":3},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"5.04\",\"duration\":\"1.6\",\"content\":\"Okay\"}]";
      ArrayList<TranscriptLine> expectedArrayList = (ArrayList<TranscriptLine>) gson.fromJson(expectedJson,(new ArrayList<List<TranscriptLine>>().getClass()));
      
      ArrayList<TranscriptLine> jsonArray = (ArrayList<TranscriptLine>) gson.fromJson(json, (new ArrayList<List<TranscriptLine>>().getClass()));
      assertThat(expectedArrayList).isEqualTo(jsonArray);
      
      assertEquals(0, ds.prepare(new Query(TranscriptServlet.PARAM_LECTURE_ID)).countEntities(withLimit(20)));
      LocalDatastoreServiceTestConfig.getLocalDatastoreService();
  }

  @Test
  public void testDoPost() {
    request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, "jNQXAC9IVRw");
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, "123");

  }
}