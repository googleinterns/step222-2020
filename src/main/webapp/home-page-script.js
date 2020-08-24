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

/** Class used to define the basic characteristics of a group. */
class Group {
  /**
   * Creates a new group with the given parameters.
   * @param {String} university The name of the university.
   * @param {String} degree The name of the degree.
   * @param {int} year The year of study.
   */
  constructor(university, degree, year) {
    /** @private @const {String} */
    this.university_ = university;
    /** @private @const {String} */
    this.degree_ = degree;
    /** @private @const {int} */
    this.year_ = year;
  }
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
 * Changes the display property of the container with the one received.
 * @param {String} containerID The ID of the container.
 * @param {String} displayType The new display value.
 */
function changeContainerDisplay(containerID, displayType) {
  const container = document.getElementById(containerID);
  container.style.display = displayType;
}

/**
 * Closes the new group form and displays the "Create new group" button.
 */
function closeGroupForm() {
  changeContainerDisplay('group-form', 'none');
  changeContainerDisplay('open-group-form', 'initial');
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
 * @param {object} group The group for which we will create a new element.
 * @return {element} The element created.
 */
function createGroupElement(group) {
  const groupElement = createElement('div', 'group', '');

  groupElement.appendChild(createElement('div', 'group-university',
      group.university));
  groupElement.appendChild(createElement('hr', '', ''));
  groupElement.appendChild(createElement('div', 'group-details',
      createDetailsMessage(group.degree, group.year)));

  return groupElement;
}

/**
 * Fetches groups from the server and adds them to the groups section.
 */
function loadGroups() {
  const groupsList = document.getElementById('groups');

  fetch('/groups')
      .then((response) => response.json())
      .then((groups) => {
        groups.forEach((group) => {
          const groupObject = new Group(group.university, group.degree,
              group.year);
          groupsList.appendChild(createGroupElement(groupObject));
        });
      });
}

/**
 * Opens the new group form and hides the "Create new group" button.
 */
function openGroupForm() {
  changeContainerDisplay('group-form', 'block');
  changeContainerDisplay('open-group-form', 'none');
}
