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

/* eslint-disable no-unused-vars */

let googleAuth;

const API_KEY = '';
const CLIENT_ID = '';
const SCOPE = 'profile email';
const DISCOVERY_DOCS = [];

/**
 * Retrieves the login status and checks if the user has granted the
 * scopes. Redirects the user to the appropriate page based on the
 * information mentioned before.
 * @param {boolean} isSignedIn A boolean value that indicates whether the
 * user is currently signed in.
 */
function checkSigninStatus(isSignedIn) {
  const currentURL = location.href;

  if (isSignedIn) {
    const user = googleAuth.currentUser.get();
    const isAuthorized = user.hasGrantedScopes(SCOPE);

    if (isAuthorized) {
      if (currentURL.endsWith('index.html')) {
        window.location.href = 'home-page.html';
      }
    } else {
      if (currentURL.endsWith('home-page.html')) {
        window.location.href = 'index.html';
      }
    }
  } else {
    if (currentURL.endsWith('home-page.html')) {
      window.location.href = 'index.html';
    }
  }
}

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
 * Initializes the GoogleAuth object and checks if the user is on the
 * right page based on their login status. If not, redirects them to
 * the appropriate page.
 */
async function initClient() {
  await initGoogleAuthObject();

  googleAuth = gapi.auth2.getAuthInstance();
  googleAuth.isSignedIn.listen(checkSigninStatus);
  checkSigninStatus(googleAuth.isSignedIn.get());
}

/**
 * Initializes the client and loads the data associated with their profile.
 */
async function initHomeClient() {
  await initClient();
  loadProfileData();
}

/**
 * Initializes the JavaScript object and loads the gapi.auth2 module
 * to perform OAuth.
 */
async function initGoogleAuthObject() {
  await gapi.client.init({
    'apiKey': API_KEY,
    'clientId': CLIENT_ID,
    'discoveryDocs': DISCOVERY_DOCS,
    'scope': SCOPE,
    'prompt': 'select_account',
  });
}

/**
 * Loads the required Google APIs modules and initializes the client.
 */
function loadClient() {
  gapi.load('client:auth2', initClient);
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
 * Loads the profile data associated with the currently logged in user.
 */
function loadProfileData() {
  if (googleAuth.isSignedIn.get()) {
    const userProfile = googleAuth.currentUser.get().getBasicProfile();

    const menuElement = document.getElementById('menu');
    const profilePicture = createElement('img', 'profile-picture', '');
    profilePicture.src = userProfile.getImageUrl();
    menuElement.prepend(profilePicture);
  }
}

/**
 * Signs in the user and redirects them to the home page.
 */
async function signIn() {
  await googleAuth.signIn();
  window.location.href = 'home-page.html';
}

/**
 * Signs out the user and redirects them to the start page.
 */
async function signOut() {
  await googleAuth.signOut();
  window.location.href = 'index.html';
}
