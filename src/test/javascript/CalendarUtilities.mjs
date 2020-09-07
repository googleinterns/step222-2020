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

/** Class used to define the basic characteristics of an event. */
class Event {
  /**
   * Creates a new event with the given parameters.
   * @param {Long} id The id used to store the event in the database.
   * @param {String} title The title of the event.
   * @param {Date} start The start date of the event.
   * @param {Date} end The end date of the event.
   */
  constructor(id, title, start, end) {
    /** @private @const {Long} */
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
 * Compares two events based on their start date.
 * @param {Object} firstEvent First event to be compared.
 * @param {Object} secondEvent Second event to be compared.
 * @return {Boolean} False if the two events should be switched.
 */
function compareEventsByStartDate(firstEvent, secondEvent) {
  return firstEvent.start_ < secondEvent.start_;
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

module.exports = {
  Event: Event,
  compareEventsByStartDate: compareEventsByStartDate,
  getDateOfTheNextMonth: getDateOfTheNextMonth,
  getDateOfThePreviousMonth: getDateOfThePreviousMonth,
  getNumberOfDaysInMonth: getNumberOfDaysInMonth
}
