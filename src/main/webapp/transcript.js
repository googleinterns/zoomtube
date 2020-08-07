
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

/**
 * Sends a POST request to the transcript.
 */
// TODO: Delete this method once TranscriptServlet's doPost()
// is called in LectureServlet.
function sendPostToTranscript(lectureQueryString) {
  const params = new URLSearchParams(lectureQueryString);
  fetch('/transcript', {method: 'POST', body: params})
}

/**
 * Fetches the transcript lines from {@code ENDPOINT_TRANSCRIPT}.
 *
 * <p> This function assumes that the transcript lines have already
 * been added to the datastore.
 *
 * @param lectureQueryString Indicates the video ID and the lecture ID
 * to fetch the transcript from.
 */
function loadTranscript(lectureQueryString) {
  fetch(ENDPOINT_TRANSCRIPT + lectureQueryString)
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

  appendParagraphToContainer(timestamp, liElement, ['mx-auto']);
  liElement.classList.add('d-flex', 'flex-row', 'justify-content-between');
  appendParagraphToContainer(transcriptLine.content, liElement, ['mx-auto']);
  liElement.appendChild(document.createElement('hr'));
  ulElement.appendChild(liElement);

  liElement.startDate = startDate;
  liElement.endDate = endDate;
}

/**
 * Creates a p tag to store the given {@code text} inside the
 * {@code container}.
 *
 * <p>Adds classes the the p tag if {@code classList} is provided.
 */
function appendParagraphToContainer(text, container, classes = []) {
  const pTag = document.createElement('p');
  pTag.innerText = text;
  container.appendChild(pTag);

  if (classes.length == 0) {
    return;
  }
  pTag.classList.add(...classes);
}

/**
 * Sends a POST request to delete all of the transcript lines from datastore.
 */
function deleteTranscript() {
  fetch('/delete-transcript', {method: 'POST'});
}
