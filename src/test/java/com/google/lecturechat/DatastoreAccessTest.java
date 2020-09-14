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

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DatastoreAccessTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreAccess datastore;
  private DatastoreService service;

  // Parameters for arranging.
  private final String UNIVERSITY_A = "Uni A";
  private final String UNIVERSITY_B = "Uni B";
  private final String UNIVERSITY_C = "Uni C";
  private final String DEGREE = "Degree A";
  private final int YEAR = 1;
  private final String EVENT_TITLE_A = "Event A";
  private final String EVENT_TITLE_B = "Event B";
  private final String EVENT_TITLE_C = "Event C";
  private final String EVENT_CREATOR = "Creator A";
  private final long START_TIME = 0;
  private final long END_TIME = 0;
  private final String USER_ID = "User Id A";
  private final String USER_NAME = "User Name A";
  private final String MESSAGE_CONTENT = "Message A";

  // Constants since we don't have access to the constants files here.
  private final String groupEntityLabel = "Group";
  private final String eventEntityLabel = "Event";
  private final String userEntityLabel = "User";
  private final String messageEntityLabel = "Message";

  @Before
  public void setUp() {
    helper.setUp();
    service = DatastoreServiceFactory.getDatastoreService();
    datastore = DatastoreAccess.getDatastoreAccess();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void addGroupAddsExactlyOneGroup() {
    datastore.addGroup(UNIVERSITY_A, DEGREE, YEAR);

    assertEquals(1, service.prepare(new Query(groupEntityLabel)).countEntities());
  }

  @Test
  public void addGroupDoesntAddAlreadyExistingGroup() {
    datastore.addGroup(UNIVERSITY_A, DEGREE, YEAR);

    datastore.addGroup(UNIVERSITY_A, DEGREE, YEAR);

    assertEquals(1, service.prepare(new Query(groupEntityLabel)).countEntities());
  }

  @Test
  public void getAllGroupsReturnsEmptyListIfNoGroupsInDatastore() {
    List<Group> groups = datastore.getAllGroups();

    assertEquals(0, groups.size());
  }

  @Test
  public void getAllGroupsReturnsCorrectNumberOfGroups() {
    datastore.addGroup(UNIVERSITY_A, DEGREE, YEAR);
    datastore.addGroup(UNIVERSITY_B, DEGREE, YEAR);

    List<Group> groups = datastore.getAllGroups();

    assertEquals(2, groups.size());
  }

  @Test
  public void addEventAddsExactlyOneEventToCorrectGroup() {
    long groupId = datastore.addGroup(UNIVERSITY_A, DEGREE, YEAR);

    datastore.addEventToGroup(groupId, EVENT_TITLE_A, START_TIME, END_TIME, EVENT_CREATOR);
    List<Event> events = datastore.getAllEventsFromGroup(groupId);

    assertEquals(1, service.prepare(new Query(eventEntityLabel)).countEntities());
    assertEquals(1, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void addEventToNonexistingGroupThrowsException() {
    long groupId = 123L;

    datastore.addEventToGroup(groupId, EVENT_TITLE_A, START_TIME, END_TIME, EVENT_CREATOR);
  }

  @Test
  public void addUserAddsExactlyOneUser() {
    datastore.addUser(USER_ID, USER_NAME);

    assertEquals(1, service.prepare(new Query(userEntityLabel)).countEntities());
  }

  @Test
  public void addUserDoesntAddExistingUser() {
    datastore.addUser(USER_ID, USER_NAME);

    datastore.addUser(USER_ID, USER_NAME);

    assertEquals(1, service.prepare(new Query(userEntityLabel)).countEntities());
  }

  @Test
  public void getCorrectNumberOfJoinedAndNotJoinedGroups() {
    long groupA = datastore.addGroup(UNIVERSITY_A, DEGREE, YEAR);
    long groupB = datastore.addGroup(UNIVERSITY_B, DEGREE, YEAR);
    long groupC = datastore.addGroup(UNIVERSITY_C, DEGREE, YEAR);
    datastore.addUser(USER_ID, USER_NAME);
    datastore.joinGroup(USER_ID, groupA);
    datastore.joinGroup(USER_ID, groupB);

    List<Group> joined = datastore.getJoinedGroups(USER_ID);
    List<Group> notJoined = datastore.getNotJoinedGroups(USER_ID);

    assertEquals(2, joined.size());
    assertEquals(1, notJoined.size());
  }

  @Test
  public void getCorrectNumberOfJoinedAndNotJoinedEvents() {
    long groupId = datastore.addGroup(UNIVERSITY_A, DEGREE, YEAR);
    long eventA =
        datastore.addEventToGroup(groupId, EVENT_TITLE_A, START_TIME, END_TIME, EVENT_CREATOR);
    long eventB =
        datastore.addEventToGroup(groupId, EVENT_TITLE_B, START_TIME, END_TIME, EVENT_CREATOR);
    long eventC =
        datastore.addEventToGroup(groupId, EVENT_TITLE_C, START_TIME, END_TIME, EVENT_CREATOR);
    datastore.addUser(USER_ID, USER_NAME);
    datastore.joinGroup(USER_ID, groupId);
    datastore.joinEvent(USER_ID, eventA);
    datastore.joinEvent(USER_ID, eventB);

    List<Event> joined = datastore.getAllJoinedEventsFromGroup(groupId, USER_ID);
    List<Event> notJoined = datastore.getAllNotJoinedEventsFromGroup(groupId, USER_ID);

    assertEquals(2, joined.size());
    assertEquals(1, notJoined.size());
  }

  @Test
  public void addAndRetrieveCorrectNumberOfMessages() {
    long eventId = 123L;
    datastore.addMessage(eventId, MESSAGE_CONTENT, USER_NAME);
    datastore.addMessage(eventId, MESSAGE_CONTENT, USER_NAME);
    datastore.addMessage(eventId, MESSAGE_CONTENT, USER_NAME);

    List<Message> messages = datastore.getMessagesFromEvent(eventId, 20);

    assertEquals(3, service.prepare(new Query(messageEntityLabel)).countEntities());
    assertEquals(3, messages.size());
  }

  @Test
  public void addAndRetrieveCorrectNumberOfMessagesWithLimit() {
    long eventId = 123L;
    datastore.addMessage(eventId, MESSAGE_CONTENT, USER_NAME);
    datastore.addMessage(eventId, MESSAGE_CONTENT, USER_NAME);

    List<Message> messages = datastore.getMessagesFromEvent(eventId, 1);

    assertEquals(1, messages.size());
  }
}
