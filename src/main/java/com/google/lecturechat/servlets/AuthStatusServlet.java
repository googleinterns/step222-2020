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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.lecturechat.data.AuthStatus;
import com.google.lecturechat.data.DatastoreAccess;
import com.google.lecturechat.data.Event;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Enumeration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for listing all events in a certain group. */
@WebServlet("/auth-status")
public class AuthStatusServlet extends HttpServlet {

  private static final String TYPE_PARAMETER = "type";
  private static final String AUTH_CODE_PARAMETER = "auth-code";
  private static final String CLIENT_ID = "";
  private static final String CLIENT_SECRET = "";
  private static final String REDIRECT_URI = "";
  private static DatastoreAccess datastore;
  private static final AuthStatus authStatus = AuthStatus.getInstance();

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(authStatus.isLoggedIn()));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String type = request.getParameter(TYPE_PARAMETER);

    try {
      if (type.equals("login")) {
        String authCode = request.getParameter(AUTH_CODE_PARAMETER);

        GoogleTokenResponse tokenResponse =
            new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                CLIENT_ID,
                CLIENT_SECRET,
                authCode,
                REDIRECT_URI)
                .execute();

        String accessToken = tokenResponse.getAccessToken();

        GoogleIdToken idToken = tokenResponse.parseIdToken();
        GoogleIdToken.Payload payload = idToken.getPayload();
        String userId = payload.getSubject();
        String name = (String) payload.get("name");

        datastore.addUser(userId, name);
        authStatus.setUserId(userId);
      } else if (type.equals("logout")) {
        authStatus.setUserId(null);
      }
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }
}
