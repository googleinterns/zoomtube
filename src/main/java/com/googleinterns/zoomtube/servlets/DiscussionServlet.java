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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.Comment;
import com.googleinterns.zoomtube.utils.CommentUtil;
import com.googleinterns.zoomtube.utils.LectureUtil;
import java.io.IOException;
import java.time.Clock;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages discussions for a lecture.
 */
public class DiscussionServlet extends HttpServlet {
  @VisibleForTesting static final String PARAM_LECTURE = "lecture";
  @VisibleForTesting static final String PARAM_PARENT = "parent";

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
    Key lecture = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    String parentIdString = request.getParameter(PARAM_PARENT);
    Key parent = null;
    if (parentIdString != null) {
      long parentId = Long.parseLong(parentIdString);
      parent = KeyFactory.createKey(CommentUtil.KIND, parentId);
    }
    User author = userService.getCurrentUser();
    if (author == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not logged in.");
      return;
    }
    String content = CharStreams.toString(request.getReader());
    // TODO: Get actual video timestamp from request.
    // Use the start of the video for now.
    Date timestamp = new Date(0);
    Date dateNow = new Date(Clock.systemUTC().millis());

    if (parent == null) {
      datastore.put(CommentUtil.createEntity(lecture, timestamp, author, content, dateNow));
    } else {
      datastore.put(CommentUtil.createEntity(lecture, parent, timestamp, author, content, dateNow));
    }

    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    Filter lectureFilter = new FilterPredicate(CommentUtil.LECTURE, FilterOperator.EQUAL, lecture);

    Query query = new Query(CommentUtil.KIND)
                      .setFilter(lectureFilter)
                      .addSort(CommentUtil.TIMESTAMP, SortDirection.ASCENDING)
                      .addSort(CommentUtil.CREATED, SortDirection.DESCENDING);
    PreparedQuery pq = datastore.prepare(query);

    ImmutableList.Builder<Comment> commentsBuilder = new ImmutableList.Builder<>();
    for (Entity entity : pq.asQueryResultIterable()) {
      commentsBuilder.add(CommentUtil.createComment(entity));
    }
    ImmutableList<Comment> comments = commentsBuilder.build();

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }
}
