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

const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'];
const WEEK_DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday',
    'Friday', 'Saturday'];

/** Class used to define the basic characteristics of a group. */
class Group {
  /**
   * Creates a new group with the given parameters.
   * @param {String} university The name of the university.
   * @param {String} degree The name of the degree.
   * @param {int} year The year of study.
   */
  constructor(university, degree, year) {
    this.university = university;
    this.degree = degree;
    this.year = year;
  }
}

/** Class used to define the basic characteristics of a event. */
class Event {
  /**
   * Creates a new event with the given parameters.
   * @param {String} title The title of the event.
   * @param {String} start The start date of the event.
   * @param {int} end The end date of the event.
   */
  constructor(title, start, end) {
    this.title = title;
    this.start = start;
    this.end = end;
  }
}

/**
 * Adds the days of the current month into the calendar table.
 * @param {Element} calendarTable The table that is part of the calendar.
 * @param {Date} date The date for whose month we will add the days.
 */
function addMonthDays(calendarTable, date) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const numberOfDaysInMonth = getNumberOfDaysInMonth(date);
  const firstWeekDay = new Date(year, month, 1).getDay();

  // It will used to display the days in groups of the same size as the
  // WEEK_DAYS array.
  let currentRowElement = createElement('tr', '', '');

  // Add padding for the days of the week that were part of the previous month.
  for (let i = 0; i < firstWeekDay; i++) {
    currentRowElement.appendChild(createElement('td', '', ''));
  }

  for (let i = 0; i < numberOfDaysInMonth; i++) {
    // Add the row and begin a new one if the current one is full.
    if ((i + firstWeekDay) % WEEK_DAYS.length === 0) {
      calendarTable.appendChild(currentRowElement);
      currentRowElement = createElement('tr', '', '');
    }
    currentRowElement.appendChild(createElement('td', 'calendar-day', i + 1));
  }

  // Add the final row that was created.
  calendarTable.appendChild(currentRowElement);
}

/**
 * Adds a header containing details about the current month and two buttons
 * to switch between the previous and next month.
 * @param {Element} calendarContainer The container that will contain the
 * calendar.
 * @param {Date} date The date for whose month we will display the days and
 * events associated.
 */
function addMonthHeader(calendarContainer, date) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const calendarHeader = createElement('div', 'calendar-header', '');

  addNewMonthButton('arrow_back_ios', calendarHeader, date, getPreviousMonthDate);
  calendarHeader.appendChild(createElement('p', 'calendar-details', MONTHS[month] + ' ' + year));
  addNewMonthButton('arrow_forward_ios', calendarHeader, date, getNextMonthDate);

  calendarContainer.appendChild(calendarHeader);
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
 * Adds a button that will load the new month created determined by the
 * function received (e.g. previous / next month).
 * @param {String} buttonClass The class of the button used to identify
 * the icon.
 * @param {Element} calendarHeader The header of the calendar.
 * @param {Date} date The date used to compute the new month.
 * @param {Function} functionToCreateNewMonth The function used to create
 * the new month.
 */
function addNewMonthButton(buttonClass, calendarHeader, date,
    functionToCreateNewMonth) {
  const newMonthButton = createElement('i', 'material-icons', buttonClass);
  newMonthButton.addEventListener('click', function() {
    loadNewMonth(date, functionToCreateNewMonth)
  }); 
  calendarHeader.appendChild(newMonthButton);
}

/**
 * Adds the week days header in the calendar's table.
 * @param {Element} calendarTable The table that is part of the calendar.
 */
function addWeekDays(calendarTable) {
  const weekDaysRow = createElement('tr', 'week-days-row', '');

  for (let i = 0; i < WEEK_DAYS.length; i++) {
    weekDaysRow.appendChild(createElement('th', 'week-day', WEEK_DAYS[i]));
  }
  calendarTable.appendChild(weekDaysRow);
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
 * Creates a Date JavaScript object based on the Java object received.
 * @param {Object} dateObject The Java object received.
 * @return {Date} The JavaScript object associated.
 */
function createDate(dateObject) {
  let date = new Date();

  date.setFullYear(dateObject.year);
  date.setMonth(dateObject.month);
  date.setDate(dateObject.day);
  date.setHours(dateObject.hour);
  date.setMinutes(dateObject.minute);

  return date;
}

/**
 * Creates a custom message based on the details provided (degree, year of study).
 * @return {String} The message created.
 */
function createDetailsMessage(degree, year) {
  let message = year;

  switch(year) {
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
 * @return {Element} The element created.
 */
function createGroupElement(group) {
  const groupElement = createElement('div', 'group', '');

  groupElement.appendChild(createElement('div', 'group-university', group.university));
  groupElement.appendChild(createElement('hr', '', ''));
  groupElement.appendChild(createElement('div', 'group-details',
      createDetailsMessage(group.degree, group.year)));

  return groupElement;
}

/**
 * Creates the calendar associated with the date given.
 * @param {Date} date The date for which we want to display the calendar.
 */
function createMonthCalendar(date) {
  const calendarContainer = document.getElementById('calendar');
  const calendarTable = createElement('table', 'calendar-table', '');

  addMonthHeader(calendarContainer, date);
  addWeekDays(calendarTable);
  addMonthDays(calendarTable, date);
  calendarContainer.appendChild(calendarTable);
}

/**
 * Gets the next month date of the date received.
 * @param {Date} date The date for which the new date will be created.
 * @return {Date} The date of the next month.
 */
function getNextMonthDate(date) {
  const currentMonth = date.getMonth();
  const currentYear = date.getFullYear();

  if (currentMonth === 11) {
    return new Date(currentYear + 1, 0);
  }

  return new Date(currentYear, currentMonth + 1);
}

/**
 * Gets the number of days present in the month of the date received.
 * @param {Date} date The date for which we will compute the number of days.
 * @return {Integer} The number of the days.
 */
function getNumberOfDaysInMonth(date) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const nextMonthDate = getNextMonthDate(new Date(year, month));
  const nextMonthYear = nextMonthDate.getFullYear();
  const nextMonth = nextMonthDate.getMonth();

  return new Date(nextMonthYear, nextMonth, 0).getDate();
}

/**
 * Gets the previous month date of the date received.
 * @param {Date} date The date for which the new date will be created.
 * @return {Date} The date of the previous month.
 */
function getPreviousMonthDate(date) {
  const currentMonth = date.getMonth();
  const currentYear = date.getFullYear();

  if (currentMonth === 0) {
    return new Date(currentYear - 1, 11);
  }
  
  return new Date(currentYear, currentMonth - 1);
}

/**
 * Loads the calendar associated with the current date.
 */
function loadCalendar() {
  const currentDate = new Date();
  createMonthCalendar(currentDate);
}

/**
 * Fetches events from the server and returns them.
 * @return {Array} The array of events retrieved from the server.
 */
async function loadEvents() {
  const eventsArray = [];
  const response = await fetch('/events')
  const events = await response.json();
  
  events.forEach((event) => {
    eventsArray.push(new Event(event.name, createDate(event.start), createDate(event.end)));
  });

  return eventsArray;
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
          const groupObject = new Group(group.university, group.degree, group.year);
          groupsList.appendChild(createGroupElement(groupObject));
        });
      });
}

/**
 * Loads the new month determined by the function provided.
 * @param {Date} date The date used to compute the new month.
 * @param {Function} functionToCreateNewMonth The function used to create
 * the new month.
 */
function loadNewMonth(date, functionToCreateNewMonth) {
  const newMonthDate = functionToCreateNewMonth(date);
  document.getElementById('calendar').innerHTML = '';
  createMonthCalendar(newMonthDate);
}

/**
 * Loads the calendar and events associated with the user profile.
 */
function loadProfile() {
  loadGroups();
  loadCalendar();
}

/**
 * Opens the new group form and hides the "Create new group" button.
 */
function openGroupForm() {
  changeContainerDisplay('group-form', 'block');
  changeContainerDisplay('open-group-form', 'none');
}
