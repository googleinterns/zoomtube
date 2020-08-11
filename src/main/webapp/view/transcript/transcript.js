
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

const TRANSCRIPT_CONTAINER = 'transcript-lines-container';
const ENDPOINT_TRANSCRIPT = '/transcript';
const DEFAULT_FONT_WEIGHT = 'text-muted';
const BOLD_FONT_WEIGHT = 'font-weight-bold';

/**
 * Sends a POST request to the transcript.
 */
// TODO: Delete this method once TranscriptServlet's doPost()
// is called in LectureServlet.
function sendPostToTranscript(lectureQueryString) {
  const params = new URLSearchParams(lectureQueryString);
  fetch('/transcript', {method: 'POST', body: params});
}

/**
 * Fetches the transcript lines from {@code ENDPOINT_TRANSCRIPT}.
 *
 * <p>This function assumes that the transcript lines have already
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
  ulElement.class = 'mx-auto';
  transcriptContainer.appendChild(ulElement);

  transcriptLines.forEach((transcriptLine) => {
    appendTextToList(transcriptLine, ulElement);
  });
}

/**
 * Creates an <li> element containing {@code transcriptLine}'s text, start
 * time, and end time and appends it to {@code ulElement}.
 */
function appendTextToList(transcriptLine, ulElement) {
  const startTimestamp = window.timestampToString(transcriptLine.start);
  const endTimestamp = window.timestampToString(transcriptLine.end);
  const timestamp = `${startTimestamp} - ${endTimestamp}`;

  const contentDivElement = document.createElement('div');
  contentDivElement.classList.add('d-flex', 'flex-row', 'mb-1');
  appendParagraphToContainer(
      timestamp, contentDivElement, ['justify-content-start', 'mb-1']);
  appendParagraphToContainer(
      transcriptLine.content, contentDivElement, ['ml-4', 'mb-1']);

  const liElement = document.createElement('li');
  liElement.classList.add('align-self-center', 'mb-2');
  liElement.appendChild(contentDivElement);
  const hrElement = document.createElement('hr');
  hrElement.classList.add('my-1', 'align-middle', 'mr-5');
  liElement.appendChild(hrElement);
  ulElement.appendChild(liElement);
  // Save the dates for seeking later.
  liElement.startDate = new Date(transcriptLine.start);
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

/** Seeks transcript to {@code currentTime}. */
function seekTranscript(currentTime) {
  // TODO: Remove and implement.
  console.log('SEEKING TRANSCRIPT TO: ' + currentTime);
}

/** Bolds the text in `transcriptLineLiElement` */
function addBold(transcriptLineLiElement) {
  transcriptLineLiElement.classList.add(BOLD_FONT_WEIGHT);
  // if the class is not in the list, nothing will happen
  // and there will not be an error.
  transcriptLineLiElement.classList.remove(DEFAULT_FONT_WEIGHT);
}

/** Removes bold from the text in `transcriptLineLiElement` */
function removeBold(transcriptLineLiElement) {
  transcriptLineLiElement.classList.add(DEFAULT_FONT_WEIGHT);
  transcriptLineLiElement.classList.remove(BOLD_FONT_WEIGHT);
}
