/* Database key for specific lecture. */
const LECTURE_KEY = getLectureKey();

// TODO: Here for testing, remove.
console.log('Lecture Key: ' + LECTURE_KEY);

/** Returns database key id for lecture given a {@code url}. */
function getLectureKey(url) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('key');
}
