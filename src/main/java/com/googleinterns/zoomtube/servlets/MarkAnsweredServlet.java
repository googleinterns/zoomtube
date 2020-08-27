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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.annotations.VisibleForTesting;
import com.googleinterns.zoomtube.data.Comment;
import com.googleinterns.zoomtube.utils.CommentUtil;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Marks discussion questions as answered or unanswered.
 */
public class MarkAnsweredServlet extends HttpServlet {
  @VisibleForTesting static final String PARAM_COMMENT = "comment";
  @VisibleForTesting static final String PARAM_NEW_TYPE = "new-type";

  private static final String ERROR_MISSING_COMMENT = "Missing comment parameter.";
  private static final String ERROR_MISSING_NEW_TYPE = "Missing new type parameter.";
  private static final String ERROR_INVALID_COMMENT = "Specified comment could not be found.";
  private static final String ERROR_INVALID_COMMENT_TYPE = "Specified comment is not a question.";
  private static final String ERROR_INVALID_NEW_TYPE = "Invalid new type.";
  private static final String ERROR_NOT_LOGGED_IN = "You are not logged in.";

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

    Comment.Type newType = Comment.Type.valueOf(request.getParameter(PARAM_NEW_TYPE));
    if (newType != Comment.Type.QUESTION_UNANSWERED && newType != Comment.Type.QUESTION_ANSWERED) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, ERROR_INVALID_NEW_TYPE);
      return;
    }

    User author = userService.getCurrentUser();
    if (author == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, ERROR_NOT_LOGGED_IN);
      return;
    }

    long commentId = Long.parseLong(request.getParameter(PARAM_COMMENT));
    Key commentKey = KeyFactory.createKey(CommentUtil.KIND, commentId);
    final Entity commentEntity;
    try {
      commentEntity = datastore.get(commentKey);
    } catch (EntityNotFoundException e) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, ERROR_INVALID_COMMENT);
      return;
    }

    Comment comment = CommentUtil.createComment(commentEntity);
    Comment.Type currentType = comment.type();
    if (currentType != Comment.Type.QUESTION_UNANSWERED
        && currentType != Comment.Type.QUESTION_ANSWERED) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, ERROR_INVALID_COMMENT_TYPE);
      return;
    }

    commentEntity.setProperty(CommentUtil.TYPE, newType.toString());
    datastore.put(commentEntity);

    response.setStatus(HttpServletResponse.SC_ACCEPTED);
  }

  private Optional<String> validatePostRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_COMMENT) == null) {
      return Optional.of(ERROR_MISSING_COMMENT);
    }
    if (request.getParameter(PARAM_NEW_TYPE) == null) {
      return Optional.of(ERROR_MISSING_NEW_TYPE);
    }

    return Optional.empty();
  }
}
