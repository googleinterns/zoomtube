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

    User author = userService.getCurrentUser();
    if (author == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, ERROR_NOT_LOGGED_IN);
      return;
    }

    long commentId = Long.parseLong(request.getParameter(PARAM_COMMENT));
    Key commentKey = KeyFactory.createKey(CommentUtil.KIND, commentId);
    Comment.Type newType = Comment.Type.valueOf(request.getParameter(PARAM_NEW_TYPE));

    error = updateCommentType(commentKey, newType);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
    }

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

  /**
   * Updates the question-type comment specified by `commentKey` to `newType`.  Returns
   * {@Optional.empty()} if there were no errors, otherwise returns an error message.
   */
  private Optional<String> updateCommentType(Key commentKey, Comment.Type newType) {
    if (!isQuestionType(newType)) {
      return Optional.of(ERROR_INVALID_NEW_TYPE);
    }

    final Entity commentEntity;
    try {
      commentEntity = datastore.get(commentKey);
    } catch (EntityNotFoundException e) {
      return Optional.of(ERROR_INVALID_COMMENT);
    }

    Comment.Type currentType = CommentUtil.createComment(commentEntity).type();
    if (!isQuestionType(currentType)) {
      return Optional.of(ERROR_INVALID_COMMENT_TYPE);
    }

    commentEntity.setProperty(CommentUtil.TYPE, newType.toString());
    datastore.put(commentEntity);
    return Optional.empty();
  }

  private boolean isQuestionType(Comment.Type type) {
    return type == Comment.Type.QUESTION_ANSWERED || type == Comment.Type.QUESTION_UNANSWERED;
  }
}
