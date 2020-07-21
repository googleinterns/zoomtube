/* Database ID for specific lecture. */
const LECTURE_ID = getLectureKey();

// TODO: Here for testing, remove.
console.log('Lecture ID: ' + LECTURE_ID);

/** Returns database ID for lecture. */
function getLectureKey() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('id');
}
