// // Copyright 2019 Google LLC
// //
// // Licensed under the Apache License, Version 2.0 (the "License");
// // you may not use this file except in compliance with the License.
// // You may obtain a copy of the License at
// //
// //     https://www.apache.org/licenses/LICENSE-2.0
// //
// // Unless required by applicable law or agreed to in writing, software
// // distributed under the License is distributed on an "AS IS" BASIS,
// // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// // See the License for the specific language governing permissions and
// // limitations under the License.

// package com.googleinterns.zoomtube.servlets;
// import com.google.common.collect.ImmutableList;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Collection;
// import java.util.Collections;
// import java.util.List;
// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.junit.runners.JUnit4;
// import org.springframework.mock.web.MockHttpServletRequest;
// import org.springframework.mock.web.MockHttpServletResponse;
// import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
// import com.google.appengine.tools.development.testing.LocalServiceTestConfig;
// import com.google.gson.Gson;
// import com.googleinterns.zoomtube.data.TranscriptLine;
// import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
// import static org.junit.Assert.assertEquals;
// import com.google.appengine.api.datastore.Entity;
// import com.google.appengine.api.datastore.Key;
// import com.google.appengine.api.datastore.KeyFactory;
// import com.google.appengine.api.datastore.PreparedQuery;
// import com.google.appengine.api.datastore.Query;
// import com.google.appengine.api.datastore.Query.Filter;
// import com.google.appengine.api.datastore.Query.FilterOperator;
// import com.google.appengine.api.datastore.Query.FilterPredicate;
// import com.google.appengine.api.datastore.Query.SortDirection;
// import com.google.appengine.repackaged.com.google.gson.JsonSyntaxException;
// import com.google.appengine.api.datastore.DatastoreService;
// import com.google.appengine.api.datastore.DatastoreServiceFactory;
// import com.google.appengine.api.datastore.Entity;
// import com.google.appengine.api.datastore.Query;
// import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
// import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
// import org.junit.After;
// import org.junit.Before;
// import org.junit.Test;

// import static org.junit.Assert.assertTrue;
// import static org.mockito.Mockito.*;
// import static com.google.common.truth.Truth.assertThat;
// import java.io.IOException;
// import javax.servlet.ServletException;
// import com.google.gson.GsonBuilder;
// import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
// import com.google.appengine.api.datastore.DatastoreService;
// import com.google.appengine.api.datastore.DatastoreServiceFactory;


// /** */
// @RunWith(JUnit4.class)
// public final class TranscriptServletTest {
//   private TranscriptServlet servlet;
//   private MockHttpServletRequest request;
//   private MockHttpServletResponse response;
//   private LocalDatastoreServiceTestConfig datastoreConfig = (new  LocalDatastoreServiceTestConfig()).setNoStorage(true);
//   private final LocalServiceTestHelper localServiceHelper = new LocalServiceTestHelper(datastoreConfig);
//   private DatastoreService datastore;
//   private Gson gson;

//   private static final String LECTURE_ID_B = "345";
//   private static final String LECTURE_ID_C = "234";
//   private static final String LECTURE_ID_A = "123";

//   private static final String SHORT_VIDEO_ID = "Obgnr9pc820";
//   private static final String SHORT_VIDEO_JSON = 
//       "[{\"key\":{\"kind\":\"TranscriptLine\",\"id\":1},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"0.4\",\"duration\":\"1\",\"content\":\" \"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":2},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"2.28\",\"duration\":\"1\",\"content\":\"Hi\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":3},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"5.04\",\"duration\":\"1.6\",\"content\":\"Okay\"}]";
//   private static final String LONG_VIDEO_ID = "jNQXAC9IVRw";
//   private static final String LONG_VIDEO_JSON = 
//       "[{\"key\":{\"kind\":\"TranscriptLine\",\"id\":1},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"1.3\",\"duration\":\"3.1\",\"content\":\"All right, so here we are\\nin front of the elephants,\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":4},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"12.7\",\"duration\":\"4.3\",\"content\":\"and that&#39;s, that&#39;s cool.\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":5},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"17\",\"duration\":\"1.767\",\"content\":\"And that&#39;s pretty much all there is to say.\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":2},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"4.4\",\"duration\":\"4.766\",\"content\":\"the cool thing about these guys\\nis that they have really,\"},{\"key\":{\"kind\":\"TranscriptLine\",\"id\":3},\"lecture\":{\"kind\":\"lecture\",\"id\":123},\"start\":\"9.166\",\"duration\":\"3.534\",\"content\":\"really, really long trunks,\"}]";
  
  
//   @Before
//   public void setUp() throws ServletException {
//     localServiceHelper.setUp();
//     servlet = new TranscriptServlet();
//     datastore = DatastoreServiceFactory.getDatastoreService();
//     servlet.init(datastore);
//     request = new MockHttpServletRequest();
//     response = new MockHttpServletResponse();
//     gson = new GsonBuilder()
//     .registerTypeAdapterFactory(GenerateTypeAdapter.FACTORY)
//     .create();
//   }
  

//   @After
//   public void tearDown() {
//     localServiceHelper.tearDown();
//   }

//   private List<TranscriptLine> extractJsonAsArrayList(String json) {
//     return (ArrayList<TranscriptLine>) gson.fromJson(json,(new ArrayList<List<TranscriptLine>>().getClass()));
//   }

//   @Test
//   public void doGet_getDataInDatastoreForShortVideo() throws ServletException, IOException {
//     putJsonInDatastore(SHORT_VIDEO_JSON, LECTURE_ID_A);
//     request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);
    
//     servlet.doGet(request, response);
//     String actualJson = response.getContentAsString();
//     List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(SHORT_VIDEO_JSON);
//     List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

//     assertThat(actualJsonArray.size()).isEqualTo(expectedArrayList.size());
//   }

//   @Test
//   public void doPost_persistDataInDatastoreForShortVideo() throws ServletException, IOException {
//     request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, SHORT_VIDEO_ID);
//     request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_B);
//     servlet.doPost(request, response);

//     int actualQueryCount = countEntitiesInDatastore(LECTURE_ID_B);
//     int expectedQueryCount = (extractJsonAsArrayList(SHORT_VIDEO_JSON)).size();
    
//     assertThat(actualQueryCount).isEqualTo(expectedQueryCount);
//   }

//   @Test
//   public void doGet_doPost_StoreAndRetrieveShortVideo() throws ServletException, IOException {
//     request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, SHORT_VIDEO_ID);
//     request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);
//     servlet.doPost(request, response);
//     servlet.doGet(request, response);
    
//     String actualJson = response.getContentAsString();
//     List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(SHORT_VIDEO_JSON);
//     List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);
    
//     assertThat(actualJsonArray).isEqualTo(expectedArrayList); 
//   }

//   @Test
//   public void doGet_doPost_StoreAndRetrieveLongVideo() throws ServletException, IOException {
//     request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, LONG_VIDEO_ID);
//     request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);  
//     servlet.doPost(request, response);
//     servlet.doGet(request, response);
    
//     String actualJson = response.getContentAsString();
//     List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(LONG_VIDEO_JSON);
//     List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);
    
//     assertThat(actualJsonArray).isEqualTo(expectedArrayList);
//   }

//   @Test
//   public void doPost_persistDataInDatastoreForLongVideo() throws ServletException, IOException {
//     request.addParameter(TranscriptServlet.PARAM_VIDEO_ID, LONG_VIDEO_ID);
//     request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_C);
//     servlet.doPost(request, response);

//     int actualQueryCount = countEntitiesInDatastore(LECTURE_ID_C);
//     int expectedQueryCount = (extractJsonAsArrayList(LONG_VIDEO_JSON)).size();
    
//     assertThat(actualQueryCount).isEqualTo(expectedQueryCount);
//   }

//   @Test
//   public void doGet_OnlyOtherLecturesInDatastore_GetNoLectures() throws ServletException, IOException {
//     putJsonInDatastore(SHORT_VIDEO_JSON, LECTURE_ID_B);
//     putJsonInDatastore(LONG_VIDEO_JSON, LECTURE_ID_A);
//     request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_C);  
    
//     servlet.doGet(request, response);
//     String actualJson = response.getContentAsString();
//     List<TranscriptLine> expectedArrayList = new ArrayList<>();
//     List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

//     assertThat(actualJsonArray).isEqualTo(expectedArrayList); 
//   }

//   @Test
//   public void doGet_TwoLecturesInDatastore_GetOneLecture() throws ServletException, IOException {
//     putJsonInDatastore(SHORT_VIDEO_JSON, LECTURE_ID_B);
//     putJsonInDatastore(LONG_VIDEO_JSON, LECTURE_ID_A);
//     request.addParameter(TranscriptServlet.PARAM_LECTURE_ID, LECTURE_ID_A);  
    
//     servlet.doGet(request, response);
//     String actualJson = response.getContentAsString();
//     List<TranscriptLine> expectedArrayList = extractJsonAsArrayList(LONG_VIDEO_JSON);
//     List<TranscriptLine> actualJsonArray = extractJsonAsArrayList(actualJson);

//     assertThat(actualJsonArray).isEqualTo(expectedArrayList); 
//   }

//   private void putJsonInDatastore(String json, String lectureId) {
//     List<TranscriptLine> transcriptLineArray = extractJsonAsArrayList(json);
//     Key lectureKey = KeyFactory.createKey(TranscriptServlet.PARAM_LECTURE, Long.parseLong(lectureId));
//     for (int i = 0; i < transcriptLineArray.size(); i++) {
//       Entity lineEntity = new Entity(TranscriptLine.ENTITY_KIND);
//       lineEntity.setProperty(TranscriptLine.PROP_LECTURE, lectureKey);
//       lineEntity.setProperty(TranscriptLine.PROP_START, lectureId);
//       lineEntity.setProperty(TranscriptLine.PROP_DURATION, lectureId);
//       lineEntity.setProperty(TranscriptLine.PROP_CONTENT, lectureId);
//       datastore.put(lineEntity);
//     }
//   }

//   private int countEntitiesInDatastore(String lectureId) {
//     return datastore.prepare(filteredQuery(lectureId)).countEntities(withLimit(100));
//   }

//   private Query filteredQuery(String lectureId) {
//     Key lecture = KeyFactory.createKey(TranscriptServlet.PARAM_LECTURE, Long.parseLong(lectureId));
//     Filter lectureFilter =
//         new FilterPredicate(TranscriptLine.PROP_LECTURE, FilterOperator.EQUAL, lecture);
//     return new Query(TranscriptLine.ENTITY_KIND).setFilter(lectureFilter);    
//   }
// }
