
const TRANSCRIPT_CONTAINER = 'transcript-container';

/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  // TODO: Update with user input.
  let t0 = performance.now();
  const testParamsString = 'id=123456789&video=8PrOp9t0PyQ';
  const params = new URLSearchParams(testParamsString);
  fetch('/transcript', {method: 'POST', body: params})
      .then(fetchTranscriptLines(testParamsString));
  let t1 = performance.now();
  console.log(t1 - t0);
  console.log('spt')
}

/**
 * Fetches the transcript lines from \transcript.
 *
 * <p>Video id to fetch the transcript from is
 * indicated in {@code testParamsString}.
 */
function fetchTranscriptLines(testParamsString) {
  let t0 = performance.now();
  fetch(
      '/transcript' +
      '?' + testParamsString)
      .then((response) => response.json())
      .then((transcriptLines) => {
        addMultipleTranscriptLinesToDom(transcriptLines);
      });
  let t1 = performance.now();
  console.log(t1 - t0);
  console.log('fetchtl');
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
 *  and submitter's name and appends it to {@code ulElement}.
 */
function appendTextToList(transcriptLine, ulElement) {
  const liElement = document.createElement('li');
  const startTime = (new Date(transcriptLine.start)).getSeconds();
  const endTime = (new Date(transcriptLine.end).getSeconds());
  const timestamp = startTime + ' secs - ' + endTime + ' secs';

  const timestampPElement = appendPTagToContainer(timestamp, liElement);
  liElement.classList.add('d-flex', 'flex-row', 'justify-content-between');
  timestampPElement.classList.add('mx-auto');
  const transcriptLinePElement =
      appendPTagToContainer(transcriptLine.content, liElement);
  transcriptLinePElement.classList.add('mx-auto');
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

/**
 * Sends a POST request to delete all of the transcript lines from datastore.
 */
function deleteTranscript() {
  const params = new URLSearchParams('');
  fetch('/delete-data', {method: 'POST', body: params});
}