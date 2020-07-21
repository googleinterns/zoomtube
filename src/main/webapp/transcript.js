
const TRANSCRIPT_CONTAINER = "transcript-container";

/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  // TODO: Update with user input.
  const testParamsString = 'id=123456789&video=3ymwOvzhwHs';
  const params = new URLSearchParams(testParamsString);
  fetch('/transcript', {method: 'POST', body: params})
    .then(response => response.json())
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
 *  and submitter's name and appends it to {@code ulElement}.
 */
function appendTextToList(transcriptLine, ulElement) {
  const liElement = document.createElement('li');

  const infoDivElement = document.createElement('div');
  infoDivElement.className = INFO_CLASS;
  
  const date = (new Date(comment.timestamp)).toString()
      .substring(0, END_OF_TIMESTAMP);

  appendPTagToContainer(comment.nickname, infoDivElement);
  appendPTagToContainer("Feeling " + comment.mood, infoDivElement);
  appendPTagToContainer(date, infoDivElement);

  liElement.appendChild(infoDivElement);
  const textPElement = appendPTagToContainer(comment.text, liElement);
  liElement.appendChild(createCommentImage(comment.imageUrl));
  // Separates each comment with a horizontal bar.
  liElement.appendChild(document.createElement('hr'));
  textPElement.className = COMMENT_CLASS;
  ulElement.appendChild(liElement);
}