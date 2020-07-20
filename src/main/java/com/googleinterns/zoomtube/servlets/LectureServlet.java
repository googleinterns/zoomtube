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

import java.util.regex.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity; 

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides information on a lecture.
 */
@WebServlet("/lecture")
public class LectureServlet extends HttpServlet {

  /* Used to create Entity and its fields */
  private static final String LECTURE = "Lecture";
  private static final String LECTURE_NAME = "lectureName";
  private static final String VIDEO_LINK = "videoLink";
  private static final String VIDEO_ID = "videoId";

  /* Name of input field used for lecture name in lecture selection page. */
  private static final String NAME_INPUT = "name-input";
  /* Name of input field used for lecture video link in lecture selection page. */
  private static final String VIDEO_INPUT = "video-input";
  /* Default value if new lecture inputs are empty. */
  private static final String DEFAULT_VALUE = "";

  @Override
  public void init() throws ServletException {
    // TODO: Implement LectureServlet.
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
    String lectureName = getParameter(request, NAME_INPUT, DEFAULT_VALUE);
    String videoLink = getParameter(request, VIDEO_INPUT, DEFAULT_VALUE);
    String videoId = getVideoId(videoLink);

    // Creates Entity and stores in database
    Entity lectureEntity = new Entity(LECTURE);
    lectureEntity.setProperty(LECTURE_NAME, lectureName);
    lectureEntity.setProperty(VIDEO_LINK, videoLink);
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
   * @param request Form sent by client
   * @param name {@code <input>} to read content of
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
  
  /** Returns YouTube video ID for a given {@code videoLink}. */
  private String getVideoId(String videoLink) {
    String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
    Pattern compiledPattern = Pattern.compile(pattern);
    Matcher matcher = compiledPattern.matcher(videoLink);
    if (matcher.find()) {
      return matcher.group();
    }
    // TODO: Throw an error saying ID not found.
    return "";
  }
}
