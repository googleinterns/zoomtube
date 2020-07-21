/* Used to gather URL parameters. */
const URL_PARAM_ID = 'id';
const URL_PARAM_VIDEO_ID = 'video_id';

/* Database ID for specific lecture. */
const LECTURE_ID = getLectureKey();
/* Video ID for specific lecture. */
const VIDEO_ID = getVideoId();

/* TODO: Here for testing, remove. */
console.log(LECTURE_ID);
console.log(VIDEO_ID);

/* Used to control video. */
let videoPlayer;

loadApi();

/** Loads YouTube iFrame API. */
async function loadApi() {
  const videoApiScript = document.createElement('script');
  const firstScriptTag = document.getElementsByTagName('script')[0];
  videoApiScript.src = 'https://www.youtube.com/iframe_api';
  firstScriptTag.parentNode.insertBefore(videoApiScript, firstScriptTag);
}

/**
 * Creates a YouTube Video iFrame playing video with id:{@code VIDEO_ID} after
 * API is loaded.
 */
function onYouTubeIframeAPIReady() {
  videoPlayer = new window.YT.Player('player', {
    height: '390',
    width: '640',
    videoId: VIDEO_ID,
    events: {
      onReady: onPlayerReady,
    },
  });
}

/** Plays video. Called after API and iFrame load. */
function onPlayerReady(event) {
  event.target.playVideo();
}

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
