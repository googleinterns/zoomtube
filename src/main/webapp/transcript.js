/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  // TODO: Update with user input (window.location.search)
  const testParamsString = 'Obgnr9pc820';
  const params = new URLSearchParams(testParamsString);
  fetch('/transcript', {method: 'POST', body: params});
}
