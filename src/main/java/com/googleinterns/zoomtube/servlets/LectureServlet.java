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

  @VisibleForTesting static final String PARAM_ID = "id";

  /* Name of input field used for lecture name in lecture selection page. */
  @VisibleForTesting static final String PARAM_NAME = "name-input";

  /* Name of input field used for lecture video link in lecture selection page. */
  @VisibleForTesting static final String PARAM_LINK = "link-input";


  @VisibleForTesting static final String ERROR_MISSING_NAME = "Missing name parameter.";
  @VisibleForTesting static final String ERROR_MISSING_LINK = "Missing link parameter.";
  @VisibleForTesting static final String ERROR_MISSING_ID = "Missing id parameter.";
  @VisibleForTesting static final String ERROR_INVALID_LINK = "Invalid video link.";
  @VisibleForTesting static final String ERROR_FAILED_REDIRECT = "Failed to create redirect URL.";
  @VisibleForTesting static final String ERROR_LECTURE_NOT_FOUND = "Lecture not found in database.";

  private static final String REDIRECT_URL = "/view";

  /* Pattern used to create a matcher for a video ID. */
  private static Pattern videoUrlGeneratedPattern;

  private static DatastoreService datastore;

  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
    videoUrlGeneratedPattern = Pattern.compile(YOUTUBE_VIDEO_URL_PATTERN);
  }

  @Override
  // TODO: Check if URL is valid.
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (request.getParameter(PARAM_NAME) == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MISSING_NAME);
      return;
    }
    String lectureName = request.getParameter(PARAM_NAME);

    if (request.getParameter(PARAM_LINK) == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MISSING_LINK);
      return;
    }
    String videoUrl = request.getParameter(PARAM_LINK);

    Optional<String> videoId = getVideoId(videoUrl);
    if (!videoId.isPresent()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_INVALID_LINK);
      return;
    }

    Optional<Entity> existingEntity = checkUrlInDatabase(videoUrl);
    final Optional<String> redirectUrl;
    if (existingEntity.isPresent()) {
      redirectUrl = buildRedirectUrl(existingEntity.get());
    } else {
      Entity lectureEntity = LectureUtil.createEntity(lectureName, videoUrl, videoId.get());
      datastore.put(lectureEntity);
      initializeTranscript(lectureEntity);
      redirectUrl = buildRedirectUrl(lectureEntity);
    }

    if (!redirectUrl.isPresent()) {
      response.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_FAILED_REDIRECT);
      return;
    }
    response.sendRedirect(redirectUrl.get());
  }

  /**
   * Parses and stores the transcript lines in datastore using the {@code lectureKey}
   * and {@code videoId} properties in {@code lectureEntity}.
   */
  private void initializeTranscript(Entity lectureEntity) throws IOException, ServletException {
    TranscriptParser transcriptParser = TranscriptParser.getParser();
    Key lectureKey = lectureEntity.getKey();
    String videoId = (String) lectureEntity.getProperty(LectureUtil.VIDEO_ID);
    transcriptParser.parseAndStoreTranscript(videoId, lectureKey);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getParameter(PARAM_ID) == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_MISSING_ID);
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

  /**
   * Returns the {@code Entity} in database that has {@code videoUrl}, or
   * {@code Optional.empty()} if one doesn't exist.
   */
  // TODO: Use a filter to avoid fetching all lectures.  See: #185.
  private Optional<Entity> checkUrlInDatabase(String videoUrl) {
    Query query = new Query(LectureUtil.KIND);
    PreparedQuery results = datastore.prepare(query);
    Iterable<Entity> resultsIterable = results.asIterable();

    for (Entity lecture : resultsIterable) {
      if (lecture.getProperty(LectureUtil.VIDEO_URL).equals(videoUrl)) {
        return Optional.of(lecture);
      }
    }
    return Optional.empty();
  }

  @VisibleForTesting
  /** Returns YouTube video ID for a given {@code videoUrl}. */
  protected Optional<String> getVideoId(String videoUrl) {
    Matcher matcher = videoUrlGeneratedPattern.matcher(videoUrl);
    if (matcher.find()) {
      return Optional.of(matcher.group());
    }
    return Optional.empty();
  }

  /**
   * Returns URL for the lecture view page for {@code lectureEntity}, or {@code Optional.empty()} if the URL
   * couldn't be built.
   */
  private Optional<String> buildRedirectUrl(Entity lectureEntity) {
    String lectureId = String.valueOf(lectureEntity.getKey().getId());

    try {
      URIBuilder urlBuilder = new URIBuilder(REDIRECT_URL).addParameter(PARAM_ID, lectureId);
      return Optional.of(urlBuilder.build().toString());
    } catch (URISyntaxException urlBuilderError) {
      return Optional.empty();
    }
  }
}
