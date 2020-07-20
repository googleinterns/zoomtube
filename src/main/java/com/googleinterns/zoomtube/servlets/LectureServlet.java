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
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Provides information on a lecture. */
@WebServlet("/lecture")
public class LectureServlet extends HttpServlet {
  
  /* Used to generate a Pattern for a Video URL. */
  private static final String PATTERN = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";

  /* Used to create Entity and its fields */
  private static final String LECTURE = "Lecture";
  private static final String LECTURE_NAME = "lectureName";
  private static final String VIDEO_URL = "videoUrl";
  private static final String VIDEO_ID = "videoId";

  /* Name of input field used for lecture name in lecture selection page. */
  private static final String NAME_INPUT = "name-input";
  /* Name of input field used for lecture video link in lecture selection page. */
  private static final String VIDEO_INPUT = "video-input";
  /* Default value if new lecture inputs are empty. */
  private static final String DEFAULT_VALUE = "";

  /* Pattern used to create a matcher for a video ID. */
  Pattern compiledPattern;

  @Override
  public void init() throws ServletException {
    compiledPattern = Pattern.compile(PATTERN);
  }

  @Override
  // TODO: Check and see if lectureURL is already in database and if it is valid.
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String lectureName = getParameter(request, NAME_INPUT, DEFAULT_VALUE);
    String videoUrl = getParameter(request, VIDEO_INPUT, DEFAULT_VALUE);
    String videoId = getVideoId(videoUrl);

    // Creates Entity and stores in database
    Entity lectureEntity = new Entity(LECTURE);
    lectureEntity.setProperty(LECTURE_NAME, lectureName);
    lectureEntity.setProperty(VIDEO_URL, videoUrl);
    lectureEntity.setProperty(VIDEO_ID, videoId);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(lectureEntity);

    // TODO: Add redirect link to lecture site.
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendError(500, "Not Implemented");
  }

  /**
   * Returns value with {@code name} from the {@code request} form.
   * If the {@code name} cannot be found, return {@code defaultValue}.
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /** Returns YouTube video ID for a given {@code videoUrl}. */
  private String getVideoId(String videoUrl) {
    Matcher matcher = compiledPattern.matcher(videoUrl);
    if (matcher.find()) {
      return matcher.group();
    }
    // TODO: Throw an error saying ID not found.
    return "";
  }
}
