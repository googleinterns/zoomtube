/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  const params = new URLSearchParams(window.location.search);
  fetch('/transcript', {method: 'POST', body: params});
}
