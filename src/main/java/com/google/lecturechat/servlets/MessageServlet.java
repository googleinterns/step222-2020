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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for adding a chat message and getting all messages associated with an event. */
@WebServlet("/messages")
public class MessageServlet extends HttpServlet {

  private static final String EVENT_ID_PARAMETER = "id";
  private static final String MESSAGE_PARAMETER = "message";
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

    try {
      long eventId = Long.parseLong(request.getParameter(EVENT_ID_PARAMETER));
      List<String> messages = datastore.getMessagesFromEvent(eventId);
      if (messages == null) {
        messages = new ArrayList<String>();
      }
      response.setContentType("application/json;");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(messages));
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> userId = AuthStatus.getUserId(request);
    if (!userId.isPresent()) {
      return;
    }

    try {
      long eventId = Long.parseLong(request.getParameter(EVENT_ID_PARAMETER));
      String message = request.getParameter(MESSAGE_PARAMETER);
      datastore.addMessage(eventId, message);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }
}
