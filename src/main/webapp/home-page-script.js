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

import {initClient, createElement, loadProfileData, signOut} from './script.js';
import {loadGroups} from './groups-script.js';
import {loadEvents, loadCalendar} from './events-script.js';

/**
 * Initializes the client and loads the data associated with their profile.
 */
async function initHomeClient() {
  await initClient();
  loadProfile();
}

/**
 * Loads the required Google APIs modules and initializes the client
 * already logged in. The difference between this function and the loadClient
 * function is that we will also load the data associated with this client.
 */
function loadHomeClient() {
  gapi.load('client:auth2', initHomeClient);
}

/**
 * Loads the data associated with the user profile.
 */
async function loadProfile() {
  await loadProfileData();
  await loadEvents();
  loadCalendar();
  loadGroups();
}

window.signOut = signOut;
loadHomeClient('auth');
