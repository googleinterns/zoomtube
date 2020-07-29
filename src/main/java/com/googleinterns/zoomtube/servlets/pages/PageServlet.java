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

package com.googleinterns.zoomtube.servlets.pages;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves a static file for logged in users.  If a user isn't logged in, they are
 * redirected to a login page.
 */
public class PageServlet extends HttpServlet {
  private UserService userService;
  private String loginRedirect;
  private String pageFile;

  public PageServlet(String pageFile, String loginRedirect) {
    this.pageFile = pageFile;
    this.loginRedirect = loginRedirect;
  }

  @Override
  public void init() throws ServletException {
    userService = UserServiceFactory.getUserService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User user = userService.getCurrentUser();
    if (user == null) {
      String login = userService.createLoginURL(loginRedirect);
      response.sendRedirect(login);
      return;
    }

    sendFile(response, pageFile);
  }

  /**
   * Reads a file from disk and sends to the {@code response} output stream. This also
   * automatically detects the file's MIME type and updates the content-type header.
   */
  private void sendFile(HttpServletResponse response, String path) throws IOException {
    ServletContext context = getServletContext();
    String mimeType = context.getMimeType(path);
    if (mimeType == null) {
      // Set to binary type if MIME mapping not found.
      mimeType = "application/octet-stream;";
    }
    response.setContentType(mimeType);

    File file = new File(path);
    FileInputStream inStream = new FileInputStream(file);
    response.setContentLength((int) file.length());

    OutputStream outStream = response.getOutputStream();
    byte[] buffer = new byte[4096];
    int bytesRead = -1;
    while ((bytesRead = inStream.read(buffer)) != -1) {
      outStream.write(buffer, 0, bytesRead);
    }

    inStream.close();
    outStream.close();
  }
}
