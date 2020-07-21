/* Number of characters until lecture ID in URL */
const URL_ID_SPACING = 5;

/* Database key for specific lecture */
const LECTURE_KEY = getParam(window.location.href);

// TODO: Here for testing, remove.
console.log('Lecture Key: ' + LECTURE_KEY);

/** Returns database key id for lecture given a {@code url}. */
function getParam(url) {
  const urlParser = document.createElement('a');
  urlParser.href = url;
  return urlParser.search.substring(URL_ID_SPACING);
}
