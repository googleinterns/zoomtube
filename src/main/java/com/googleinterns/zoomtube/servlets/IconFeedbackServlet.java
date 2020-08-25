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
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.IconFeedback;
import com.googleinterns.zoomtube.utils.IconFeedbackUtil;
import com.googleinterns.zoomtube.utils.LectureUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Obtains list of available Lectures stored in Datastore. */
public class IconFeedbackServlet extends HttpServlet {
  private DatastoreService datastore;

  private static final String PARAM_LECTURE_ID = "lectureId";
  private static final String PARAM_TIMESTAMP = "timestampMs";
  private static final String PARAM_ICON_TYPE = "iconType";

  private static final String ERROR_MISSING_LECTURE_ID = "Missing lecture id parameter.";
  private static final String ERROR_MISSING_TIMESTAMP = "Missing timestamp parameter.";
  private static final String ERROR_MISSING_ICON_TYPE = "Missing icon type parameter.";

  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> error = validatePostRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
      return;
    }

    long lectureId = Long.parseLong(request.getParameter(PARAM_LECTURE_ID));
    long videoTimeStamp = Long.parseLong(request.getParameter(PARAM_TIMESTAMP));
    IconFeedback.Type type = IconFeedback.Type.valueOf(request.getParameter(PARAM_ICON_TYPE));
    Key lectureEntityKey = KeyFactory.createKey(LectureUtil.KIND, lectureId);

    Entity iconFeedbackEntity =
        IconFeedbackUtil.createEntity(lectureEntityKey, videoTimeStamp, type);
    System.out.println(iconFeedbackEntity);
    System.out.println();
    datastore.put(iconFeedbackEntity);
  }

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
}
