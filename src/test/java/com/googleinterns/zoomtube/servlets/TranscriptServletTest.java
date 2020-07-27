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

import static org.junit.Assert.assertTrue;
// TODO: Fix astrik imports.
import static org.mockito.Mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.apache.commons.io.FileUtils;
import static com.google.common.truth.Truth.assertThat;
import java.io.IOException;
import javax.servlet.ServletException;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.GsonBuilder;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;

/** */
@RunWith(JUnit4.class)
public final class TranscriptServletTest {
  private TranscriptServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());

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

  // @Test
  // public void testTest() throws IOException {
  // servlet.doPost(request, response);
  // servlet.doGet(request, response);

  // assertThat(response.getContentType()).isEqualTo("application/json;");
  // String json = response.getContentAsString();
  // Gson gson = new GsonBuilder()
  //     .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
  //     .create();
  // TranscriptLine line = gson.fromJson(json, TranscriptLine.class);
  // assertThat(line.start()).contains("0");

  // helper.tearDown();
  // }

// To Test: TranscriptLine class
// DoPost and doGet (everything pretty much in TranscriptServlet)
// Test Get works
// Test DoPost stores the right stuff

// Create issue to document all of this
  @Test
  public void testDoGet() throws ServletException, IOException {
    request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, "XsX3ATc3FbA");
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, "123456789");  
    servlet.doPost(request, response);
    servlet.doGet(request, response);

      assertThat(response.getContentType()).isEqualTo("application/json;");
      String json = response.getContentAsString();
      System.out.println(json);
      Gson gson = new GsonBuilder()
          .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
          .create();
      TranscriptLine line = gson.fromJson(json, TranscriptLine.class);
      // // assertThat(line.start()).contains("0");
      // System.out.println(line);
  }

  // @Test
  // public void helpMeTest() {
  //   DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  //   assertEquals(0, datastore.prepare(new Query(TranscriptLine.ENTITY_KIND)).countEntities(withLimit(10)));
  //   datastore.put(new Entity("yam"));
  //   datastore.put(new Entity("yam"));
  //   // replace with doPOst stuff
  //   request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, "XsX3ATc3FbA");
  //   request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, "123456789");
  //   servlet.doGet(request, response);  
  //   assertEquals(2, datastore.prepare(new Query("yam")).countEntities(withLimit(10)));
  // }
}