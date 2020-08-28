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
import com.google.lecturechat.data.constants.EventEntity;
import com.google.lecturechat.data.constants.GroupEntity;
import com.google.lecturechat.data.constants.UserEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
   */
  public void addGroup(String university, String degree, int year) {
    Transaction transaction = datastore.beginTransaction();
    try {
      if (!groupExistsAlready(university, degree, year)) {
        Entity groupEntity = new Entity(GroupEntity.KIND.getLabel());
        groupEntity.setProperty(GroupEntity.UNIVERSITY_PROPERTY.getLabel(), university);
        groupEntity.setProperty(GroupEntity.DEGREE_PROPERTY.getLabel(), degree);
        groupEntity.setProperty(GroupEntity.YEAR_PROPERTY.getLabel(), year);
        groupEntity.setProperty(GroupEntity.STUDENTS_PROPERTY.getLabel(), new ArrayList<Long>());
        groupEntity.setProperty(GroupEntity.EVENTS_PROPERTY.getLabel(), new ArrayList<Long>());
        datastore.put(groupEntity);
      }
      transaction.commit();
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
   * @return True if it exists, else false.
   */
  private boolean groupExistsAlready(String university, String degree, int year) {
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
    return (groupEntity != null);
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
   */
  public void addEventToGroup(
      long groupId, String title, long startTime, long endTime, String creator) {
    Transaction eventTransaction = datastore.beginTransaction();
    long eventId = 0;
    try {
      Entity eventEntity = new Entity(EventEntity.KIND.getLabel());
      eventEntity.setProperty(EventEntity.TITLE_PROPERTY.getLabel(), title);
      eventEntity.setProperty(EventEntity.START_PROPERTY.getLabel(), startTime);
      eventEntity.setProperty(EventEntity.END_PROPERTY.getLabel(), endTime);
      eventEntity.setProperty(EventEntity.CREATOR_PROPERTY.getLabel(), creator);
      eventEntity.setProperty(EventEntity.MESSAGES_PROPERTY.getLabel(), new ArrayList<Long>());
      eventEntity.setProperty(EventEntity.ATTENDEES_PROPERTY.getLabel(), new ArrayList<Long>());
      eventId = datastore.put(eventEntity).getId();
      eventTransaction.commit();
    } finally {
      if (eventTransaction.isActive()) {
        eventTransaction.rollback();
      }
    }
    if (eventId != 0) {
      Transaction groupTransaction = datastore.beginTransaction();
      try {
        Entity groupEntity = getEntityById(GroupEntity.KIND.getLabel(), groupId);
        List<Long> eventIds =
            (ArrayList) (groupEntity.getProperty(GroupEntity.EVENTS_PROPERTY.getLabel()));
        if (eventIds == null) {
          eventIds = new ArrayList<>();
        }
        eventIds.add(eventId);
        groupEntity.setProperty(GroupEntity.EVENTS_PROPERTY.getLabel(), eventIds);
        datastore.put(groupEntity);
        groupTransaction.commit();
      } finally {
        if (groupTransaction.isActive()) {
          groupTransaction.rollback();
        }
      }
    }
  }

  /**
   * Queries the database to get a list of all events in a certain group.
   *
   * @param groupId The id of the group.
   * @return The list of events.
   */
  public List<Event> getAllEventsFromGroup(long groupId) {
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
  public void joinEntity(String userId, long entityId, String entityLabel) {
    if (!isUserRegistered(userId)) {
      return;
    }

    Transaction transaction = datastore.beginTransaction();
    try {
      Entity userEntity = getEntityByIdString(UserEntity.KIND.getLabel(), userId).get();
      List<Long> entitiesIds = (ArrayList) (userEntity.getProperty(entityLabel));
      if (entitiesIds == null) {
        entitiesIds = new ArrayList<>();
      }
      // TODO: The classes will be later modified such that the groupsIds and eventsIds
      // will be stored as sets instead of lists.
      if (!entitiesIds.contains(entityId)) {
        entitiesIds.add(entityId);
      }
      userEntity.setProperty(entityLabel, entitiesIds);
      datastore.put(userEntity);
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
    if (!isUserRegistered(userId)) {
      return new ArrayList<>();
    }

    Entity userEntity = getEntityByIdString(UserEntity.KIND.getLabel(), userId).get();
    List<Long> groupsIds =
        (ArrayList) (userEntity.getProperty(UserEntity.GROUPS_PROPERTY.getLabel()));
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
    if (!isUserRegistered(userId)) {
      return new ArrayList<>();
    }

    Entity userEntity = getEntityByIdString(UserEntity.KIND.getLabel(), userId).get();
    List<Long> eventsIds =
        (ArrayList) (userEntity.getProperty(UserEntity.EVENTS_PROPERTY.getLabel()));
    if (eventsIds == null) {
      return new ArrayList<>();
    }
    return eventsIds.stream()
        .map(
            eventId ->
                Event.createEventFromEntity(getEntityById(EventEntity.KIND.getLabel(), eventId)))
        .collect(Collectors.toList());
  }
}
