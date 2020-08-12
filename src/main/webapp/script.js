function onSignIn(googleUser) {
  var profile = googleUser.getBasicProfile();
  console.log('ID: '+ profile.getId());
  console.log('Name: '+ profile.getName());
  console.log('Image URL: '+ profile.getImageUrl());
  console.log('Email: '+ profile.getEmail());
}

function signOut() {
  var auth2 = gapi.auth2.getAuthInstance();
  auth2.signOut().then(function () {
    console.log('User signed out.');
  });
}

function onFailure(error) {
  console.log(error);
}

function renderButton() {
  gapi.signin2.render('custom-signin2', {
    'scope': 'profile email',
    'width': 240,
    'height': 50,
    'longtitle': true,
    'theme': 'dark',
    'onsuccess': onSignIn,
    'onfailure': onFailure
  });
}