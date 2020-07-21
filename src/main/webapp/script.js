/**
 * Sends a POST request to the transcript.
 */
function sendPOSTToTranscript() {
  // TODO: Update with user input.
  const testParamsString = 'id=123456789&video=3ymwOvzhwHs';
  const params = new URLSearchParams(testParamsString);
  fetch('/transcript', {method: 'POST', body: params});
}