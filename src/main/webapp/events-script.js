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

// Dictionary used to simply retrieve the events that start on a given day.
const eventsDictionary = {};

/** Class used to define the basic characteristics of an event. */
class Event {
  /**
   * Creates a new event with the given parameters.
   * @param {String} title The title of the event.
   * @param {Date} start The start date of the event.
   * @param {Date} end The end date of the event.
   */
  constructor(title, start, end) {
    /** @private @const {String} */
    this.title_ = title;
    /** @private @const {String} */
    this.start_ = start;
    /** @private @const {String} */
    this.end_ = end;
  }
}

/**
 * Adds the events associated with that date to the element.
 * @param {Date} date The date for which the events will be added.
 * @param {Element} dateElement The element in which the events will be added.
 */
function addEventsOfTheDay(date, dateElement) {
  const events = eventsDictionary[date];
  if (events === undefined) {
    return;
  }
  events.sort(compareEventsByStartDate);
  dateElement.classList.add('day-with-events');
  dateElement.addEventListener('click', function() {
    displayEvents(date);

    const currentlySelectedDay = document.getElementById('selected-day');
    if (currentlySelectedDay !== null) {
      currentlySelectedDay.id = '';
    }
    dateElement.id = 'selected-day';
  });
}

/**
 * Adds the days of the current month into the calendar table.
 * @param {Element} calendarTable The table that is part of the calendar.
 * @param {Date} date The date for whose month we will add the days.
 */
function addDaysOfTheMonth(calendarTable, date) {
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
    addEventsOfTheDay(new Date(year, month, day), dayElement);
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
  const firstEventStartDate = firstEvent.start_;
  const secondEventStartDate = secondEvent.start_;

  return firstEventStartDate < secondEventStartDate;
}

/**
 * Creates the element associated with a given event.
 * @param {object} event The event for which we will create a new element.
 * @return {Element} The element created.
 */
function createEventElement(event) {
  const eventElement = createElement('div', 'event', '');

  eventElement.appendChild(createElement('div', 'event-title', event.title_));
  eventElement.appendChild(createElement('hr', '', ''));
  eventElement.appendChild(createElement('div', 'event-time',
      event.start_ + ' - ' + event.end_));

  return eventElement;
}

/**
 * Creates the calendar associated with the date given.
 * @param {Date} date The date for which we want to display the calendar.
 */
function createCalendarOfTheMonth(date) {
  const calendarContainer = document.getElementById('calendar');
  const calendarTable = createElement('table', 'calendar-table', '');

  addHeaderOfTheMonth(calendarContainer, date);
  addWeekDaysToCalendar(calendarTable);
  addDaysOfTheMonth(calendarTable, date);

  calendarContainer.appendChild(calendarTable);
}

/**
 * Displays the events happening on the given date in the 'events'
 * container.
 * @param {Date} date The date for which the events will be displayed.
 */
function displayEvents(date) {
  const events = eventsDictionary[date];
  if (events === undefined) {
    return;
  }

  const eventsContainer = document.getElementById('events');
  eventsContainer.innerHTML = '';

  for (let i = 0; i < events.length; i++) {
    eventsContainer.appendChild(createEventElement(events[i]));
  }
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
 * Loads the calendar associated with the current date and displays the events
 * of the current day.
 */
function loadCalendar() {
  const currentDate = new Date();
  const today = new Date(currentDate.getFullYear(), currentDate.getMonth(),
      currentDate.getDate());

  createCalendarOfTheMonth(currentDate);
  displayEvents(today);
}

/**
 * Fetches events from the server and stores them in the dictionary.
 */
async function loadEvents() {
  const response = await fetch('/user-events');
  const events = await response.json();

  events.forEach((event) => {
    const eventStartDate = new Date(event.start);
    const eventEndDate = new Date(event.end);
    const eventStartDay = new Date(eventStartDate.getFullYear(),
        eventStartDate.getMonth(), eventStartDate.getDate());
    const eventObject = new Event(event.title, eventStartDate,
        eventEndDate);

    if (eventStartDay in eventsDictionary) {
      eventsDictionary[eventStartDay].push(eventObject);
    } else {
      eventsDictionary[eventStartDay] = [eventObject];
    }
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
  createCalendarOfTheMonth(newMonthDate);
}

export {createEventElement, Event, loadEvents, loadCalendar};
