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

package com.google.lecturechat.data;

/** 
 * A helper class for sharing the id of the currently logged in user between
 * different servlets.
 */
public class AuthStatus {
  private String userId;
  private static AuthStatus instance;

  private AuthStatus() {}

  public static AuthStatus getInstance() {
    if (instance == null) {
      instance = new AuthStatus();
    }
    return instance;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public boolean isLoggedIn() {
    return (userId != null);
  }
}