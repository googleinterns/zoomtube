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

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This mocks {@link javax.servlet.http.HttpServletResponse} to allow for testing
 * servlets. As more tests and servlets are added, you may need to override additional
 * methods.
 */
public class MockResponse extends HttpServletResponseWrapper {
  private StringWriter content;
  private PrintWriter writer;
  private String contentType;
  private String redirectUrl;
  private int contentLength;
  private int status;

  public MockResponse() {
    // This is a bit of a hack, but it compiles and requires that any methods used
    // in servlets must be overidden in this class.
    super(mock(HttpServletResponse.class));
    content = new StringWriter();
    writer = new PrintWriter(content);
  }

  @Override
  public PrintWriter getWriter() {
    return writer;
  }

  @Override
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  public int getContentLength() {
    return contentLength;
  }
  
  @Override
  public void sendRedirect(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public String getRedirectedUrl() {
    return redirectUrl;
  }

  public String getContentAsString() {
    return content.toString();
  }

  @Override
  public void sendError(int code, String message) {
    setStatus(code);
    writer.println(message);
  }

  @Override
  public void setStatus(int code) {
    status = code;
  }

  @Override
  public int getStatus() {
    return status;
  }
}