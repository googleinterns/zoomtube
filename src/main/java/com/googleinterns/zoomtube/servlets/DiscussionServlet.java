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
import java.util.Optional;
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

  @VisibleForTesting static final String ERROR_MISSING_LECTURE = "Missing lecture parameter.";
  @VisibleForTesting
  static final String ERROR_MISSING_COMMENT_TYPE = "Missing comment type parameter.";
  @VisibleForTesting
  static final String ERROR_MISSING_PARENT = "Missing parent parameter for reply comment.";
  @VisibleForTesting
  static final String ERROR_MISSING_TIMESTAMP = "Missing timestamp parameter for root comment.";
  @VisibleForTesting static final String ERROR_NOT_LOGGED_IN = "You are not logged in.";

  private UserService userService;
  private DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    userService = UserServiceFactory.getUserService();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> error = validatePostRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
      return;
    }

    User author = userService.getCurrentUser();
    if (author == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, ERROR_NOT_LOGGED_IN);
      return;
    }

    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE));
    Key lecture = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    Comment.Type type = Comment.Type.valueOf(request.getParameter(PARAM_TYPE));
    // TODO: Require non-zero content length. See: #183.
    String content = CharStreams.toString(request.getReader());
    Date dateNow = new Date(Clock.systemUTC().millis());

    final Entity commentEntity;
    if (type == Comment.Type.REPLY) {
      long parentId = Long.parseLong(request.getParameter(PARAM_PARENT));
      Key parent = KeyFactory.createKey(CommentUtil.KIND, parentId);
      commentEntity = CommentUtil.createReplyEntity(lecture, parent, author, content, dateNow);
    } else {
      long timestampMs = Long.parseLong(request.getParameter(PARAM_TIMESTAMP));
      commentEntity =
          CommentUtil.createRootEntity(lecture, timestampMs, author, content, dateNow, type);
    }
    datastore.put(commentEntity);

    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  private Optional<String> validatePostRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_LECTURE) == null) {
      return Optional.of(ERROR_MISSING_LECTURE);
    }
    if (request.getParameter(PARAM_TYPE) == null) {
      return Optional.of(ERROR_MISSING_COMMENT_TYPE);
    }

    Comment.Type type = Comment.Type.valueOf(request.getParameter(PARAM_TYPE));
    if (type == Comment.Type.REPLY) {
      // Replies need a parent.
      if (request.getParameter(PARAM_PARENT) == null) {
        return Optional.of(ERROR_MISSING_PARENT);
      }
    } else {
      // Root (non-reply) comments need a timestamp.
      if (request.getParameter(PARAM_TIMESTAMP) == null) {
        return Optional.of(ERROR_MISSING_TIMESTAMP);
      }
    }
    return Optional.empty();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> error = validateGetRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
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

  private Optional<String> validateGetRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_LECTURE) == null) {
      return Optional.of(ERROR_MISSING_LECTURE);
    }
    return Optional.empty();
  }
}
