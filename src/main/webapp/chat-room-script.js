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


let eventId;
// Repeatedly load messages to synchronise chat between users.
const pollingIntervall = 1000;
setInterval(showMessages, pollingIntervall);

/**
 * Loads event id and title for this chat room.
 */
window.loadEventData = function loadEventData() {
  const urlParams = new URLSearchParams(window.location.search);
  eventId = urlParams.get('id');
  document.getElementById('title').innerHTML = urlParams.get('title');
};

/**
 * Sends message entered in form to server.
 */
window.sendMessage = async function sendMessage() {
  const form = document.getElementById('message-form');
  const params = new URLSearchParams();
  const formData = new FormData(form);
  for (const pair of formData.entries()) {
    params.append(pair[0], pair[1]);
  }
  params.append('id', eventId);
  await fetch('/messages', {
    method: 'POST',
    body: params,
  });
  form.reset();
  showMessages();
};

/**
 * Retrieves all messages associated with the event from the server.
 */
async function showMessages() {
  fetch('/messages?id=' + eventId)
      .then((response) => response.json()).then(async (messages) => {
        const chat = document.getElementById('chat-container');
        chat.innerHTML = '';
        let i = 0;
        for (i = 0; i < messages.length; i++) {
          chat.appendChild(createMessageElement(messages[i]));
        }
      });
}

/**
 * Creates html element to display a chat message.
 * @param {String} message The message content.
 * @return {Element} The element created.
 */
function createMessageElement(message) {
  const element = document.createElement('p');
  element.innerText = message;
  element.className = 'message';
  return element;
}
