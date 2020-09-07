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

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import java.io.IOException;

/** A helper class used to access and retrieve the secrets (e.g. the OAuth 2.0 client ID).*/
public class AccessSecrets {
  private static final String projectId = "lecturechat";
  private static final String secretId = "client-id";
  private static final String versionId = "1";

  /**
   * Returns the OAuth 2.0 client ID if it was included in the Secret Manager.
   *
   * @return The client ID if the operations are successfull.
   * @throws IOException If the SecretManagerServiceClient object couldn't been created.
   * @throws ApiException If the secret version associated with client ID couldn't have been
   *     accessed.
   */
  public static String getClientId() throws IOException, ApiException {
    SecretManagerServiceClient client = SecretManagerServiceClient.create();
    String response =
        client
            .accessSecretVersion(SecretVersionName.of(projectId, secretId, versionId))
            .getPayload()
            .getData()
            .toStringUtf8();
    client.close();
    return response;
  }
}
