let utils = require('../src/CalendarUtilities.mjs');

describe("Compare events", function() {

  it("Nothing should be nothing", function() {
    expect().nothing();
  });

  it("The events can be compared by the start date", function() {
    expect(utils.compareEventsByStartDate).toBeDefined();
  });

  it("The first event is smaller than the second one if it begins earlier",
      function() {
    const firstEvent = new utils.Event(0, 'First event', new Date(2020, 8, 10),
        new Date(2020, 8, 11));
    const secondEvent = new utils.Event(1, 'Second event',
        new Date(2020, 9, 10), new Date(2020, 9, 11));
    const comparisonResult = utils.compareEventsByStartDate(firstEvent,
        secondEvent);

    expect(comparisonResult).toBe(true);
  });

  it("The first event is bigger than the second one if they begin at the" +
      "same time", function() {
    const firstEvent = new utils.Event(0, 'First event', new Date(2020, 8, 10),
        new Date(2020, 8, 11));
    const secondEvent = new utils.Event(1, 'Second event',
        new Date(2020, 8, 10), new Date(2020, 9, 11));
    const comparisonResult = utils.compareEventsByStartDate(firstEvent,
        secondEvent);

    expect(comparisonResult).toBe(false);
  });

  it("The first event is bigger than the second one if it begins later",
      function() {
    const firstEvent = new utils.Event(0, 'First event', new Date(2020, 9, 10),
        new Date(2020, 9, 11));
    const secondEvent = new utils.Event(1, 'Second event',
        new Date(2020, 8, 10), new Date(2020, 8, 11));
    const comparisonResult = utils.compareEventsByStartDate(firstEvent,
        secondEvent);

    expect(comparisonResult).toBe(false);
  });
});

describe("Previous month", function() {

  it("The previous month can be determined", function() {
    expect(utils.getDateOfThePreviousMonth).toBeDefined();
  });

  it("The previous month is generated correctly when the year changes",
      function() {
    expect(utils.getDateOfThePreviousMonth(new Date(2020, 0)))
        .toEqual(new Date(2019, 11));
  });

  it("The previous month is generated correctly when the year doesn't change",
      function() {
    expect(utils.getDateOfThePreviousMonth(new Date(2020, 6)))
        .toEqual(new Date(2020, 5));
  });
});

describe("Next month", function() {

  it("The next month can be determined", function() {
    expect(utils.getDateOfTheNextMonth).toBeDefined();
  });

  it("The next month is generated correctly when the year changes",
      function() {
    expect(utils.getDateOfTheNextMonth(new Date(2019, 11)))
        .toEqual(new Date(2020, 0));
  });

  it("The next month is generated correctly when the year doesn't change",
      function() {
    expect(utils.getDateOfTheNextMonth(new Date(2020, 5)))
        .toEqual(new Date(2020, 6));
  });
});

describe("The number of days in a month", function() {
  
  it("The number of days in a month can be determined", function() {
    expect(utils.getNumberOfDaysInMonth).toBeDefined();
  });

  it("The number of days in a regular month is generated correctly",
      function() {
    expect(utils.getNumberOfDaysInMonth(new Date(2020, 0))).toBe(31);
  });

  it("The number of days in February is 29 if it's a leap year",
      function() {
    expect(utils.getNumberOfDaysInMonth(new Date(2020, 1))).toBe(29);
  });

  it("The number of days in February is 28 if it's not a leap year",
      function() {
    expect(utils.getNumberOfDaysInMonth(new Date(2019, 1))).toBe(28);
  });
});