let googleAuth;

const API_KEY = 'AIzaSyD3ogMiqyWgGT9cjAI2ZxaNq5xSQP7Cz5E';
const CLIENT_ID =  '846915115683-52sirhdgpc0752cmavgo0igaes207a5n.apps.googleusercontent.com';
const SCOPE = 'https://www.googleapis.com/auth/calendar';

const CALENDAR_DISCOVERY_URL = 'https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest';
const DISCOVERY_DOCS = [CALENDAR_DISCOVERY_URL];

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
 * Initializes the JavaScript object and loads the gapi.auth2 module
 * to perform OAuth.
 */
async function initGoogleAuthObject() {
  await gapi.client.init({
    'apiKey': API_KEY,
    'clientId': CLIENT_ID,
    'discoveryDocs': DISCOVERY_DOCS,
    'scope': SCOPE,
    'prompt': 'select_account'
  });
}

/**
 * Loads the required Google APIs modules and initializes the client.
 */
function loadClient() {
  gapi.load('client:auth2', initClient);
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
