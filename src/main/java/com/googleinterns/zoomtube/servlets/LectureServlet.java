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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.Lecture;
import com.googleinterns.zoomtube.utils.LectureEntityFields;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;

/** Provides information on a lecture. */
@WebServlet("/lecture")
public class LectureServlet extends HttpServlet {
  /* Used to generate a Pattern for a Video URL. */
  private static final String YOUTUBE_VIDEO_URL_PATTERN =
      "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|"
      + "watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|"
      + "embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

  /* Name of input field used for lecture name in lecture selection page. */
  private static final String NAME_INPUT = "name-input";
  /* Name of input field used for lecture video link in lecture selection page. */
  private static final String LINK_INPUT = "link-input";
  /* Default value if new lecture inputs are empty. */
  private static final String DEFAULT_VALUE = "";
  private static final String REDIRECT_URL = "/lecture-view.html";

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
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> optionalVideoUrl = getParameter(request, LINK_INPUT);
    String videoUrl = optionalVideoUrl.isPresent() ? optionalVideoUrl.get() : "";
    Optional<Entity> existingEntity = checkUrlInDatabase(videoUrl);

    if (existingEntity.isPresent()) {
      response.sendRedirect(buildRedirectUrl(existingEntity.get()));
      return;
    }
    Entity lectureEntity = createLectureEntity(request);
    datastore.put(lectureEntity);
    response.sendRedirect(buildRedirectUrl(lectureEntity));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Lecture> lectures = getLectures();
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(lectures));
  }

  /**
   * Returns the Entity in database that has {@code url}, or
   * {@code Optional.empty()} if one doesn't exist.
   */
  private Optional<Entity> checkUrlInDatabase(String url) {
    Query query = new Query(LectureEntityFields.KIND);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Iterable<Entity> resultsIterable = results.asIterable();

    for (Entity lecture : resultsIterable) {
      if (lecture.getProperty(LectureEntityFields.VIDEO_URL).equals(url)) {
        return Optional.of(lecture);
      }
    }
    return Optional.empty();
  }

  /** Creates and returns an Entity using parameters found in {@code request}. */
  private Entity createLectureEntity(HttpServletRequest request) {
    Optional<String> optionalLectureName = getParameter(request, NAME_INPUT);
    String lectureName = optionalLectureName.isPresent() ? optionalLectureName.get() : "";
    Optional<String> optionalVideoUrl = getParameter(request, LINK_INPUT);
    String videoUrl = optionalVideoUrl.isPresent() ? optionalVideoUrl.get() : "";
    Optional<String> optionalVideoId = getVideoId(videoUrl);
    String videoId = optionalVideoId.isPresent() ? optionalVideoId.get() : "";

    Entity lectureEntity = new Entity(LectureEntityFields.KIND);
    lectureEntity.setProperty(LectureEntityFields.NAME, lectureName);
    lectureEntity.setProperty(LectureEntityFields.VIDEO_URL, videoUrl);
    lectureEntity.setProperty(LectureEntityFields.VIDEO_ID, videoId);
    return lectureEntity;
  }

  /** Returns lectures stored in the database. */
  private List<Lecture> getLectures() {
    Query query = new Query(LectureEntityFields.KIND);
    PreparedQuery results = datastore.prepare(query);
    List<Lecture> lectures = new ArrayList<>();
    for (Entity lecture : results.asIterable()) {
      Lecture newLecture = Lecture.fromLectureEntity(lecture);
      lectures.add(newLecture);
    }
    return lectures;
  }

  /**
   * Returns the value of {@code name} from the {@code request} form.
   * If the {@code name} cannot be found, return {@code Optional.empty()}.
   */
  private Optional<String> getParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /** Returns YouTube video ID for a given {@code videoUrl}. */
  private Optional<String> getVideoId(String videoUrl) {
    Matcher matcher = videoUrlGeneratedPattern.matcher(videoUrl);
    if (matcher.find()) {
      return Optional.of(matcher.group());
    }
    // TODO: Throw an error saying ID not found.
    return Optional.empty();
  }

  /**
   * Returns URL redirecting to lecture view page with parameters {@code lectureId}
   * and {@code videoId} found in {@code lectureEntity}.
   */
  private String buildRedirectUrl(Entity lectureEntity) {
    String lectureId = String.valueOf(lectureEntity.getKey().getId());
    String videoId = (String) lectureEntity.getProperty(LectureEntityFields.ID);

    try {
      URIBuilder urlBuilder = new URIBuilder(REDIRECT_URL)
                                  .addParameter(LectureEntityFields.ID, lectureId)
                                  .addParameter(LectureEntityFields.VIDEO_ID, videoId);
      return urlBuilder.build().toString();
    } catch (URISyntaxException urlBuilderError) {
      throw new RuntimeException(urlBuilderError);
    }
  }
}
