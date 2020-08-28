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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

/**
 * A helper class used to retrieve specific data such as the id_token from a request.
 */
public class AuthStatus {
  private static final String CLIENT_ID = "";

  /**
   * Gets the id_token from the associated cookie if it is included in the request.
   *
   * @param request The request from which we will extract the cookie.
   * @return An Optional object that contains the id_token if it was found in the request.
   */
  public static Optional<String> getIdToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    Optional<String> optionalIdToken = Optional.empty();
    Optional<String> optionalEncodedIdToken = Optional.empty();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if(cookie.getName().equals("id_token")) {
          String encodedIdToken = cookie.getValue();
          optionalEncodedIdToken = Optional.of(encodedIdToken);
        }
      }
    }

    if (optionalEncodedIdToken.isPresent()) {
      String encodedIdToken = optionalEncodedIdToken.get();
      try {
        String idToken = java.net.URLDecoder.decode(encodedIdToken, StandardCharsets.UTF_8.name());
        optionalIdToken = Optional.of(idToken);
      } catch (UnsupportedEncodingException e) {
        throw new BadRequestException(e.getMessage());
      }
    }

    return optionalIdToken;
  }

  /**
   * Gets the payload containing information about the user (if the id_token is included in the
   * cookie associated and it is valid).
   *
   * @param request The request from which we will extract the cookie.
   * @return An Optional object that contains the payload if the id_token was included in the
   * request and it was valid.
   */
  public static Optional<Payload> getUserPayload(HttpServletRequest request) throws IOException {
    Optional<String> optionalIdToken = AuthStatus.getIdToken(request);
    Optional<Payload> optionalUserPayload = Optional.empty();

    if (optionalIdToken.isPresent()) {
      String idTokenString = optionalIdToken.get();

      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
        JacksonFactory.getDefaultInstance())
        .setAudience(Collections.singletonList(CLIENT_ID))
        .build();

      try {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
          Payload payload = idToken.getPayload();
          optionalUserPayload = Optional.of(payload);
        }
      } catch (GeneralSecurityException e) {
        throw new BadRequestException("Invalid id_token");
      }
    }

    return optionalUserPayload;
  }

  /**
   * Gets user id from the payload (if the request included a valid id_token).
   *
   * @param request The request from which we will extract the id_token.
   * @return An Optional object that contains the user id if the request included a valid id_token.
   */
  public static Optional<String> getUserId(HttpServletRequest request) throws IOException {
    Optional<Payload> optionalUserPayload = getUserPayload(request);
    Optional<String> optionalUserId = Optional.empty();

    if (optionalUserPayload.isPresent()) {
      Payload userPayload = optionalUserPayload.get();
      String userId = userPayload.getSubject();
      optionalUserId = Optional.of(userId);
    }

    return optionalUserId;
  }

  /**
   * Checks if the user is signed by inspecting the cookies included in the request.
   *
   * @param request The request from which we will extract the cookies.
   * @return True if the user is signed in, false otherwise.
   */
  public static boolean isSignedIn(HttpServletRequest request) {
    Optional<String> optionalIdToken = AuthStatus.getIdToken(request);

    if (optionalIdToken.isPresent()) {
      return true;
    } else {
      return false;
    }
  }
}
