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
import com.google.lecturechat.data.AuthStatus;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

/** Servlet for joining a group. */
@WebServlet("/join-group")
public class JoinGroupServlet extends HttpServlet {

  private static final String GROUP_ID_PARAMETER = "groupId";
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
      datastore.joinGroup(groupId, authStatus);
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }
}
