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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
   * Adds new group entity to the database if it doesn't already exist.
   *
   * @param university The name of the unversity the new group is associated with.
   * @param degree The name of the degree the new group is associated with.
   * @param year The year of the degree the new group is associated with.
   */
  public void addGroup(String university, String degree, int year) {
    Transaction txn = datastore.beginTransaction();
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
      txn.commit();
    } finally {
      if (txn.isActive()) {
        txn.rollback();
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
   * Adds new event entity to a specific group in the database (atomic transaction).
   *
   * @param groupId The id of the group the new event belongs to.
   * @param title The title of the new event.
   * @param start The start time of the event (should be in UTC).
   * @param end The end time of the event (should be in UTC).
   * @param creator The creator of the event.
   */
  public void addEventToGroup(
      long groupId, String title, String start, String end, String creator) {
    Transaction eventTxn = datastore.beginTransaction();
    long eventId = 0;
    try {
      Entity eventEntity = new Entity(EventEntity.KIND.getLabel());
      eventEntity.setProperty(EventEntity.TITLE_PROPERTY.getLabel(), title);
      eventEntity.setProperty(EventEntity.START_PROPERTY.getLabel(), start);
      eventEntity.setProperty(EventEntity.END_PROPERTY.getLabel(), end);
      eventEntity.setProperty(EventEntity.CREATOR_PROPERTY.getLabel(), creator);
      eventEntity.setProperty(EventEntity.MESSAGES_PROPERTY.getLabel(), new ArrayList<Long>());
      eventEntity.setProperty(EventEntity.ATTENDEES_PROPERTY.getLabel(), new ArrayList<Long>());
      eventId = datastore.put(eventEntity).getId();
      eventTxn.commit();
    } finally {
      if (eventTxn.isActive()) {
        eventTxn.rollback();
      }
    }
    if (eventId != 0) {
      Transaction groupTxn = datastore.beginTransaction();
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
        groupTxn.commit();
      } finally {
        if (groupTxn.isActive()) {
          groupTxn.rollback();
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
      for (long eventId: eventIds) {
        Event event =
            Event.createEventFromEntity(getEntityById(EventEntity.KIND.getLabel(), eventId));
        events.add(event);
      }
    }
    return events;
  }
}
