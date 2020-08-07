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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.googleinterns.zoomtube.data.AuthenticationStatus;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sends a user their authentication information, with links to login and logout.
 */
public class AuthenticationServlet extends HttpServlet {
  private static final String REDIRECT_URL = "/";

  private UserService userService;

  @Override
  public void init() throws ServletException {
    userService = UserServiceFactory.getUserService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    User user = userService.getCurrentUser();
    final AuthenticationStatus auth;
    if (user == null) {
      String login = userService.createLoginURL(REDIRECT_URL);
      auth = AuthenticationStatus.loggedOut(login);
    } else {
      String logout = userService.createLogoutURL(REDIRECT_URL);
      auth = AuthenticationStatus.loggedIn(user, logout);
    }

    Gson gson = new Gson();
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(auth));
  }
}
