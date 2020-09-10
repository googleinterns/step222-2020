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
import com.google.lecturechat.data.constants.MessageEntity;

/** A helper class for passing message data. */
public final class Message {

  private final long id;
  private final String content;
  private final long timestamp;
  private final String author;
  private final long event;

  public Message(long id, String content, long timestamp, String author, long event) {
    this.id = id;
    this.content = content;
    this.timestamp = timestamp;
    this.author = author;
    this.event = event;
  }

  public long getId() {
    return id;
  }

  public String getContent() {
    return content;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getAuthor() {
    return author;
  }

  public long getEvent() {
    return event;
  }

  public static Message createMessageFromEntity(Entity messageEntity) {
    if (messageEntity.getKind().equals(MessageEntity.KIND.getLabel())) {
      long id = messageEntity.getKey().getId();
      String content =
          (String) (messageEntity.getProperty(MessageEntity.CONTENT_PROPERTY.getLabel()));
      long timestamp =
          (long) (messageEntity.getProperty(MessageEntity.TIMESTAMP_PROPERTY.getLabel()));
      String author =
          (String) (messageEntity.getProperty(MessageEntity.AUTHOR_PROPERTY.getLabel()));
      long event = (long) (messageEntity.getProperty(MessageEntity.EVENT_PROPERTY.getLabel()));
      return new Message(id, content, timestamp, author, event);
    } else {
      throw new IllegalArgumentException(
          "Attempted to create message object from entity that is not a message.");
    }
  }

  @Override
  public boolean equals(Object anotherObject) {
    if (!(anotherObject instanceof Message)) {
      return false;
    }
    return (this.getId() == ((Message) anotherObject).getId());
  }
}
