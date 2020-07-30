
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

const TRANSCRIPT_CONTAINER = 'transcript-container';
const ENDPOINT_TRANSCRIPT = '/transcript';
const MILLISECOND_CONVERTER = 1000;

/**
 * Sends a POST request to the transcript.
 */
function sendPostToTranscript() {
  // TODO: Update lectureQueryString with user input and URL Builder.
  const lectureQueryString = 'id=123456789&video=3ymwOvzhwHs';
  const params = new URLSearchParams(lectureQueryString);
  fetch(ENDPOINT_TRANSCRIPT, {method: 'POST', body: params})
      .then(fetchTranscriptLines(lectureQueryString));
}

/**
 * Fetches the transcript lines from {@code ENDPOINT_TRANSCRIPT}.
 *
 * <p>{@code lectureQueryString} indicates the video ID and the lecture ID
 * to fetch the transcript from.
 */
function fetchTranscriptLines(lectureQueryString) {
  fetch(ENDPOINT_TRANSCRIPT + '?' + lectureQueryString)
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
  const startDate = new Date(transcriptLine.start);
  const endDate = new Date(transcriptLine.end);
  const startTimestamp = `${startDate.getHours()}:${startDate.getMinutes()}:${
    startDate.getSeconds()}`;
  const endTimestamp =
    `${endDate.getHours()}:${endDate.getMinutes()}:${endDate.getSeconds()}`;
  const timestamp = `${startTimestamp} - ${endTimestamp}`;

  const timestampPElement = appendParagraphToContainer(timestamp, liElement);
  liElement.classList.add('d-flex', 'flex-row', 'justify-content-between');
  timestampPElement.classList.add('mx-auto');
  const transcriptLinePElement =
      appendParagraphToContainer(transcriptLine.content, liElement);
  transcriptLinePElement.classList.add('mx-auto');
  liElement.appendChild(document.createElement('hr'));
  ulElement.appendChild(liElement);

  liElement.startDate = startDate;
  liElement.endDate = endDate;
}

/**
 * Creates a <p> tag to store the given {@code text} inside the
 * {@code container} and returns the <p> tag using the given text
 * 
 * <p>The <p> tag is returned so that the calling method can add additional
 * attributes to the tag.
 */
function appendParagraphToContainer(text, container) {
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
