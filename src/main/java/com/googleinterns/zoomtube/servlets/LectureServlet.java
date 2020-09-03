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
  @VisibleForTesting static final String PARAM_LANGUAGE = "language-input";

  /* Pattern used to create a matcher for a video ID. */
  private static Pattern videoUrlGeneratedPattern = Pattern.compile(YOUTUBE_VIDEO_URL_PATTERN);
  private static DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  // TODO: Check if videoId is a valid YouTube video. See: #224.
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    Optional<String> error = validatePostRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
      return;
    }

    String videoUrl = request.getParameter(PARAM_LINK);
    Optional<String> videoId = getVideoId(videoUrl);
    if (!videoId.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_LINK);
      return;
    }

    Optional<Entity> existingEntity = queryForLectureWithVideoId(videoId.get());
    if (existingEntity.isPresent()) {
      response.sendRedirect(buildRedirectUrl(existingEntity.get()));
      return;
    }

    String lectureName = request.getParameter(PARAM_NAME);
    Entity lectureEntity = LectureUtil.createEntity(lectureName, videoUrl, videoId.get());
    datastore.put(lectureEntity);
    try {
      Optional<String> transcriptLanguage =
          Optional.ofNullable(request.getParameter(PARAM_LANGUAGE));
      initializeTranscript(lectureEntity, transcriptLanguage);
    } catch (IOException | ServletException e) {
      // If there was an error initializing the transcript, then this lecture won't have one.
      // Luckily that's still ok, so we suppress these errors so we can redirect.
      // We might want to log these somewhere, but that's beyond the scope of this project.
    }
    response.sendRedirect(buildRedirectUrl(lectureEntity));
  }

  private Optional<String> validatePostRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_NAME) == null) {
      return Optional.of(ERROR_MISSING_NAME);
    }
    if (request.getParameter(PARAM_LINK) == null) {
      return Optional.of(ERROR_MISSING_LINK);
    }
    return Optional.empty();
  }

  /**
   * Parses and stores the transcript lines in datastore using the properties
   * in {@code lectureEntity}. The language for parsing is determined by
   * {@code transcriptLanguage}.
   */
  private void initializeTranscript(Entity lectureEntity, Optional<String> transcriptLanguage)
      throws IOException, ServletException {
    TranscriptParser transcriptParser = TranscriptParser.getParser();
    Key lectureKey = lectureEntity.getKey();
    String videoId = (String) lectureEntity.getProperty(LectureUtil.VIDEO_ID);
    transcriptParser.parseAndStoreTranscript(videoId, lectureKey, transcriptLanguage.orElse(""));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> error = validateGetRequest(request);
    if (error.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, error.get());
      return;
    }

    long lectureId = Long.parseLong(request.getParameter(PARAM_ID));
    Key lectureEntityKey = KeyFactory.createKey(LectureUtil.KIND, lectureId);
    try {
      Entity lectureEntity = datastore.get(lectureEntityKey);
      Gson gson = new Gson();
      response.setContentType("application/json");
      response.getWriter().println(gson.toJson(LectureUtil.createLecture(lectureEntity)));
    } catch (EntityNotFoundException entityNotFound) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_LECTURE_NOT_FOUND);
    }
  }

  private Optional<String> validateGetRequest(HttpServletRequest request) {
    if (request.getParameter(PARAM_ID) == null) {
      return Optional.of(ERROR_MISSING_ID);
    }
    return Optional.empty();
  }

  /**
   * Returns the Entity in database that has {@code videoId}, or
   * {@code Optional.empty()} if one doesn't exist.
   */
  private Optional<Entity> queryForLectureWithVideoId(String videoId) {
    Filter videoIdFilter = new FilterPredicate(LectureUtil.VIDEO_ID, FilterOperator.EQUAL, videoId);
    Query query = new Query(LectureUtil.KIND).setFilter(videoIdFilter);
    PreparedQuery results = datastore.prepare(query);

    return Optional.ofNullable(results.asSingleEntity());
  }

  /** Returns YouTube video ID for a given {@code videoUrl}. */
  // TODO: Move this function to a Utils class.
  public static Optional<String> getVideoId(String videoUrl) {
    Matcher matcher = videoUrlGeneratedPattern.matcher(videoUrl);
    if (matcher.find()) {
      return Optional.of(matcher.group());
    }
    return Optional.empty();
  }

  /**
   * Returns URL for the lecture view page for {@code lectureEntity}.
   */
  private String buildRedirectUrl(Entity lectureEntity) throws ServletException {
    String lectureId = String.valueOf(lectureEntity.getKey().getId());
    try {
      URIBuilder urlBuilder = new URIBuilder(REDIRECT_URL).addParameter(PARAM_ID, lectureId);
      return urlBuilder.build().toString();
    } catch (URISyntaxException e) {
      // This should never happen because our params are constants.
      // But if it does, we let it bubble up.
      throw new ServletException(e.getCause());
    }
  }
}
