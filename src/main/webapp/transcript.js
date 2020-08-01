/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  // TODO: Update with user input (window.location.search)
  const testParamsString = 'id=123456789&video=Obgnr9pc820';
  const params = new URLSearchParams(testParamsString);
  fetch('/transcript', {method: 'POST', body: params});
}
