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
import com.google.lecturechat.data.Group;
import com.google.lecturechat.data.AuthStatus;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

/** Servlet for listing all available groups and add a new group. */
@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {

  private static final String UNIVERSITY_PARAMETER = "university";
  private static final String DEGREE_PARAMETER = "degree";
  private static final String YEAR_PARAMETER = "year";
  private static DatastoreAccess datastore;
  private static final AuthStatus authStatus = AuthStatus.getInstance();

  @Override
  public void init() {
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Group> groups = datastore.getAllGroups();
    response.setContentType("application/json;");
    response.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(groups));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!authStatus.isSignedIn()) {
      return;
    }
    try {
      String university = request.getParameter(UNIVERSITY_PARAMETER);
      String degree = request.getParameter(DEGREE_PARAMETER);
      int year = Integer.parseInt(request.getParameter(YEAR_PARAMETER));

      datastore.addGroup(university, degree, year);
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }
  }
}
