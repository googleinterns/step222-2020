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
import com.google.lecturechat.data.constants.GroupEntity;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;

/** A helper class for passing group data. */
public final class Group {

  private final long id;
  private final String university;
  private final String degree;
  private final int year;
  private final ArrayList<Long> students;
  private final ArrayList<Long> events;

  public Group(long id, String university, String degree, int year, ArrayList<Long> students, ArrayList<Long> events) {
    this.id = id;
    this.university = university;
    this.degree = degree;
    this.year = year;
    this.students = students;
    this.events = events;
  }

  public Group(Entity groupEntity) {
    if (groupEntity.getKind().equals(GroupEntity.KIND.getLabel())) {
      this.id = groupEntity.getKey().getId();
      this.university = (String) (groupEntity.getProperty(GroupEntity.UNIVERSITY_PROPERTY.getLabel()));
      this.degree = (String) (groupEntity.getProperty(GroupEntity.DEGREE_PROPERTY.getLabel()));
      Long longYear = (Long) (groupEntity.getProperty(GroupEntity.YEAR_PROPERTY.getLabel()));
      this.year = longYear.intValue();
      this.students = (ArrayList) (groupEntity.getProperty(GroupEntity.STUDENTS_PROPERTY.getLabel()));
      this.events = (ArrayList) (groupEntity.getProperty(GroupEntity.EVENTS_PROPERTY.getLabel()));
    } else {
      throw new IllegalArgumentException("Attempted to create group object from entity that is not a group.");
    }
  }
}
