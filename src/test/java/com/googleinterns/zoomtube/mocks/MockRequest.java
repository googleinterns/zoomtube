package com.googleinterns.zoomtube.mocks;

import static org.mockito.Mockito.mock;

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
  public Map<String, String> parameters = new HashMap<>();

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
}
