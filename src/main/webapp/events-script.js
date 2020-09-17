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

const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'];
const WEEK_DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday',
  'Friday', 'Saturday'];

/** Class used to define the basic characteristics of an event. */
class Event {
  /**
   * Creates a new event with the given parameters.
   * @param {long} id The id used to store the event in the database.
   * @param {String} title The title of the event.
   * @param {Date} start The start date of the event.
   * @param {Date} end The end date of the event.
   */
  constructor(id, title, start, end) {
    /** @private @const {long} */
    this.id_ = id;
    /** @private @const {String} */
    this.title_ = title;
    /** @private @const {String} */
    this.start_ = start;
    /** @private @const {String} */
    this.end_ = end;
  }
}

/**
 * Gets the string associated with the date that contains information about the
 * weekday, the year, the month, the day, the hour and the minute.
 *
 * @param {Date} date The date for which we want to obtain the string.
 * @return The string generated from the date received.
 */
function getLongFormatDate(date) {
  return date.toLocaleDateString(navigator.language, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  });
}

/**
 * Gets the string associated with the date that contains information about the
 * weekday, the year, the month and the day.
 *
 * @param {Date} date The date for which we want to obtain the string.
 * @return The string generated from the date received.
 */
function getShortFormatDate(date) {
  return date.toLocaleDateString(navigator.language, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}

/**
 * Gets the date associated with the beginning of the current day.
 * @return {Date} The start of today.
 */
function getStartOfToday() {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return today;
}

/**
 * Adds the events associated with that date to the element. Today will be
 * displayed by default.
 * @param {Date} date The date for which the events will be added.
 * @param {Element} dateElement The element in which the events will be added.
 * @param {Object} eventsDictionary A dictionary that contains the events that
 * start in the given month.
 */
function addEventsOfTheDay(date, dateElement, eventsDictionary) {
  const today = getStartOfToday();

  if (date.getTime() == today.getTime()) {
    displayEventsAndMarkDay(date, dateElement, eventsDictionary);
  }

  const events = eventsDictionary[date];
  if (events === undefined) {
    return;
  }
  events.sort(compareEventsByStartDate);
  dateElement.classList.add('day-with-events');
  dateElement.addEventListener('click', function() {
    displayEventsAndMarkDay(date, dateElement, eventsDictionary);
  });
}

/**
 * Adds the days of the current month into the calendar table.
 * @param {Element} calendarTable The table that is part of the calendar.
 * @param {Date} date The date for whose month we will add the days.
 * @param {Object} eventsDictionary A dictionary that contains the events that
 * start in the given month.
 */
function addDaysOfTheMonth(calendarTable, date, eventsDictionary) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const numberOfDaysInMonth = getNumberOfDaysInMonth(date);
  const firstWeekDay = new Date(year, month, 1).getDay();

  // It will be used to display the days in groups of the same size as the
  // WEEK_DAYS array.
  let currentRowElement = createElement('tr', '', '');

  // Add padding for the days of the week that were part of the previous month.
  for (let day = 0; day < firstWeekDay; day++) {
    currentRowElement.appendChild(createElement('td', '', ''));
  }

  for (let day = 1; day <= numberOfDaysInMonth; day++) {
    // Add the row and begin a new one if the current one is already full.
    if ((day + firstWeekDay - 1) % WEEK_DAYS.length === 0) {
      calendarTable.appendChild(currentRowElement);
      currentRowElement = createElement('tr', '', '');
    }
    const dayElement = createElement('td', 'calendar-day', day);
    dayElement.addEventListener('mouseover', function() {
      dayElement.classList.add('hovered-day');
    });
    dayElement.addEventListener('mouseout', function() {
      dayElement.classList.remove('hovered-day');
    });
    addEventsOfTheDay(new Date(year, month, day), dayElement, eventsDictionary);
    currentRowElement.appendChild(dayElement);
  }

  // Add the final row that was created.
  calendarTable.appendChild(currentRowElement);
}

/**
 * Adds a header containing details about the current month and two buttons
 * to switch between the previous and the next months.
 * @param {Element} calendarContainer The container that will incorporate the
 * calendar.
 * @param {Date} date The date for whose month we will display the days and
 * the events associated.
 */
function addHeaderOfTheMonth(calendarContainer, date) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const calendarHeader = createElement('div', 'calendar-header', '');

  addButtonToGetNewMonth('arrow_back_ios', calendarHeader, date,
      getDateOfThePreviousMonth);
  calendarHeader.appendChild(createElement('p', 'calendar-details',
      MONTHS[month] + ' ' + year));
  addButtonToGetNewMonth('arrow_forward_ios', calendarHeader, date,
      getDateOfTheNextMonth);

  calendarContainer.appendChild(calendarHeader);
}

/**
 * Adds a button that will load the new month determined by the
 * function received (e.g. previous / next month).
 * @param {String} buttonClass The class of the button used to identify
 * the icon.
 * @param {Element} calendarHeader The header of the calendar.
 * @param {Date} date The date used to compute the new month.
 * @param {Function} functionToCreateNewMonth The function used to create
 * the new month.
 */
function addButtonToGetNewMonth(buttonClass, calendarHeader, date,
    functionToCreateNewMonth) {
  const newMonthButton = createElement('i', 'material-icons', buttonClass);
  newMonthButton.addEventListener('click', function() {
    loadNewMonth(date, functionToCreateNewMonth);
  });
  calendarHeader.appendChild(newMonthButton);
}

/**
 * Adds a button that can be used to open the chatroom of the event.
 * @param {Element} event The event corresponding to the chat room.
 * @param {Element} eventOptionsElement The element that will include this
 * button.
 */
function addChatroomButton(event, eventOptionsElement) {
  const chatroomButton = createElement('button', 'rounded-button', 'Chatroom');
  chatroomButton.addEventListener('click', function() {
    const url = new URL(window.location.origin + '/chat-room.html');
    url.searchParams.append('id', event.id_);
    url.searchParams.append('title', event.title_);
    window.location.href = url;
  });
  eventOptionsElement.appendChild(chatroomButton);
}

/**
 * Adds the event options available based on the user status (if the user joined
 * the event or not).
 * @param {Object} event The object containing the data associated with that
 * event.
 * @param {Element} eventElement The element associated with this event.
 * @param {Boolean} hasJoined Indicates whether or not the user has joined
 * the event.
 */
function addEventOptions(event, eventElement, hasJoined) {
  const eventOptionsElement = createElement('div', '', '');

  if (hasJoined) {
    addChatroomButton(event, eventOptionsElement);
  } else {
    addJoinEventButton(event, eventOptionsElement, eventElement);
  }

  eventElement.appendChild(eventOptionsElement);
}

/**
 * Adds a join button through which the user can join that event.
 * @param {Object} event The object containing the data associated with that
 * event.
 * @param {Element} eventOptionsElement The element that will include this
 * button.
 * @param {Element} eventElement The element associated with this event.
 */
function addJoinEventButton(event, eventOptionsElement, eventElement) {
  const joinEventButton = createElement('button', 'rounded-button', 'Join');
  joinEventButton.addEventListener('click', async function() {
    await joinEvent(event.id_);
    eventElement.remove();

    const joinedEventsList = document.getElementById('joined-events-container');
    joinedEventsList.prepend(createEventElement(event, true));

    loadCalendar();
  });
  eventOptionsElement.appendChild(joinEventButton);
}

/**
 * Adds the week days header in the calendar's table.
 * @param {Element} calendarTable The table that is part of the calendar.
 */
function addWeekDaysToCalendar(calendarTable) {
  const weekDaysRow = createElement('tr', 'week-days-row', '');
  for (let i = 0; i < WEEK_DAYS.length; i++) {
    weekDaysRow.appendChild(createElement('th', 'week-day', WEEK_DAYS[i]));
  }
  calendarTable.appendChild(weekDaysRow);
}

/**
 * Compares two events based on their start date.
 * @param {Object} firstEvent First event to be compared.
 * @param {Object} secondEvent Second event to be compared.
 * @return {Integer} A positive number if the two events should be switched.
 */
function compareEventsByStartDate(firstEvent, secondEvent) {
  return firstEvent.start_ < secondEvent.start_;
}

/**
 * Creates the element associated with a given event.
 * @param {Object} event The event for which we will create a new element.
 * @param {Boolean} hasJoined Indicates whether or not the user has joined
 * the event.
 * @param {object} dateFormat The format used to display the start date and the
 * end date of the event.
 * @return {Element} The element created.
 */
function createEventElement(event, hasJoined) {
  const eventElement = createElement('div', 'event', '');

  eventElement.appendChild(createElement('div', 'event-title', event.title_));
  eventElement.appendChild(createElement('hr', '', ''));
  eventElement.appendChild(createElement('div', 'event-time',
      getLongFormatDate(event.start_) + ' - ' + getLongFormatDate(event.end_)));
  addEventOptions(event, eventElement, hasJoined);

  return eventElement;
}

/**
 * Creates the calendar associated with the date given.
 * @param {Date} date The date for which we want to display the calendar.
 */
async function createCalendarOfTheMonth(date) {
  const eventsDictionary = await loadEvents(date);

  const calendarContainer = document.getElementById('calendar');
  const calendarTable = createElement('table', 'calendar-table', '');

  calendarContainer.innerHTML = '';
  addHeaderOfTheMonth(calendarContainer, date);
  addWeekDaysToCalendar(calendarTable);
  addDaysOfTheMonth(calendarTable, date, eventsDictionary);

  calendarContainer.appendChild(calendarTable);
}

/**
 * Displays the events happening on the given date for which the user signed up.
 * @param {Date} date The date for which the events will be displayed.
 * @param {Object} eventsDictionary A dictionary that contains the events that
 * start in the given month.
 */
function displayEvents(date, eventsDictionary) {
  const today = getStartOfToday();
  const eventsHeadline = document.getElementById('events-headline');
  eventsHeadline.innerHTML = 'Your events ';

  if (date.getTime() === today.getTime()) {
    eventsHeadline.innerHTML += 'today';
  } else {
    eventsHeadline.innerHTML += 'on ' + getShortFormatDate(date);
  }

  const eventsContainer = document.getElementById('events');
  const events = eventsDictionary[date];
  eventsContainer.innerHTML = '';

  if (events === undefined) {
    eventsContainer.innerHTML = 'No events yet.';
    eventsContainer.classList.add('no-events-container');
    return;
  }

  eventsContainer.classList.remove('no-events-container');
  for (let i = 0; i < events.length; i++) {
    eventsContainer.appendChild(createEventElement(events[i], true));
  }
}

/**
 * Displays the events from the date received for which the user signed up
 * and marks the element as the one containing the selected day.
 * @param {Date} date The date for which the events will be displayed.
 * @param {Element} dateElement The element containing the selected day.
 * @param {Object} eventsDictionary A dictionary that contains the events that
 * start in the given month.
 */
function displayEventsAndMarkDay(date, dateElement, eventsDictionary) {
  displayEvents(date, eventsDictionary);

  const currentlySelectedDay = document.getElementById('selected-day');
  if (currentlySelectedDay !== null) {
    currentlySelectedDay.id = '';
  }
  dateElement.id = 'selected-day';
}

/**
 * Gets the next month date of the date received.
 * @param {Date} date The date for which the new date will be created.
 * @return {Date} The date of the next month.
 */
function getDateOfTheNextMonth(date) {
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
 * @return {Integer} The number of days.
 */
function getNumberOfDaysInMonth(date) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const nextMonthDate = getDateOfTheNextMonth(new Date(year, month));
  const nextMonthYear = nextMonthDate.getFullYear();
  const nextMonth = nextMonthDate.getMonth();

  return new Date(nextMonthYear, nextMonth, 0).getDate();
}

/**
 * Gets the previous month of the date received.
 * @param {Date} date The date for which the new date will be created.
 * @return {Date} The date of the previous month.
 */
function getDateOfThePreviousMonth(date) {
  const currentMonth = date.getMonth();
  const currentYear = date.getFullYear();

  if (currentMonth === 0) {
    return new Date(currentYear - 1, 11);
  }

  return new Date(currentYear, currentMonth - 1);
}

/**
 * Joins the event by sending the request to the server.
 * @param {String} eventId The id of the event that the user will join.
 */
async function joinEvent(eventId) {
  const params = new URLSearchParams();
  params.append('event-id', eventId);

  await fetch('/joined-events', {method: 'POST', body: params});
}

/**
 * Loads the calendar associated with the current date and displays the events
 * of the current day for which the user signed up.
 */
function loadCalendar() {
  const currentDate = new Date();
  createCalendarOfTheMonth(currentDate);
}

/**
 * Fetches events joined by the user that start in the month of the date
 * received.
 * @param {Date} date The date relative to whose month the events will be
 * retrieved.
 */
async function loadEvents(date) {
  const url = new URL('/joined-events', window.location.origin);
  const params = new URLSearchParams();
  params.append('beginning-date', new Date(date.getFullYear(),
      date.getMonth()).getTime());
  params.append('ending-date', getDateOfTheNextMonth(date).getTime());
  url.search = params;

  const response = await fetch(url);
  const events = await response.json();
  const eventsDictionary = {};

  events.forEach((event) => {
    const eventStartDate = new Date(event.startTime);
    const eventEndDate = new Date(event.endTime);
    const eventStartDay = new Date(eventStartDate.getFullYear(),
        eventStartDate.getMonth(), eventStartDate.getDate());
    const eventObject = new Event(event.id, event.title, eventStartDate,
        eventEndDate);

    if (eventStartDay in eventsDictionary) {
      eventsDictionary[eventStartDay].push(eventObject);
    } else {
      eventsDictionary[eventStartDay] = [eventObject];
    }
  });

  return eventsDictionary;
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
  createCalendarOfTheMonth(newMonthDate);
}

export {createEventElement, Event, loadCalendar};
