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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

/** A helper class used to retrieve specific data such as the id_token from a request. */
public class AuthStatus {
  private static final String CLIENT_ID = "";
  private static final GoogleIdTokenVerifier verifier =
      new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
          .setAudience(Collections.singletonList(CLIENT_ID))
          .build();

  /**
   * Gets the id_token from the associated cookie if it is included in the request.
   *
   * @param request The request from which we will extract the cookie.
   * @return An Optional object that contains the id_token if it was found in the request.
   */
  private static Optional<GoogleIdToken> getIdToken(HttpServletRequest request) throws IOException {
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("id_token")) {
          try {
            String idTokenString =
                java.net.URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.name());
            return Optional.ofNullable(verifier.verify(idTokenString));
          } catch (GeneralSecurityException e) {
            throw new BadRequestException("Invalid id_token");
          } catch (UnsupportedEncodingException e) {
            throw new BadRequestException(e.getMessage());
          }
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Gets the payload containing information about the user (if the id_token is included in the
   * cookie associated and it is valid).
   *
   * @param request The request from which we will extract the cookie.
   * @return An Optional object that contains the payload if the id_token was included in the
   *     request and it was valid.
   */
  public static Optional<Payload> getUserPayload(HttpServletRequest request) throws IOException {
    return getIdToken(request).map(GoogleIdToken::getPayload);
  }

  /**
   * Gets user id from the payload (if the request included a valid id_token).
   *
   * @param request The request from which we will extract the id_token.
   * @return An Optional object that contains the user id if the request included a valid id_token.
   */
  public static Optional<String> getUserId(HttpServletRequest request) throws IOException {
    return getUserPayload(request).map(Payload::getSubject);
  }

  /**
   * Checks if the user is signed by inspecting the cookies included in the request.
   *
   * @param request The request from which we will extract the cookies.
   * @return True if the user is signed in, false otherwise.
   */
  public static boolean isSignedIn(HttpServletRequest request) throws IOException {
    return getIdToken(request).isPresent();
  }
}
