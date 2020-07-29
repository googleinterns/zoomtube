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
import com.google.common.collect.ImmutableList;
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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.gson.JsonSyntaxException;
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

import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalCapabilitiesServiceTestConfig;

/** */
@RunWith(JUnit4.class)
public final class TranscriptServletTest {
  private TranscriptServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  LocalDatastoreServiceTestConfig help = (new  LocalDatastoreServiceTestConfig()).setNoStorage(true);
  private LocalServiceTestHelper helper = new LocalServiceTestHelper(help);
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Gson gson;

  private static final String LECTURE_ID_A = "123";
  private static final String LECTURE_ID_B = "234";
  private static final String LECTURE_ID_C = "345";

  private static final String THREE_LINE_VIDEO_ID = "Obgnr9pc820";
  private static final String THREE_LINE_VIDEO_JSON = 
      "[{\"key\":{\"kind\":\"TranscriptLine\",\"id\":1},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"0.4\",\"duration\":\"1\",\"content\":\" \"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":2},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"2.28\",\"duration\":\"1\",\"content\":\"Hi\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":3},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"5.04\",\"duration\":\"1.6\",\"content\":\"Okay\"}]";
  private static final String THREE_MIN_VIDEO_ID = "jNQXAC9IVRw";
  private static final String THREE_MIN_VIDEO_JSON = 
      "[{\"key\":{\"kind\":\"TranscriptLine\",\"id\":1},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"1.3\",\"duration\":\"3.1\",\"content\":\"All right, so here we are\\nin front of the elephants,\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":4},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"12.7\",\"duration\":\"4.3\",\"content\":\"and that&#39;s, that&#39;s cool.\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":5},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"17\",\"duration\":\"1.767\",\"content\":\"And that&#39;s pretty much all there is to say.\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":2},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"4.4\",\"duration\":\"4.766\",\"content\":\"the cool thing about these guys\\nis that they have really,\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":3},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"9.166\",\"duration\":\"3.534\",\"content\":\"really, really long trunks,\"}]";
  
  
  @Before
  public void setUp() throws ServletException {
    helper.setUp();
    servlet = new TranscriptServlet();
    servlet.init(datastore);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    gson = new GsonBuilder()
    .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
    .create();
  }
  
  @After
  public void tearDown() {
    helper.tearDown();
  }

  private ArrayList<TranscriptLine> extractJsonAsArrayList(String json) {
    return (ArrayList<TranscriptLine>) gson.fromJson(THREE_LINE_VIDEO_JSON,(new ArrayList<List<TranscriptLine>>().getClass()));
  }

  @Test
  public void doGet_getDataInDatastoreForShortVideo() throws ServletException, IOException {
    putJsonInDatastore(THREE_LINE_VIDEO_JSON, LECTURE_ID_A);
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);
    
    servlet.doGet(request, response);
    String actualJson = response.getContentAsString();
    ArrayList<TranscriptLine> expectedArrayList = extractJsonAsArrayList(THREE_LINE_VIDEO_JSON);
    ArrayList<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(actualJsonArray.size()).isEqualTo(expectedArrayList.size());
  }

  @Test
  public void doPost_persistDataInDatastoreForShortVideo() throws ServletException, IOException {
    request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, THREE_LINE_VIDEO_ID);
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);
    servlet.doPost(request, response);  

    int actualQueryCount = countEntities(LECTURE_ID_A);
    int expectedQueryCount = (extractJsonAsArrayList(THREE_LINE_VIDEO_JSON)).size();
    assertEquals(expectedQueryCount, actualQueryCount);
  }

  private int countEntitiesInDatastore(String lectureId) {
    return datastore.prepare(filteredQuery(lectureId)).countEntities(withLimit(10));
  }

  private Query filteredQuery(String lectureId) {
    Key lecture = KeyFactory.createKey(TranscriptServlet.PARAM_LECTURE, Long.parseLong(LECTURE_ID_A));
    Filter lectureFilter =
        new FilterPredicate(TranscriptLine.PROP_LECTURE, FilterOperator.EQUAL, lecture);
    return new Query(TranscriptLine.ENTITY_KIND).setFilter(lectureFilter);    
  }

  @Test
  public void doGet_doPost_StoreAndRetrieveShortVideo() throws ServletException, IOException {
    request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, THREE_LINE_VIDEO_ID);
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);  
    servlet.doPost(request, response);
    servlet.doGet(request, response);
    
    String actualJson = response.getContentAsString();
    ArrayList<TranscriptLine> expectedArrayList = extractJsonAsArrayList(THREE_LINE_VIDEO_JSON);
    ArrayList<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);
    
    assertThat(expectedArrayList).isEqualTo(actualJsonArray); 
  }

  @Test
  public void doGet_doPost_StoreAndRetrieveLongVideo() throws ServletException, IOException {
    request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, THREE_MIN_VIDEO_ID);
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);  
    servlet.doPost(request, response);
    servlet.doGet(request, response);
    
    String actualJson = response.getContentAsString();
    ArrayList<TranscriptLine> expectedArrayList = extractJsonAsArrayList(THREE_MIN_VIDEO_JSON);
    ArrayList<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);
    
    assertThat(expectedArrayList).isEqualTo(actualJsonArray); 
  }

  @Test
  public void doGet_TwoLecturesInDatastore_GetOneLecture() throws ServletException, IOException {
    putJsonInDatastore(THREE_LINE_VIDEO_JSON, LECTURE_ID_A);
    putJsonInDatastore(THREE_MIN_VIDEO_JSON, LECTURE_ID_B);

    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);  
    servlet.doGet(request, response);
    
    String actualJson = response.getContentAsString();
    ArrayList<TranscriptLine> expectedArrayList = extractJsonAsArrayList(THREE_LINE_VIDEO_JSON);
    ArrayList<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(expectedArrayList).isEqualTo(actualJsonArray); 
  }

  @Test
  public void doGet_OnlyOtherLecturesInDatastore_GetNoLectures() throws ServletException, IOException {
    putJsonInDatastore(THREE_LINE_VIDEO_JSON, LECTURE_ID_A);
    putJsonInDatastore(THREE_MIN_VIDEO_JSON, LECTURE_ID_B);
    request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_C);  
    
    servlet.doGet(request, response);
    String actualJson = response.getContentAsString();
    ArrayList<TranscriptLine> expectedArrayList = new ArrayList<TranscriptLine>();
    ArrayList<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

    assertThat(expectedArrayList).isEqualTo(actualJsonArray); 
  }

  private void putJsonInDatastore(String json, String lectureId) {
    ArrayList<TranscriptLine> transcriptLineArray = extractJsonAsArrayList(json);
    Key lectureKey = KeyFactory.createKey(TranscriptServlet.PARAM_LECTURE, Long.parseLong(lectureId));
    for (int i = 0; i < transcriptLineArray.size(); i++) {
      Entity lineEntity = new Entity(TranscriptLine.ENTITY_KIND);
      lineEntity.setProperty(TranscriptLine.PROP_LECTURE, lectureKey);
      lineEntity.setProperty(TranscriptLine.PROP_START, "");
      lineEntity.setProperty(TranscriptLine.PROP_DURATION, "");
      lineEntity.setProperty(TranscriptLine.PROP_CONTENT, "");
      datastore.put(lineEntity);
    }
  }

  private int countEntitiesInDatastore(String lectureId) {
    return datastore.prepare(filteredQuery(lectureId)).countEntities(withLimit(10));
  }

  private Query filteredQuery(String lectureId) {
    Key lecture = KeyFactory.createKey(TranscriptServlet.PARAM_LECTURE, Long.parseLong(LECTURE_ID_A));
    Filter lectureFilter =
        new FilterPredicate(TranscriptLine.PROP_LECTURE, FilterOperator.EQUAL, lecture);
    return new Query(TranscriptLine.ENTITY_KIND).setFilter(lectureFilter);    
  }
}
