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

  private final String groupEntityLabel = "Group";

  private final LocalServiceTestHelper helper = 
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreAccess datastore;
  private DatastoreService service;

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
    String university = "A";
    String degree = "B";
    int year = 1;

    datastore.addGroup(university, degree, year);

    assertEquals(1, service.prepare(new Query(groupEntityLabel)).countEntities());
  }

  @Test
  public void addGroupDoesntAddAlreadyExistingGroup() {
    String university = "A";
    String degree = "B";
    int year = 1;
    datastore.addGroup(university, degree, year);

    datastore.addGroup(university, degree, year);

    assertEquals(1, service.prepare(new Query(groupEntityLabel)).countEntities());
  }

  @Test
  public void getAllGroupsReturnsEmptyListIfNoGroupsInDatastore() {
    List<Group> groups = datastore.getAllGroups();

    assertEquals(0, groups.size());
  }

  @Test
  public void getAllGroupsReturnsCorrectNumberOfGroups() {
    String university1 = "A";
    String degree1 = "B";
    int year1 = 1;
    String university2 = "C";
    String degree2 = "D";
    int year2 = 1;
    datastore.addGroup(university1, degree1, year1);
    datastore.addGroup(university2, degree2, year2);

    List<Group> groups = datastore.getAllGroups();

    assertEquals(2, groups.size());
  }
}
