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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.IconFeedback;
import com.googleinterns.zoomtube.utils.IconFeedbackUtil;
import com.googleinterns.zoomtube.utils.LectureUtil;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles storing and retrieving IconFeedback from database. */
public class IconFeedbackServlet extends HttpServlet {
  /* URL search parameters used in request. */
  @VisibleForTesting static final String PARAM_LECTURE_ID = "lectureId";
  @VisibleForTesting static final String PARAM_TIMESTAMP = "timestampMs";
  @VisibleForTesting static final String PARAM_ICON_TYPE = "iconType";

  /* Error messages for missing parameters. */
  private static final String ERROR_MISSING_LECTURE_ID = "Missing lecture id parameter.";
  private static final String ERROR_MISSING_TIMESTAMP = "Missing timestamp parameter.";
  private static final String ERROR_MISSING_ICON_TYPE = "Missing icon type parameter.";

  private DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> postRequestError = validatePostRequest(request);
    if (postRequestError.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, postRequestError.get());
      return;
    }
    datastore.put(createEntityFromRequest(request));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> getRequestError = validateGetRequest(request);
    if (getRequestError.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, getRequestError.get());
      return;
    }

    List<IconFeedback> lectures = getIconFeedback(request);
    Gson gson = new Gson();
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(lectures));
  }

  /** Returns IconFeedback (associated with a specific lecture) from the database. */
  private List<IconFeedback> getIconFeedback(HttpServletRequest request) {
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE_ID));
    Key lecture = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    Filter lectureFilter =
        new FilterPredicate(IconFeedbackUtil.LECTURE, FilterOperator.EQUAL, lecture);

    Query query = new Query(IconFeedbackUtil.KIND)
                      .setFilter(lectureFilter)
                      .addSort(IconFeedbackUtil.TIMESTAMP_MS, SortDirection.ASCENDING);
    PreparedQuery pq = datastore.prepare(query);
    ImmutableList.Builder<IconFeedback> iconFeedbackListBuilder = new ImmutableList.Builder<>();

    for (Entity iconFeedbackEntity : pq.asQueryResultIterable()) {
      iconFeedbackListBuilder.add(IconFeedbackUtil.createIconFeedback(iconFeedbackEntity));
    }
    ImmutableList<IconFeedback> iconFeedbackList = iconFeedbackListBuilder.build();
    return iconFeedbackList;
  }

  /** Returns an IconFeedback Entity from parameters found in {@code request}. */
  private Entity createEntityFromRequest(HttpServletRequest request) {
    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE_ID));
    long videoTimeStamp = Long.parseLong(request.getParameter(PARAM_TIMESTAMP));
    IconFeedback.Type type = IconFeedback.Type.valueOf(request.getParameter(PARAM_ICON_TYPE));
    Key lectureEntityKey = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    return IconFeedbackUtil.createEntity(lectureEntityKey, videoTimeStamp, type);
  }

  /** Ensures request paramters are present. Returns error message if any of them are missing. */
  private Optional<String> validatePostRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_LECTURE_ID) == null) {
      return Optional.of(ERROR_MISSING_LECTURE_ID);
    }
    if (request.getParameter(PARAM_TIMESTAMP) == null) {
      return Optional.of(ERROR_MISSING_TIMESTAMP);
    }
    if (request.getParameter(PARAM_ICON_TYPE) == null) {
      return Optional.of(ERROR_MISSING_ICON_TYPE);
    }
    return Optional.empty();
  }

  /** Ensures request paramters are present. Returns error message if any of them are missing. */
  private Optional<String> validateGetRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_LECTURE_ID) == null) {
      return Optional.of(ERROR_MISSING_LECTURE_ID);
    }
    return Optional.empty();
  }
}
