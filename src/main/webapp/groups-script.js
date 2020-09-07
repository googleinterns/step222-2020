/* eslint-disable no-unused-vars */

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

import {createElement} from './script.js';
import {createEventElement, Event} from './events-script.js';

/** Class used to define the basic characteristics of a group. */
class Group {
  /**
   * Creates a new group with the given parameters.
   * @param {long} id The group id used by the database.
   * @param {String} university The name of the university.
   * @param {String} degree The name of the degree.
   * @param {int} year The year of study.
   */
  constructor(id, university, degree, year) {
    /** @private @const {long} */
    this.id_ = id;
    /** @private @const {String} */
    this.university_ = university;
    /** @private @const {String} */
    this.degree_ = degree;
    /** @private @const {int} */
    this.year_ = year;
  }
}

/**
 * Adds the group options available based on the user membership (if the user
 * is part of the group or not).
 * @param {Object} group The object containing the data associated with that
 * group.
 * @param {Element} groupElement The element associated with this group.
 * @param {Boolean} isMember Indicates whether or not the user is a member of
 * the group.
 */
function addGroupOptions(group, groupElement, isMember) {
  const groupOptionsElement = createElement('div', 'group-options', '');

  if (isMember) {
    addShowEventsButton(group, groupOptionsElement);
  } else {
    addJoinGroupButton(group, groupOptionsElement, groupElement);
  }

  groupElement.appendChild(groupOptionsElement);
}

/**
 * Adds a join button through which the user can become a member of that group.
 * @param {Object} group The object containing the data associated with that
 * group.
 * @param {Element} groupOptionsElement The element in which the button
 * will be inserted.
 * @param {Element} groupElement The element associated with this group.
 */
function addJoinGroupButton(group, groupOptionsElement, groupElement) {
  const joinGroupButton = createElement('button', 'rounded-button', 'Join');

  joinGroupButton.addEventListener('click', function() {
    joinGroup(group.id_);
    groupElement.remove();

    const joinedGroupsList = document.getElementById('joined-groups-container');
    joinedGroupsList.prepend(createGroupElement(group, true));
  });

  groupOptionsElement.appendChild(joinGroupButton);
}

/**
 * Adds a new event to the group specified.
 * @param {load} groupId The id of the group to which we will add the event.
 */
function addNewEvent(groupId) {
  const params = new URLSearchParams();
  const title = document.getElementById('new-event-title').value;
  const start = Date.parse(document.getElementById('new-event-start').value);
  const end = Date.parse(document.getElementById('new-event-end').value);

  params.append('group-id', groupId);
  params.append('title', title);
  params.append('start', start);
  params.append('end', end);

  fetch('/group-events', {method: 'POST', body: params});
}

/**
 * Adds a new group to the database.
 */
function addNewGroup() {
  const params = new URLSearchParams();
  const university = document.getElementById('new-group-university').value;
  const degree = document.getElementById('new-group-degree').value;
  const year = document.getElementById('new-group-year').value;

  params.append('university', university);
  params.append('degree', degree);
  params.append('year', year);

  fetch('/groups', {method: 'POST', body: params});
}

/**
 * Adds a show events button through which the user can see the list of events
 * of the group that they didn't join yet.
 * @param {Object} group The object containing the data associated with that
 * group.
 * @param {Element} groupOptionsElement The element in which the button
 * will be inserted.
 */
function addShowEventsButton(group, groupOptionsElement) {
  const showEventsButton = createElement('button', 'rounded-button', 'Events');

  showEventsButton.addEventListener('click', function() {
    showListOfEvents(group.id_);
  });

  groupOptionsElement.appendChild(showEventsButton);
}

/**
 * Changes the display property of the container with the one received.
 * @param {String} containerID The ID of the container.
 * @param {String} displayType The new display value.
 */
function changeContainerDisplay(containerID, displayType) {
  const container = document.getElementById(containerID);
  container.style.display = displayType;
}

/**
 * Closes the form with the given type (e.g. group, event) and displays
 * the button associated.
 * @param {String} formType The form type that is included in the ids of the
 * form and of the button.
 */
function closeForm(formType) {
  changeContainerDisplay(formType + '-form', 'none');
  changeContainerDisplay('open-' + formType + '-form', 'initial');
}

/**
 * Closes the list of events opened.
 */
function closeListOfEvents() {
  changeContainerDisplay('list-of-events', 'none');
  changeContainerDisplay('groups-section', 'initial');
}

/**
 * Creates a custom message based on the details provided (degree, year of
 * study).
 * @param {String} degree The degree that will be included in the message.
 * @param {String} year The year that will be included in the message.
 * @return {String} The message created.
 */
function createDetailsMessage(degree, year) {
  let message = year;

  switch (year) {
    case 1:
      message += 'st';
      break;
    case 2:
      message += 'nd';
      break;
    case 3:
      message += 'rd';
      break;
    default:
      message += 'th';
  }

  message += ' year ' + degree + ' students';
  return message;
}

/**
 * Creates the element associated with a given group.
 * @param {Object} group The group for which we will create a new element.
 * @param {Boolean} isMember Indicates whether or not the user is member of
 * the group.
 * @return {Element} The element created.
 */
function createGroupElement(group, isMember) {
  const groupElement = createElement('div', 'group', '');

  groupElement.appendChild(createElement('div', 'group-university',
      group.university_));
  groupElement.appendChild(createElement('hr', '', ''));
  groupElement.appendChild(createElement('div', 'group-details',
      createDetailsMessage(group.degree_, group.year_)));
  addGroupOptions(group, groupElement, isMember);

  return groupElement;
}

/**
 * Joins the group by sending the request to the server.
 * @param {String} groupId The id of the group that the user will join.
 */
function joinGroup(groupId) {
  const params = new URLSearchParams();
  params.append('group-id', groupId);

  fetch('/joined-groups', {method: 'POST', body: params});
}

/**
 * Fetches events (from the group specified by the groupId) from the servlet
 * and displays them in the given container. The function assumes the user has
 * joined all the events or none at all.
 * @param {String} servlet The servlet from which the events will be fetched.
 * @param {String} containerID The ID of the container that will display all
 * the events.
 * @param {long} groupId The ID of the group associated with the events.
 * @param {Boolean} hasJoined Indicates whether or not the user has joined
 * the events.
 */
async function loadGroupEvents(servlet, containerID, groupId, hasJoined) {
  const eventsContainer = document.getElementById(containerID);
  eventsContainer.innerHTML = '';

  const url = new URL(servlet, window.location.origin);
  const params = new URLSearchParams();
  params.append('group-id', groupId);
  url.search = params;

  const response = await fetch(url);
  const events = await response.json();

  events.forEach((event) => {
    const eventStartTime = new Date(event.startTime);
    const eventEndDate = new Date(event.endTime);
    const eventObject = new Event(event.id, event.title, eventStartTime,
        eventEndDate);
    eventsContainer.appendChild(createEventElement(eventObject, hasJoined));
  });
}

/**
 * Fetches the events joined by the user from the group specified by its ID.
 * @param {long} groupId The group id.
 */
async function loadJoinedEventsFromGroup(groupId) {
  await loadGroupEvents('/joined-events', 'joined-events-container',
      groupId, true);
}

/**
 * Fetches the events not joined by the user from the group specified by
 * its ID.
 * @param {long} groupId The group id.
 */
async function loadNotJoinedEventsFromGroup(groupId) {
  await loadGroupEvents('/group-events', 'not-joined-events-container',
      groupId, false);
}

/**
 * Fetches groups from the servlet and displays them in the given container.
 * The function assumes the user is either part of all the groups or none at
 * all.
 * @param {String} servlet The servlet from which the groups will be fetched.
 * @param {String} containerID The ID of the container that will display all
 * the groups.
 * @param {Boolean} isMember Indicates whether or not the user is a member of
 * those groups or not.
 */
function loadGroups(servlet, containerID, isMember) {
  const groupsList = document.getElementById(containerID);
  groupsList.innerHTML = '';

  fetch(servlet)
      .then((response) => response.json())
      .then((groups) => {
        groups.forEach((group) => {
          const groupObject = new Group(group.id, group.university,
              group.degree, group.year);
          groupsList.appendChild(createGroupElement(groupObject, isMember));
        });
      });
}

/**
 * Fetches the groups joined by the user and adds them to the associated
 * container.
 */
function loadJoinedGroups() {
  loadGroups('/joined-groups', 'joined-groups-container', true);
}

/**
 * Fetches the groups not joined by the user and adds them to the associated
 * container.
 */
function loadNotJoinedGroups() {
  loadGroups('/groups', 'not-joined-groups-container', false);
}

/**
 * Opens the form with the given type (e.g. group, event) and hides
 * the button associated.
 * @param {String} formType The form type that is included in the ids of the
 * form and of the button.
 */
function openForm(formType) {
  changeContainerDisplay(formType + '-form', 'block');
  changeContainerDisplay('open-' + formType + '-form', 'none');
}

/**
 * Shows the events (only the ones not joined yet by the user) of the
 * group as a list.
 * @param {long} groupId The group id.
 */
async function showListOfEvents(groupId) {
  changeContainerDisplay('list-of-events', 'initial');
  changeContainerDisplay('groups-section', 'none');

  await loadJoinedEventsFromGroup(groupId);
  await loadNotJoinedEventsFromGroup(groupId);

  const eventForm = document.getElementById('new-event-form');
  eventForm.onsubmit = function() {
    addNewEvent(groupId);
  };
}

window.addNewGroup = addNewGroup;
window.closeListOfEvents = closeListOfEvents;
window.closeForm = closeForm;
window.openForm = openForm;

export {loadJoinedGroups, loadNotJoinedGroups};
