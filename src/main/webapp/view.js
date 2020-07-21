/* Database ID for specific lecture. */
const LECTURE_ID = getLectureKey();
/* Video ID for specific lecture. */
const VIDEO_ID = getVideoId();

/* Used to control video. */
var videoPlayer;

loadApi();

/** Loads YouTube iFrame API. */
async function loadApi() {
  var videoApiScript = document.createElement('script');
  var firstScriptTag = document.getElementsByTagName('script')[0];
  videoApiScript.src = 'https://www.youtube.com/iframe_api';
  firstScriptTag.parentNode.insertBefore(videoApiScript, firstScriptTag);
}

/** Creates a YouTube Video iFrame playing video with {@code VIDEO_ID} after API is loaded. */
function onYouTubeIframeAPIReady() {
  videoPlayer = new YT.Player('player', {
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
  event.target.playVideo()
}

/** Returns database ID for lecture. */
function getLectureKey() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('id');
}

/** Returns video ID for lecture. */
function getVideoId() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('videoId');
}
