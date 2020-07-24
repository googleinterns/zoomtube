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
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.io.CharStreams;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.Comment;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages discussions for a lecture.
 */
// TODO: References to Lecture kind string literal need to be updated once Lecture code is merged.
@WebServlet("/discussion")
public class DiscussionServlet extends HttpServlet {
  private static final String PARAM_LECTURE = "lecture";
  private static final String PARAM_PARENT = "parent";

  private UserService userService;
  private DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    userService = UserServiceFactory.getUserService();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(/* kind= */ "Lecture", lectureId);

    String parentIdString = request.getParameter(PARAM_PARENT);
    Key parent = null;
    if (parentIdString != null) {
      long parentId = Long.parseLong(parentIdString);
      parent = KeyFactory.createKey(Comment.ENTITY_KIND, parentId);
    }

    User author = userService.getCurrentUser();
    if (author == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not logged in.");
      return;
    }

    String content = CharStreams.toString(request.getReader());

    Entity commentEntity = new Entity(Comment.ENTITY_KIND);
    commentEntity.setProperty(Comment.PROP_LECTURE, lecture);
    commentEntity.setProperty(Comment.PROP_PARENT, parent);
    // TODO: Add support for timestamped comments
    commentEntity.setProperty(Comment.PROP_TIMESTAMP, new Date(0));
    commentEntity.setProperty(Comment.PROP_AUTHOR, author);
    commentEntity.setProperty(Comment.PROP_CONTENT, content);
    commentEntity.setProperty(Comment.PROP_CREATED, new Date());

    datastore.put(commentEntity);

    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(/* kind= */ "Lecture", lectureId);
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
