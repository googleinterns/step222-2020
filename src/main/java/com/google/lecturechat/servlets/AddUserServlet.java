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
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for adding a new user to the database (if they are not already registered). */
@WebServlet("/add-user")
public class AddUserServlet extends HttpServlet {

  private static DatastoreAccess datastore;

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  public void addUserFromPayload(Payload userPayload) {
    String userId = userPayload.getSubject();
    String name = (String) userPayload.get("name");
    datastore.addUser(userId, name);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<Payload> userPayload = AuthStatus.getUserPayload(request);

    if (!userPayload.isPresent()) {
      response.sendRedirect(request.getContextPath() + "/index.html");
      return;
    }

    addUserFromPayload(userPayload.get());
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<Payload> userPayload = AuthStatus.getUserPayload(request);

    if (userPayload.isPresent()) {
      addUserFromPayload(userPayload.get());
    }

    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(userPayload.isPresent()));
  }
}
