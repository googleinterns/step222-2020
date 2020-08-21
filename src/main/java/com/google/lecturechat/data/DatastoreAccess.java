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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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
   * @param university The name of the unversity the new group is associated with.
   * @param degree The name of the degree the new group is associated with.
   * @param year The year of the degree the new group is associated with.
   */
  public void addGroup(String university, String degree, int year) {
    if (!groupExistsAlready(university, degree, year)) {
      Entity groupEntity = new Entity(GroupEntity.KIND.getLabel());
      groupEntity.setProperty(GroupEntity.UNIVERSITY_PROPERTY.getLabel(), university);
      groupEntity.setProperty(GroupEntity.DEGREE_PROPERTY.getLabel(), degree);
      groupEntity.setProperty(GroupEntity.YEAR_PROPERTY.getLabel(), year);
      groupEntity.setProperty(GroupEntity.STUDENTS_PROPERTY.getLabel(), new ArrayList<Long>());
      groupEntity.setProperty(GroupEntity.EVENTS_PROPERTY.getLabel(), new ArrayList<Long>());
      datastore.put(groupEntity);
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
}
