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

package com.google.lecturechat.servlets;

import com.google.gson.Gson;
import com.google.lecturechat.data.AuthStatus;
import com.google.lecturechat.data.DatastoreAccess;
import com.google.lecturechat.data.Event;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

/** Servlet for joining an event and listing all the events that the user joined. */
@WebServlet("/joined-events")
public class JoinedEventsServlet extends HttpServlet {

  private static final String EVENT_ID_PARAMETER = "event-id";
  private static DatastoreAccess datastore;

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> optionalUserId = AuthStatus.getUserId(request);

    if (optionalUserId.isPresent()) {
      String userId = optionalUserId.get();
      List<Event> events = datastore.getJoinedEvents(userId);
      response.setContentType("application/json;");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(events));
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> optionalUserId = AuthStatus.getUserId(request);

    if (optionalUserId.isPresent()) {
      String userId = optionalUserId.get();

      try {
        long eventId = Long.parseLong(request.getParameter(EVENT_ID_PARAMETER));
        datastore.joinEvent(userId, eventId);
      } catch (Exception e) {
        throw new BadRequestException(e.getMessage());
      }
    }
  }
}
