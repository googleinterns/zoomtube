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
        addMultipleLinesToDom(transcriptLines);
    });
}

/**
 * Adds {@code comments} to the DOM as list elements.
 */
function addMultipleMessagesToDom(comments) {
  const commentContainer = document.getElementById(COMMENT_CONTAINER);

  // Removes the ul tag in the container if there is one. This prevents having
  // multiple sets of ul tags every time the number of comments is changed.
  if (commentContainer.firstChild) {
    commentContainer.removeChild(commentContainer.firstChild);
  }
  const ulElement = document.createElement('ul');
  commentContainer.appendChild(ulElement);

  comments.forEach((comment) => {
    appendTextToList(comment, ulElement);
  });
}

/**
 * Creates an <li> element containing {@code comment}'s text, timestamp,
 *  and submitter's name and appends it to {@code ulElement}.
 */
function appendTextToList(comment, ulElement) {
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