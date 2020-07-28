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
