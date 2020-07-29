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

package com.googleinterns.zoomtube.mocks;

import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This mocks {@link javax.servlet.http.HttpServletRequest} to allow for testing
 * servlets. As more tests and servlets are added, you may need to override additional
 * methods.
 */
public class MockRequest extends HttpServletRequestWrapper {
  private Map<String, String> parameters = new HashMap<>();
  private String content;

  public MockRequest() {
    // This is a bit of a hack, but it compiles and requires that any methods used
    // in servlets must be overidden in this class.
    super(mock(HttpServletRequest.class));
  }

  public void setParameter(String name, String value) {
    parameters.put(name, value);
  }

  @Override
  public String getParameter(String name) {
    return parameters.get(name);
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new StringReader(content));
  }
}