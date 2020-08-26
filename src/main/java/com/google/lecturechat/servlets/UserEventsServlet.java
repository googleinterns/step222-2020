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

/** Servlet for listing all events that the user signed up for. */
@WebServlet("/user-events")
public class UserEventsServlet extends HttpServlet {

  private static DatastoreAccess datastore;
  private static final AuthStatus authStatus = AuthStatus.getInstance();

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!authStatus.isSignedIn()) {
      return;
    }
    List<Long> groups = datastore.getJoinedGroups(authStatus);
    List<Event> events = new ArrayList<Event>();
    if (groups != null) {
      for (int i = 0; i < groups.size(); i++) {
        events.addAll(datastore.getAllEventsFromGroup(groups.get(i)));
      }
    }
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(events));
  }
}
