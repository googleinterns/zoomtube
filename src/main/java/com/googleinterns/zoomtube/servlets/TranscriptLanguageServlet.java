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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.transcriptParser.TranscriptParser;
import com.googleinterns.zoomtube.utils.LectureUtil;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;

/** Provides information on a lecture. */
public class LectureServlet extends HttpServlet {
  /* Used to generate a Pattern for a Video URL. */
  private static final String YOUTUBE_VIDEO_URL_PATTERN =
      "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|"
      + "watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|"
      + "embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
  private static final String ERROR_MISSING_NAME = "Missing name parameter.";
  private static final String ERROR_MISSING_LINK = "Missing link parameter.";
  private static final String ERROR_MISSING_ID = "Missing id parameter.";
  private static final String ERROR_INVALID_LINK = "Invalid video link.";
  private static final String ERROR_LECTURE_NOT_FOUND = "Lecture not found in database.";
  private static final String REDIRECT_URL = "/view/";

  /* Name of input field used for lecture name in lecture selection page. */
  @VisibleForTesting static final String PARAM_NAME = "name-input";
  /* Name of input field used for lecture video link in lecture selection page. */
  @VisibleForTesting static final String PARAM_LINK = "link-input";
  @VisibleForTesting static final String PARAM_ID = "id";

  /* Pattern used to create a matcher for a video ID. */
  private static Pattern videoUrlGeneratedPattern;

  private static DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    videoUrlGeneratedPattern = Pattern.compile(YOUTUBE_VIDEO_URL_PATTERN);
  }

  @Override
  // TODO: Check if videoId is a valid YouTube video. See: #224.
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> error = validateGetRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
      return;
    }

    long lectureId = Long.parseLong(request.getParameter(PARAM_LINK));

  }

  private Optional<String> validateGetRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_LINK) == null) {
      return Optional.of(ERROR_MISSING_ID);
    }
    return Optional.empty();
  }
}
