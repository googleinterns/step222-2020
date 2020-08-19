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
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.lecturechat.data.constants.EventEntity;
import com.google.lecturechat.data.constants.GroupEntity;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * API class for methods that access and operate on the datastore database.
 */
public class DatastoreAccess {

  private final DatastoreService datastore;

  private DatastoreAccess(DatastoreService datastore) {
    this.datastore = datastore;
  }

  /**
  * Factory constructor.
  */
  public static DatastoreAccess getDatastoreAccess() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    return new DatastoreAccess(datastore);
  }

  /**
   * Adds new group entity to the database if it doesn't already exist (atomic).
   * @param university The name of the unversity the new group is associated with.
   * @param degree The name of the degree the new group is associated with.
   * @param year The year of the degree the new group is associated with.
   */
  public void addGroup(String university, String degree, int year) {
    Transaction txn = datastore.beginTransaction();
    try {
      if(!groupExistsAlready(university, degree, year)) {
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

  private Entity getEntityById(String kind, long id) {
    Key key = KeyFactory.createKey(kind, id);
    try {
      return datastore.get(key);
    } catch (EntityNotFoundException e) {
      throw new IllegalArgumentException("Couldn't find entity with id " + id + " and kind " + kind + ".");
    }
  }

  public void addEventToGroup(long groupId, String title, Calendar startTime, int duration) {
    Entity eventEntity = new Entity(EventEntity.KIND.getLabel());
    eventEntity.setProperty(EventEntity.TITLE_PROPERTY.getLabel(), title);
    long eventId = datastore.put(eventEntity).getId();

    Entity groupEntity = getEntityById(GroupEntity.KIND.getLabel(), groupId);
    List<Long> eventIds = (ArrayList) (groupEntity.getProperty(GroupEntity.EVENTS_PROPERTY.getLabel()));
    if(eventIds == null) {
      eventIds = new ArrayList<>();
    }
    eventIds.add(eventId);

    groupEntity.setProperty(GroupEntity.EVENTS_PROPERTY.getLabel(), eventIds);
    datastore.put(groupEntity);
  }

  public List<Entity> getAllEventsFromGroup(long groupId) {
    Entity groupEntity = getEntityById(GroupEntity.KIND.getLabel(), groupId);
    List<Long> eventIds = (ArrayList) (groupEntity.getProperty(GroupEntity.EVENTS_PROPERTY.getLabel()));
    List<Entity> events = new ArrayList<>();
    if(eventIds != null) {
      for (long eventId: eventIds) {
        events.add(getEntityById(EventEntity.KIND.getLabel(), eventId));
      }
    }
    return events;
  }
}
