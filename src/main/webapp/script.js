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

const CLIENT_ID =
    '764525537710-crtrm9pdut61dgijldb591jjjl5c2rfq.apps.googleusercontent.com';
const SCOPE = 'profile email';
const DISCOVERY_DOCS = [];

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
 * Gets the value of the cookie with the given name if it is set. Otherwise,
 * it will return null.
 * @param {String} cookieName The cookie name.
 * @return {String} The value associated with the cookie if it was set or null
 * otherwise.
 */
function getCookie(cookieName) {
  const cookies = document.cookies.split(';');

  for (const cookie of cookies) {
    const cookiePair = cookie.split('=');
    if (cookieName === cookiePair[0].trim()) {
      return decodeURIComponent(cookiePair[1]);
    }
  }

  return null;
}

/**
 * Initializes the GoogleAuth object and checks if the user is on the
 * right page based on their login status. If not, redirects them to
 * the appropriate page.
 */
async function initClient() {
  await initGoogleAuthObject();
}

/**
 * Initializes the JavaScript object and loads the gapi.auth2 module
 * to perform OAuth.
 */
async function initGoogleAuthObject() {
  await gapi.auth2.init({
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
 * Loads the profile data associated with the currently logged in user.
 */
async function loadProfileData() {
  const googleAuth = gapi.auth2.getAuthInstance();

  if (googleAuth.isSignedIn.get()) {
    const userProfile = googleAuth.currentUser.get().getBasicProfile();
    const menuElement = document.getElementById('menu');
    const profilePicture = createElement('img', 'profile-picture', '');
    profilePicture.src = userProfile.getImageUrl();
    menuElement.prepend(profilePicture);
  } else {
    window.location.href = 'index.html';
  }
}

/**
 * Sets a cookie with the given name and value.
 * @param {String} cookieName The cookie name.
 * @param {String} cookieValue The cookie value.
 */
function setCookie(cookieName, cookieValue) {
  document.cookie = cookieName + '=' + encodeURIComponent(cookieValue);
}

/**
 * Signs in the user, updates the authorization status and redirects them
 * to the home page (only if they also provide the authorization needed).
 */
async function signIn() {
  const googleAuth = gapi.auth2.getAuthInstance();
  const authResult = await googleAuth.signIn();

  if (googleAuth.isSignedIn.get()) {
    const idToken = authResult.getAuthResponse().id_token;
    setCookie('id_token', idToken);
    await fetch('/add-user', {method: 'POST'});
    window.location.href = 'home-page.html';
  }
}

/**
 * Signs out the user, deletes the cookie and redirects them to the start page.
 */
function signOut() {
  const googleAuth = gapi.auth2.getAuthInstance();
  googleAuth.signOut();
  document.cookie = 'id_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC;';
  window.location.href = 'index.html';
}

window.signIn = signIn;
window.addEventListener('load', loadClient);

export {initClient, createElement, loadProfileData, signOut};
