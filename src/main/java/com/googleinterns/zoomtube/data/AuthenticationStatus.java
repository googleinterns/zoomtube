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

package com.googleinterns.zoomtube.data;

import com.google.appengine.api.users.User;
import com.google.auto.value.AutoValue;
import com.ryanharter.auto.value.gson.GenerateTypeAdapter;
import java.util.Optional;

/** Contains a user's authentication status, with links to login or logout. */
@GenerateTypeAdapter
@AutoValue
public abstract class AuthenticationStatus {
  public static AuthenticationStatus loggedIn(User user, String logoutUrl) {
    return new AutoValue_AuthenticationStatus(
        true, Optional.of(user), Optional.empty(), Optional.of(logoutUrl));
  }

  public static AuthenticationStatus loggedOut(String loginUrl) {
    return new AutoValue_AuthenticationStatus(
        false, Optional.empty(), Optional.of(loginUrl), Optional.empty());
  }

  /** Returns {@code true} if the user is logged in. */
  public abstract boolean loggedIn();

  /**
   * Returns the current user's information if they are logged in.  Otherwise, returns
   * {@code Optional.empty()}.
   */
  public abstract Optional<User> user();

  /**
   * Returns a url that can be visited to login.  If already logged in,
   * returns {@code Optional.empty()}.
   */
  public abstract Optional<String> loginUrl();

  /**
   * Returns a url that can be visited to logout.  If already logged out,
   * returns {@code Optional.empty()}.
   */
  public abstract Optional<String> logoutUrl();
}
