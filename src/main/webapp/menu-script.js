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

/**
 * Creates a new element with a specified type, class and innerText.
 * @param {string} elementType The type of the element that will be created.
 * @param {string} className The class of the element that will be created.
 * @param {string} innerText The innerText of the element that will be
 * created.
 * @return {element} The element created.
 */
function createElement(elementType, className, innerText) {
  const newElement = document.createElement(elementType);
  newElement.className = className;
  newElement.innerText = innerText;

  return newElement;
}

/**
 * Gets the section ID from the option element.
 * @param {Element} option The menu option associated with the section.
 * @return {String} The section ID.
 */
function getSectionIdFromOption(option) {
  return option.innerHTML.trim().toLowerCase();
}

/**
 * Shows the new section by hiding the current active section.
 * @param {Element} activeOption The menu option linked to the section
 * to be displayed.
 */
function showSection(activeOption) {
  const currentActiveOption = document.getElementById('active-option');
  const currentActiveSectionId = getSectionIdFromOption(currentActiveOption);
  const currentActiveSection = document.getElementById(currentActiveSectionId);

  currentActiveOption.id = '';
  currentActiveSection.classList.remove('active-section');

  const activeSectionId = getSectionIdFromOption(activeOption);
  const activeSection = document.getElementById(activeSectionId);

  activeOption.id = 'active-option';
  activeSection.classList.add('active-section');
}

export {createElement, getSectionIdFromOption, showSection};
