
const TRANSCRIPT_CONTAINER = 'transcript-container';

/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  // TODO: Update with user input.
  const paramsString = 'id=123456789&video=3ymwOvzhwHs';
  const params = new URLSearchParams(paramsString);
  fetch('/transcript', {method: 'POST', body: params})
      .then(fetchTranscriptLines(paramsString));
}

/**
 * Fetches the transcript lines from \transcript.
 *
 * <p>{@code paramsString} indicates the video ID
 * to fetch the transcript from.
 */
function fetchTranscriptLines(paramsString) {
  fetch(
      '/transcript' +
      '?' + paramsString)
      .then((response) => response.json())
      .then((transcriptLines) => {
        addMultipleTranscriptLinesToDom(transcriptLines);
      });
}

/**
 * Adds {@code transcriptLines} to the DOM as list elements.
 */
function addMultipleTranscriptLinesToDom(transcriptLines) {
  const transcriptContainer = document.getElementById(TRANSCRIPT_CONTAINER);
  if (transcriptContainer.firstChild) {
    transcriptContainer.removeChild(transcriptContainer.firstChild);
  }
  const ulElement = document.createElement('ul');
  transcriptContainer.appendChild(ulElement);

  transcriptLines.forEach((transcriptLine) => {
    appendTextToList(transcriptLine, ulElement);
  });
}

/**
 * Creates an <li> element containing {@code transcriptLine}'s text, start time,
 * and end time and appends it to {@code ulElement}.
 */
function appendTextToList(transcriptLine, ulElement) {
  const liElement = document.createElement('li');
  const startTime = (new Date(transcriptLine.start)).getSeconds();
  const endTime = (new Date(transcriptLine.end).getSeconds());
  const timestamp = `${startTime} secs - ${endTime} secs`;

  const timestampPElement = appendPTagToContainer(timestamp, liElement);
  liElement.classList.add('d-flex', 'flex-row', 'justify-content-between');
  timestampPElement.classList.add('mx-auto');
  const transcriptLinePElement =
      appendPTagToContainer(transcriptLine.content, liElement);
  transcriptLinePElement.classList.add('mx-auto');
  liElement.appendChild(document.createElement('hr'));
  ulElement.appendChild(liElement);
}

/**
 * Creates a <p> tag to store the given {@code text} inside the
 * {@code container} and returns the <p> tag using the given text.
 */
function appendPTagToContainer(text, container) {
  const pTag = document.createElement('p');
  pTag.innerText = text;
  container.appendChild(pTag);
  return pTag;
}

/**
 * Sends a POST request to delete all of the transcript lines from datastore.
 */
// TODO: Delete this method once I no longer need to delete all of the
// transcripts for testing.
function deleteTranscript() {
  const params = new URLSearchParams('');
  fetch('/delete-transcript', {method: 'POST', body: params});
}
