/* Database key for specific lecture. */
const LECTURE_KEY = getLectureKey();

// TODO: Here for testing, remove.
console.log('Lecture Key: ' + LECTURE_KEY);

/** Returns database key id for lecture. */
function getLectureKey() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('key');
}
