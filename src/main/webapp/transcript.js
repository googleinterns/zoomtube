/**
 * Sends a POST request to the transcript.
 */
function loadTranscript(paramQueryString) {
  const params = new URLSearchParams(paramQueryString);
  fetch('/transcript', {method: 'POST', body: params});
}
