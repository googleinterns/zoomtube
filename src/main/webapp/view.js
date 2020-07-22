// TODO: Fetch video-id from servlet rather than URL.

/* Used to gather URL parameters. */
const URL_PARAM_ID = 'id';
const URL_PARAM_VIDEO_ID = 'video-id';

/* Database ID for specific lecture. */
window.LECTURE_ID = getLectureKey();
/* Video ID for specific lecture. */
window.VIDEO_ID = getVideoId();

/* TODO: Here for testing, remove. */
console.log(window.LECTURE_ID);
console.log(window.VIDEO_ID);

/** Returns database ID for lecture. */
function getLectureKey() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(URL_PARAM_ID);
}

/** Returns video ID for lecture. */
function getVideoId() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(URL_PARAM_VIDEO_ID);
}
