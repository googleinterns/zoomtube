/**
 * Sends a POST request to the transcript.
 */
function loadTranscript() {
  const params = new URLSearchParams(window.location.search);
  console.log(params);
  fetch('/transcript', {method: 'POST', body: params});
}
