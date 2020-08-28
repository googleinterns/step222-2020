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

import com.google.appengine.api.datastore.Entity;
import com.google.lecturechat.data.constants.EventEntity;
import java.util.ArrayList;
import java.util.List;

/** A helper class for passing event data. */
public final class Event {

  private final long id;
  private final String title;

  // The number of milliseconds since epoch time (1 Jan 1970 UTC).
  private final long startTime;
  private final long endTime;

  private final String creator;
  private final List<Long> messages;
  private final List<Long> attendees;

  public Event(
      long id,
      String title,
      long startTime,
      long endTime,
      String creator,
      List<Long> messages,
      List<Long> attendees) {
    this.id = id;
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
    this.creator = creator;
    this.messages = messages;
    this.attendees = attendees;
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public long getStart() {
    return startTime;
  }

  public long getEnd() {
    return endTime;
  }

  public String getCreator() {
    return creator;
  }

  public List<Long> getMessages() {
    return messages;
  }

  public List<Long> getAttendees() {
    return attendees;
  }

  public static Event createEventFromEntity(Entity eventEntity) {
    if (eventEntity.getKind().equals(EventEntity.KIND.getLabel())) {
      long id = eventEntity.getKey().getId();
      String title = (String) (eventEntity.getProperty(EventEntity.TITLE_PROPERTY.getLabel()));
      long start = (long) (eventEntity.getProperty(EventEntity.START_PROPERTY.getLabel()));
      long end = (long) (eventEntity.getProperty(EventEntity.END_PROPERTY.getLabel()));
      String creator = (String) (eventEntity.getProperty(EventEntity.CREATOR_PROPERTY.getLabel()));
      List<Long> messages =
          (ArrayList) (eventEntity.getProperty(EventEntity.MESSAGES_PROPERTY.getLabel()));
      List<Long> attendees =
          (ArrayList) (eventEntity.getProperty(EventEntity.ATTENDEES_PROPERTY.getLabel()));
      return new Event(id, title, start, end, creator, messages, attendees);
    } else {
      throw new IllegalArgumentException(
          "Attempted to create event object from entity that is not an event.");
    }
  }
}
