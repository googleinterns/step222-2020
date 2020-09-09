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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.lecturechat.data.constants.EventEntity;
import com.google.lecturechat.data.constants.GroupEntity;
import com.google.lecturechat.data.constants.UserEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/** API class for methods that access and operate on the datastore database. */
public class DatastoreAccess {

  private final DatastoreService datastore;

  private DatastoreAccess(DatastoreService datastore) {
    this.datastore = datastore;
  }

  /** Factory constructor. */
  public static DatastoreAccess getDatastoreAccess() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    return new DatastoreAccess(datastore);
  }

  /**
   * Adds new group entity to the database if it doesn't already exist (atomic).
   *
   * @param university The name of the unversity the new group is associated with.
   * @param degree The name of the degree the new group is associated with.
   * @param year The year of the degree the new group is associated with.
   * @return The id associated with the group.
   */
  public long addGroup(String university, String degree, int year) {
    Transaction transaction = datastore.beginTransaction();
    try {
      Optional<Entity> groupEntity = getExistingGroupEntity(university, degree, year);
      if (!groupEntity.isPresent()) {
        Entity newGroupEntity = new Entity(GroupEntity.KIND.getLabel());
        newGroupEntity.setProperty(GroupEntity.UNIVERSITY_PROPERTY.getLabel(), university);
        newGroupEntity.setProperty(GroupEntity.DEGREE_PROPERTY.getLabel(), degree);
        newGroupEntity.setProperty(GroupEntity.YEAR_PROPERTY.getLabel(), year);
        newGroupEntity.setProperty(GroupEntity.STUDENTS_PROPERTY.getLabel(), new ArrayList<Long>());
        newGroupEntity.setProperty(GroupEntity.EVENTS_PROPERTY.getLabel(), new ArrayList<Long>());
        datastore.put(newGroupEntity);
        groupEntity = Optional.of(newGroupEntity);
      }
      transaction.commit();
      return groupEntity.get().getKey().getId();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Queries the database to check if a group with the given parameters already exists.
   *
   * @param university The name of the university the new group is associated with.
   * @param degree The name of the degree the new group is associated with.
   * @param year The year of the degree the new group is associated with.
   * @return The entity associated with the group if it exists or null otherwise.
   */
  private Optional<Entity> getExistingGroupEntity(String university, String degree, int year) {
    Query query = new Query(GroupEntity.KIND.getLabel());
    query.setFilter(
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate(
                    GroupEntity.UNIVERSITY_PROPERTY.getLabel(), FilterOperator.EQUAL, university),
                new FilterPredicate(
                    GroupEntity.DEGREE_PROPERTY.getLabel(), FilterOperator.EQUAL, degree),
                new FilterPredicate(
                    GroupEntity.YEAR_PROPERTY.getLabel(), FilterOperator.EQUAL, year))));
    Entity groupEntity = datastore.prepare(query).asSingleEntity();
    return ((groupEntity != null) ? Optional.of(groupEntity) : Optional.empty());
  }

  /**
   * Queries the database to get a list of all groups.
   *
   * @return The list of groups.
   */
  public List<Group> getAllGroups() {
    Query query = new Query(GroupEntity.KIND.getLabel());
    PreparedQuery results = datastore.prepare(query);
    List<Group> groups = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      groups.add(Group.createGroupFromEntity(entity));
    }
    return groups;
  }

  /**
   * Queries the database to get an entity by its ID.
   *
   * @param kind The kind of the entity.
   * @param id The id of the entity.
   * @return The entity.
   * @throws IllegalArgumentException If the entity can't be found in the database.
   */
  private Entity getEntityById(String kind, long id) {
    Key key = KeyFactory.createKey(kind, id);
    try {
      return datastore.get(key);
    } catch (EntityNotFoundException e) {
      throw new IllegalArgumentException(
          "Couldn't find entity with id " + id + " and kind " + kind + ".");
    }
  }

  /**
   * Queries the database to get an entity by its ID string.
   *
   * @param kind The kind of the entity.
   * @param id The id of the entity (as a string).
   * @return The entity.
   */
  private Optional<Entity> getEntityByIdString(String kind, String id) {
    try {
      return Optional.of(datastore.get(KeyFactory.createKey(kind, id)));
    } catch (EntityNotFoundException e) {
      return Optional.empty();
    }
  }

  /**
   * Queries the database to check if the user is already registered or not.
   *
   * @param userId The id of the user to be checked.
   * @return True if the user is already registered, false otherwise.
   */
  public boolean isUserRegistered(String userId) {
    return getEntityByIdString(UserEntity.KIND.getLabel(), userId).isPresent();
  }

  /**
   * Adds new event entity to a specific group in the database (atomic).
   *
   * @param groupId The id of the group the new event belongs to.
   * @param title The title of the new event.
   * @param startTime The start time of the event (number of milliseconds since epoch time).
   * @param endTime The end time of the event (number of milliseconds since epoch time).
   * @param creator The creator of the event.
   * @return The id of the event created or 0 if the event couldn't be created.
   */
  public long addEventToGroup(
      long groupId, String title, long startTime, long endTime, String creator) {
    // Create cross-group transaction to make operations on both entity types atomic.
    Transaction transaction = datastore.beginTransaction(TransactionOptions.Builder.withXG(true));
    long eventId = 0;

    try {
      Entity eventEntity = new Entity(EventEntity.KIND.getLabel());
      eventEntity.setProperty(EventEntity.TITLE_PROPERTY.getLabel(), title);
      eventEntity.setProperty(EventEntity.START_PROPERTY.getLabel(), startTime);
      eventEntity.setProperty(EventEntity.END_PROPERTY.getLabel(), endTime);
      eventEntity.setProperty(EventEntity.CREATOR_PROPERTY.getLabel(), creator);
      eventEntity.setProperty(EventEntity.MESSAGES_PROPERTY.getLabel(), new ArrayList<Long>());
      eventEntity.setProperty(EventEntity.ATTENDEES_PROPERTY.getLabel(), new ArrayList<Long>());
      eventId = datastore.put(transaction, eventEntity).getId();

      if (eventId != 0) {
        Entity groupEntity = getEntityById(GroupEntity.KIND.getLabel(), groupId);
        List<Long> eventIds =
            (ArrayList) (groupEntity.getProperty(GroupEntity.EVENTS_PROPERTY.getLabel()));
        if (eventIds == null) {
          eventIds = new ArrayList<>();
        }
        eventIds.add(eventId);
        groupEntity.setProperty(GroupEntity.EVENTS_PROPERTY.getLabel(), eventIds);
        datastore.put(transaction, groupEntity);
        transaction.commit();
      }

    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
    return eventId;
  }

  /**
   * Queries the database to get a list of all events in a certain group.
   *
   * @param groupId The id of the group.
   * @return The list of events.
   */
  List<Event> getAllEventsFromGroup(long groupId) {
    Entity groupEntity = getEntityById(GroupEntity.KIND.getLabel(), groupId);
    List<Long> eventIds =
        (ArrayList) (groupEntity.getProperty(GroupEntity.EVENTS_PROPERTY.getLabel()));
    List<Event> events = new ArrayList<>();
    if (eventIds != null) {
      events =
          eventIds.stream()
              .map(
                  id -> Event.createEventFromEntity(getEntityById(EventEntity.KIND.getLabel(), id)))
              .collect(Collectors.toList());
    }
    return events;
  }

  /**
   * Adds the user to the database if they don't exist already.
   *
   * @param userId The id of the user that will be added.
   * @param name The name of the user that will be added.
   */
  public void addUser(String userId, String name) {
    if (isUserRegistered(userId)) {
      return;
    }

    Entity userEntity = new Entity(KeyFactory.createKey(UserEntity.KIND.getLabel(), userId));
    userEntity.setProperty(UserEntity.NAME_PROPERTY.getLabel(), name);
    userEntity.setProperty(UserEntity.GROUPS_PROPERTY.getLabel(), new ArrayList<Long>());
    userEntity.setProperty(UserEntity.EVENTS_PROPERTY.getLabel(), new ArrayList<Long>());
    datastore.put(userEntity);
  }

  /**
   * Joins the given entity by adding the entity id to the user's list of entities (The entities are
   * defined by a label). Examples of entities: groups, events.
   *
   * @param userId The id of the user that joins the entity.
   * @param entityId The id of the entity that the user joined.
   * @param entityLabel The label associated with this entity.
   */
  private void joinEntity(String userId, long entityId, String entityLabel) {
    Optional<Entity> user = getEntityByIdString(UserEntity.KIND.getLabel(), userId);
    if (!user.isPresent()) {
      return;
    }

    Transaction transaction = datastore.beginTransaction();
    try {
      List<Long> entitiesIds = (ArrayList) (user.get().getProperty(entityLabel));
      if (entitiesIds == null) {
        entitiesIds = new ArrayList<>();
      }
      if (!entitiesIds.contains(entityId)) {
        entitiesIds.add(entityId);
      }
      user.get().setProperty(entityLabel, entitiesIds);
      datastore.put(user.get());
      transaction.commit();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Joins the given group by adding the group id to the user's list of groups.
   *
   * @param userId The id of the user that joins the group.
   * @param groupId The id of the group that the user joined.
   */
  public void joinGroup(String userId, long groupId) {
    joinEntity(userId, groupId, UserEntity.GROUPS_PROPERTY.getLabel());
  }

  /**
   * Joins the given event by adding the event id to the user's list of events.
   *
   * @param userId The id of the user that joins the event.
   * @param eventId The id of the event that the user joined.
   */
  public void joinEvent(String userId, long eventId) {
    joinEntity(userId, eventId, UserEntity.EVENTS_PROPERTY.getLabel());
  }

  /**
   * Gets the groups joined by the user.
   *
   * @param userId The id of the user.
   * @return The list of the groups joined.
   */
  public List<Group> getJoinedGroups(String userId) {
    Optional<Entity> user = getEntityByIdString(UserEntity.KIND.getLabel(), userId);
    if (!user.isPresent()) {
      return new ArrayList<>();
    }

    List<Long> groupsIds =
        (ArrayList) (user.get().getProperty(UserEntity.GROUPS_PROPERTY.getLabel()));
    if (groupsIds == null) {
      return new ArrayList<>();
    }
    return groupsIds.stream()
        .map(
            groupId ->
                Group.createGroupFromEntity(getEntityById(GroupEntity.KIND.getLabel(), groupId)))
        .collect(Collectors.toList());
  }

  /**
   * Gets the events joined by the user.
   *
   * @param userId The id of the user.
   * @return The list of the events joined.
   */
  public List<Event> getJoinedEvents(String userId) {
    Optional<Entity> user = getEntityByIdString(UserEntity.KIND.getLabel(), userId);
    if (!user.isPresent()) {
      return new ArrayList<>();
    }

    List<Long> eventsIds =
        (ArrayList) (user.get().getProperty(UserEntity.EVENTS_PROPERTY.getLabel()));
    if (eventsIds == null) {
      return new ArrayList<>();
    }
    return eventsIds.stream()
        .map(
            eventId ->
                Event.createEventFromEntity(getEntityById(EventEntity.KIND.getLabel(), eventId)))
        .collect(Collectors.toList());
  }

  /**
   * Gets only the groups that the user isn't part of already.
   *
   * @param userId The id of the user.
   * @return The list of groups that the user didn't join yet.
   */
  public List<Group> getNotJoinedGroups(String userId) {
    List<Group> groups = getAllGroups();
    groups.removeAll(getJoinedGroups(userId));
    return groups;
  }

  /**
   * Gets all the events in a certain group that the user had joined already.
   *
   * @param groupId The id of the group.
   * @param userId The id of the user.
   * @return The list of events that the user had joined already.
   */
  public List<Event> getAllJoinedEventsFromGroup(long groupId, String userId) {
    List<Event> events = getAllEventsFromGroup(groupId);
    events.retainAll(getJoinedEvents(userId));
    return events;
  }

  /**
   * Gets all the events in a certain group that the user didn't join yet.
   *
   * @param groupId The id of the group.
   * @param userId The id of the user.
   * @return The list of events that the user didn't join yet.
   */
  public List<Event> getAllNotJoinedEventsFromGroup(long groupId, String userId) {
    List<Event> events = getAllEventsFromGroup(groupId);
    events.removeAll(getJoinedEvents(userId));
    return events;
  }

  /**
   * Gets all the events joined by the user whose start date is in the interval [beginningDate,
   * endingDate).
   *
   * @param beginningDate The inclusive lower bound value of the interval used to filter the events
   *     by their start date.
   * @param endingDate The exclusive upper bound value of the interval used to filter the events by
   *     their start date.
   * @param userId The id of the user.
   * @return A list of the events joined by the user whose start date is in the interval
   *     [beginningDate, endingDate).
   */
  public List<Event> getJoinedEventsThatStartBetweenDates(
      long beginningDate, long endingDate, String userId) {
    List<Event> joinedEvents = getJoinedEvents(userId);
    Query query = new Query(EventEntity.KIND.getLabel());
    query.setFilter(
        new CompositeFilter(
            CompositeFilterOperator.AND,
            Arrays.asList(
                new FilterPredicate(
                    EventEntity.START_PROPERTY.getLabel(),
                    FilterOperator.GREATER_THAN_OR_EQUAL,
                    beginningDate),
                new FilterPredicate(
                    EventEntity.START_PROPERTY.getLabel(), FilterOperator.LESS_THAN, endingDate))));
    return StreamSupport.stream(datastore.prepare(query).asIterable().spliterator(), false)
        .map(entity -> Event.createEventFromEntity(entity))
        .filter(event -> joinedEvents.contains(event))
        .collect(Collectors.toList());
  }

  /**
   * Gets all the messages in a certain group.
   *
   * @param eventId The id of the group.
   * @return The list of messages.
   */
  public List<String> getMessagesFromEvent(long eventId) {
    Entity eventEntity = getEntityById(EventEntity.KIND.getLabel(), eventId);
    List<String> messages =
        (ArrayList) (eventEntity.getProperty(EventEntity.MESSAGES_PROPERTY.getLabel()));
    if (messages == null) {
      return new ArrayList<String>();
    }
    return messages;
  }

  /**
   * Retrieve all messages from a certain event.
   *
   * @param eventId The id of the event associated with the message.
   * @param message The message that will be added.
   */
  public void addMessage(long eventId, String message) {
    Transaction transaction = datastore.beginTransaction();
    try {
      Entity eventEntity = getEntityById(EventEntity.KIND.getLabel(), eventId);
      List<String> messages =
          (ArrayList) (eventEntity.getProperty(EventEntity.MESSAGES_PROPERTY.getLabel()));
      if (messages == null) {
        messages = new ArrayList<>();
      }
      messages.add(message);
      eventEntity.setProperty(EventEntity.MESSAGES_PROPERTY.getLabel(), messages);
      datastore.put(eventEntity);
      transaction.commit();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }
}
