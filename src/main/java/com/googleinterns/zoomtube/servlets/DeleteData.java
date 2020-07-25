
package com.googleinterns.zoomtube.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.googleinterns.zoomtube.data.TranscriptLine;
import java.io.IOException;
import java.util.stream.StreamSupport;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes all of the transcript lines. */
// TODO: Delete this method after testing is complete.
@WebServlet("/delete-data")
public class DeleteData extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(TranscriptLine.ENTITY_KIND);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Iterable<Entity> resultsIterable = results.asIterable();

    StreamSupport.stream(resultsIterable.spliterator(), false)
        .map(entity -> entity.getKey())
        .forEach(datastore::delete);
    System.out.println("DeleteData doPost");
    response.sendRedirect("/");
  }
}