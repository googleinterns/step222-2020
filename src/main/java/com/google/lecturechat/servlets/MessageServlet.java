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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.gson.Gson;
import com.google.lecturechat.data.AuthStatus;
import com.google.lecturechat.data.DatastoreAccess;
import com.google.lecturechat.data.Message;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

/** Servlet for adding a chat message and getting all messages associated with an event. */
@WebServlet("/messages")
public class MessageServlet extends HttpServlet {

  private static final String EVENT_ID_PARAMETER = "id";
  private static final String MESSAGE_PARAMETER = "message";
  private static final int MESSAGE_LIMIT = 20;
  private static DatastoreAccess datastore;

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!AuthStatus.isSignedIn(request)) {
      return;
    }

    try {
      long eventId = Long.parseLong(request.getParameter(EVENT_ID_PARAMETER));
      List<Message> messages = datastore.getMessagesFromEvent(eventId, MESSAGE_LIMIT);
      response.setContentType("application/json;");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(messages));
    } catch (NumberFormatException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<Payload> userPayload = AuthStatus.getUserPayload(request);
    if (!userPayload.isPresent()) {
      return;
    }

    String name = (String) userPayload.get().get("name");

    try {
      long eventId = Long.parseLong(request.getParameter(EVENT_ID_PARAMETER));
      String content = request.getParameter(MESSAGE_PARAMETER);
      datastore.addMessage(eventId, content, name);
    } catch (NumberFormatException e) {
      throw new BadRequestException(e.getMessage());
    }
  }
}
