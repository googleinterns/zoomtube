
const TRANSCRIPT_CONTAINER = 'transcript-container';

/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  // TODO: Update with user input.
  const testParamsString = 'id=123456789&video=3ymwOvzhwHs';
  const params = new URLSearchParams(testParamsString);
  fetch('/transcript', {method: 'POST', body: params})
      .then(fetchTranscriptLines(testParamsString));
}

/**
 * Fetches the transcript lines from \transcript.
 *
 * <p>Video id to fetch the transcript from is
 * indicated in {@code testParamsString}.
 */
function fetchTranscriptLines(testParamsString) {
  fetch(
      '/transcript' +
      '?' + testParamsString)
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
 * and appends it to {@code ulElement}.
 */
function appendTextToList(transcriptLine, ulElement) {
  const liElement = document.createElement('li');
  const infoDivElement = document.createElement('div');

  appendPTagToContainer(transcriptLine.start, infoDivElement);
  liElement.appendChild(infoDivElement);
  appendPTagToContainer(transcriptLine.content, liElement);
  // Separates each comment with a horizontal bar.
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
