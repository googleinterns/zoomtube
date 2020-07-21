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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.common.io.CharStreams;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.Comment;
import java.io.IOException;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages discussions for a lecture.
 */
@WebServlet("/discussion")
public class DiscussionServlet extends HttpServlet {
  private static final String PARAM_LECTURE = "lecture";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(/* kind=*/"Lecture", lectureId);
    String content = CharStreams.toString(request.getReader());

    // TODO: Most of these values are default placeholders. Add real values as features are added.
    Entity commentEntity = new Entity(Comment.ENTITY_KIND);
    commentEntity.setProperty(Comment.PROP_LECTURE, lecture);
    commentEntity.setProperty(Comment.PROP_PARENT, null);
    commentEntity.setProperty(Comment.PROP_TIMESTAMP, new Date(0));
    commentEntity.setProperty(Comment.PROP_AUTHOR, "");
    commentEntity.setProperty(Comment.PROP_CONTENT, content);
    commentEntity.setProperty(Comment.PROP_CREATED, new Date());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey("Lecture", lectureId);
    Filter lectureFilter = new FilterPredicate(Comment.PROP_LECTURE, FilterOperator.EQUAL, lecture);

    Query query = new Query(Comment.ENTITY_KIND)
                      .setFilter(lectureFilter)
                      .addSort(Comment.PROP_TIMESTAMP, SortDirection.ASCENDING)
                      .addSort(Comment.PROP_CREATED, SortDirection.DESCENDING);
    PreparedQuery pq = datastore.prepare(query);

    ImmutableList.Builder<Comment> commentsBuilder = new ImmutableList.Builder<>();
    for (Entity entity : pq.asQueryResultIterable()) {
      commentsBuilder.add(Comment.fromEntity(entity));
    }
    ImmutableList<Comment> comments = commentsBuilder.build();

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }
}
