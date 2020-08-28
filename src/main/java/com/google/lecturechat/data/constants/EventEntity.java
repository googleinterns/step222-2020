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

package com.google.lecturechat.data.constants;

/** Specifies the kind and property names to use for event entities in the datastore database. */
public enum EventEntity {
  KIND("Event"),
  TITLE_PROPERTY("title"),
  START_PROPERTY("start"),
  END_PROPERTY("end"),
  CREATOR_PROPERTY("creator"),
  MESSAGES_PROPERTY("messages"),
  ATTENDEES_PROPERTY("attendees");

  /* Labels comments and properties of events in the database. */
  private final String label;

  private EventEntity(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
