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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
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
  @VisibleForTesting static final String PARAM_TIMESTAMP = "timestamp";
  @VisibleForTesting static final String PARAM_TYPE = "type";

  private UserService userService;
  private DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    userService = UserServiceFactory.getUserService();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getParameter(PARAM_LECTURE) == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing lecture parameter.");
      return;
    }
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(LectureUtil.KIND, lectureId);

    User author = userService.getCurrentUser();
    if (author == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not logged in.");
      return;
    }

    if (request.getParameter(PARAM_TYPE) == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing comment type parameter.");
      return;
    }
    Comment.Type type = Comment.Type.valueOf(request.getParameter(PARAM_TYPE));

    // TODO: Require non-zero content length. See: #183.
    String content = CharStreams.toString(request.getReader());
    Date dateNow = new Date(Clock.systemUTC().millis());

    final Entity commentEntity;
    if (type == Comment.Type.REPLY) {
      if (request.getParameter(PARAM_PARENT) == null) {
        response.sendError(
            HttpServletResponse.SC_BAD_REQUEST, "Missing parent parameter for reply comment.");
        return;
      }
      long parentId = Long.parseLong(request.getParameter(PARAM_PARENT));
      Key parent = KeyFactory.createKey(CommentUtil.KIND, parentId);
      commentEntity = CommentUtil.createReplyEntity(lecture, parent, author, content, dateNow);
    } else {
      if (request.getParameter(PARAM_TIMESTAMP) == null) {
        response.sendError(
            HttpServletResponse.SC_BAD_REQUEST, "Missing timestamp parameter for root comment.");
        return;
      }
      long timestampMs = Long.parseLong(request.getParameter(PARAM_TIMESTAMP));
      commentEntity =
          CommentUtil.createRootEntity(lecture, timestampMs, author, content, dateNow, type);
    }

    datastore.put(commentEntity);
    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    if (request.getParameter(PARAM_LECTURE) == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing lecture parameter.");
      return;
    }
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    Filter lectureFilter = new FilterPredicate(CommentUtil.LECTURE, FilterOperator.EQUAL, lecture);

    Query query = new Query(CommentUtil.KIND).setFilter(lectureFilter);
    PreparedQuery pq = datastore.prepare(query);

    ImmutableList.Builder<Comment> commentsBuilder = new ImmutableList.Builder<>();
    for (Entity entity : pq.asQueryResultIterable()) {
      commentsBuilder.add(CommentUtil.createComment(entity));
    }
    ImmutableList<Comment> comments = commentsBuilder.build();

    Gson gson = new Gson();
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(comments));
  }
}
