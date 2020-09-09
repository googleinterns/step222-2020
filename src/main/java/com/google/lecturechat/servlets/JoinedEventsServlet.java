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

/**
 * Servlet for joining an event and listing all the events that satisfy a specific criteria (all the
 * events joined by the user that are associated with a given group, all the events joined by the
 * user whose start date is between two dates received).
 */
@WebServlet("/joined-events")
public class JoinedEventsServlet extends HttpServlet {

  private static final String EVENT_ID_PARAMETER = "event-id";
  private static final String BEGINNING_DATE_PARAMETER = "beginning-date";
  private static final String ENDING_DATE_PARAMETER = "ending-date";
  private static final String GROUP_ID_PARAMETER = "group-id";
  private static DatastoreAccess datastore;

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> userId = AuthStatus.getUserId(request);

    if (!userId.isPresent()) {
      return;
    }

    List<Event> events;
    String groupId = request.getParameter(GROUP_ID_PARAMETER);

    try {
      if (groupId != null) {
        events = datastore.getAllJoinedEventsFromGroup(Long.parseLong(groupId), userId.get());
      } else {
        String beginningDate = request.getParameter(BEGINNING_DATE_PARAMETER);
        String endingDate = request.getParameter(ENDING_DATE_PARAMETER);

        if (beginningDate == null || endingDate == null) {
          return;
        }
        events =
            datastore.getJoinedEventsThatStartBetweenDates(
                Long.parseLong(beginningDate), Long.parseLong(endingDate), userId.get());
      }
    } catch (NumberFormatException e) {
      throw new BadRequestException(e.getMessage());
    }

    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(events));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> userId = AuthStatus.getUserId(request);

    if (!userId.isPresent()) {
      return;
    }

    try {
      long eventId = Long.parseLong(request.getParameter(EVENT_ID_PARAMETER));
      datastore.joinEvent(userId.get(), eventId);
    } catch (NumberFormatException e) {
      throw new BadRequestException(e.getMessage());
    }
  }
}
