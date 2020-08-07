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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.Lecture;
import com.googleinterns.zoomtube.utils.LectureUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns list of Lectures. */
public class LectureListServlet extends HttpServlet {
  private DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Lecture> lectures = getLectures();
    Gson gson = new Gson();
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(lectures));
  }

  /** Returns lectures stored in the database. */
  private List<Lecture> getLectures() {
    Query query = new Query(LectureUtil.KIND);
    PreparedQuery results = datastore.prepare(query);
    List<Lecture> lectures = new ArrayList<>();
    for (Entity lectureEntity : results.asIterable()) {
      Lecture lecture = LectureUtil.createLecture(lectureEntity);
      lectures.add(lecture);
    }
    return lectures;
  }
}
