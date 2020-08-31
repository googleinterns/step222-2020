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
setInterval(showMessages, 1000);

window.loadEventData = function loadEventData() {
  const urlParams = new URLSearchParams(window.location.search);
  eventId = urlParams.get('id');
  document.getElementById("title").innerHTML = urlParams.get('title');;
}

window.sendMessage = async function sendMessage() {
  const form = document.getElementById('message-form');
  const params = new URLSearchParams();
  const formData = new FormData(form);
  for (const pair of formData.entries()) {
    params.append(pair[0], pair[1]);
  }
  params.append('id', eventId)
  await fetch('/messages', {
    method: 'POST',
    body: params,
  });
  form.reset();
  showMessages();
};

async function showMessages() {
  fetch('/messages?id=' + eventId)
    .then((response) => response.json()).then(async (messages) => {
        const chat = document.getElementById('chat-container');
        chat.innerHTML = '';
        let i = 0;
        for (i = 0; i < messages.length; i++) {
          const message = createMessageElement(messages[i]);
          chat.appendChild(message);
        }
    });
}

function createMessageElement(message) {
  const element = document.createElement('p');
  element.innerText = message;
  element.className = 'message';
  return element;
}
