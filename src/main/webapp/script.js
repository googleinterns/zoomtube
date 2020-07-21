/**
 * Sends a POST request to the transcript
 */
function sendPOSTToTranscript() {
  const testParamsString = 'id=3ymwOvzhwHs&video=123456789'
  const params = new URLSearchParams(testParamsString);
  fetch('/transcript', {method: 'POST', body: params});
}