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
import com.google.lecturechat.data.DatastoreAccess;
import com.google.lecturechat.data.Event;
import com.google.lecturechat.data.AuthStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

/**
 * Servlet for listing all events in a certain group and adding a new event to
 * a certain group.
 */
@WebServlet("/group-events")
public class GroupEventsServlet extends HttpServlet {

  private static final String GROUP_ID_PARAMETER = "group-id";
  private static final String TITLE_PARAMETER = "title";
  private static final String START_DATE_PARAMETER = "start";
  private static final String END_DATE_PARAMETER = "start";
  private static DatastoreAccess datastore;
  private static final AuthStatus authStatus = AuthStatus.getInstance();

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!authStatus.isSignedIn()) {
      return;
    }

    try {
      long groupId = Long.parseLong(request.getParameter(GROUP_ID_PARAMETER));
      String title = (String)request.getParameter(TITLE_PARAMETER);
      long start = Long.parseLong(request.getParameter(START_DATE_PARAMETER));
      long end = Long.parseLong(request.getParameter(END_DATE_PARAMETER));

      datastore.addEventToGroup(groupId, title, start, end, authStatus.getUserId());
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!authStatus.isSignedIn()) {
      return;
    }

    try {
      long groupId = Long.parseLong(request.getParameter(GROUP_ID_PARAMETER));
      List<Event> events = datastore.getAllEventsFromGroup(groupId);
      response.setContentType("application/json;");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(events));
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }
}
