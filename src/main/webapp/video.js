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
    videoId: window.VIDEO_ID,
    events: {
      onReady: onPlayerReady,
    },
  });
}

/** Plays video. Called after API and iFrame load. */
function onPlayerReady(event) {
  event.target.playVideo();
}
