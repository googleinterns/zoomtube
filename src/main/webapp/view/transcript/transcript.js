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

import {timestampToString} from '../../timestamps.js';

const TRANSCRIPT_CONTAINER = 'transcript-lines-container';
const ENDPOINT_TRANSCRIPT = '/transcript';
const DEFAULT_FONT_WEIGHT = 'text-muted';
const BOLD_FONT_WEIGHT = 'font-weight-bold';
const PARAM_ID = 'id';

let /** Element */ currentTranscriptLine;

/**
 * Fetches the transcript lines from `ENDPOINT_TRANSCRIPT`.
 *
 * <p>This function assumes that the transcript lines have already
 * been added to the datastore.
 */
export function loadTranscript() {
  const url = new URL(ENDPOINT_TRANSCRIPT, window.location.origin);
  url.searchParams.append(PARAM_ID, window.LECTURE_ID);
  fetch(url).then((response) => response.json()).then((transcriptLines) => {
    addMultipleTranscriptLinesToDom(transcriptLines);
  });
}

/**
 * Adds `transcriptLines` to the DOM as list elements.
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
 * Creates an `<li>` element containing `transcriptLine`'s text, start
 * time, and end time and appends it to `ulElement`.
 */
function appendTextToList(transcriptLine, ulElement) {
  const startTimestamp = timestampToString(transcriptLine.startTimestampMs);
  const endTimestamp = timestampToString(transcriptLine.endTimestampMs);
  const timestamp = `${startTimestamp} - ${endTimestamp}`;

  const contentDivElement = document.createElement('div');
  contentDivElement.classList.add('d-flex', 'flex-row', 'mb-1');
  appendParagraphToContainer(
      timestamp, contentDivElement, ['justify-content-start', 'mb-1']);
  appendParagraphToContainer(
      transcriptLine.content, contentDivElement, ['ml-4', 'mb-1']);

  const liElement = document.createElement('li');
  liElement.classList.add('align-self-center', 'mb-2', DEFAULT_FONT_WEIGHT);
  liElement.appendChild(contentDivElement);
  const hrElement = document.createElement('hr');
  hrElement.classList.add('my-1', 'align-middle', 'mr-5');
  liElement.appendChild(hrElement);
  ulElement.appendChild(liElement);
  liElement.startTimestampMs = transcriptLine.startTimestampMs;
  liElement.endTimestampMs = transcriptLine.endTimestampMs;
  // Sets the current transcript line to be the first line.
  if (currentTranscriptLine == null) {
    currentTranscriptLine = liElement;
  }
}

/**
 * Creates a p tag to store the given `text` inside the
 * `container`.
 *
 * <p>Adds classes the the p tag if `classList` is provided.
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
export function deleteTranscript() {
  fetch('/delete-transcript', {method: 'POST'});
}

/** Seeks transcript to `currentTimeMs`. */
export function seekTranscript(currentTimeMs) {
  if (currentTimeMs < currentTranscriptLine.startTimestampMs) {
    return;
  }
  if (isWithinCurrentTimeRange(currentTimeMs)) {
    addBold(currentTranscriptLine);
    return;
  }
  removeBold(currentTranscriptLine);
  currentTranscriptLine = currentTranscriptLine.nextElementSibling;
  scrollToTopOfTranscript(currentTranscriptLine);
  addBold(currentTranscriptLine);
  // TODO: Handle the case where the video isn't only playing.
}

/**
 * Bolds the text in `transcriptLineLiElement` if it is not already
 * bolded.
 */
function addBold(transcriptLineLiElement) {
  if (isBolded(transcriptLineLiElement)) {
    return;
  }
  transcriptLineLiElement.classList.add(BOLD_FONT_WEIGHT);
  transcriptLineLiElement.classList.remove(DEFAULT_FONT_WEIGHT);
}

/**
 * Removes bold from the text in `transcriptLineLiElement` if it
 * is currently bolded.
 */
function removeBold(transcriptLineLiElement) {
  if (!isBolded(transcriptLineLiElement)) {
    return;
  }
  transcriptLineLiElement.classList.add(DEFAULT_FONT_WEIGHT);
  transcriptLineLiElement.classList.remove(BOLD_FONT_WEIGHT);
}

/** Returns true if`transcriptLineLiElement` is bolded. */
function isBolded(transcriptLineLiElement) {
  return transcriptLineLiElement.classList.contains(BOLD_FONT_WEIGHT);
}

/**
 * Returns true if `currentTimeMs` is within the time range for
 * the current transcript line.
 */
function isWithinCurrentTimeRange(currentTimeMs) {
  return currentTranscriptLine.startTimestampMs <= currentTimeMs &&
      currentTimeMs <= currentTranscriptLine.endTimestampMs;
}

/**
 * Scrolls `transcriptLine` to the top of the transcript area.
 * */
function scrollToTopOfTranscript(transcriptLine) {
  const transcriptContainer = document.getElementById(TRANSCRIPT_CONTAINER);
  const ulElementOffset = transcriptLine.parentElement.offsetTop;
  transcriptContainer.scrollTop = transcriptLine.offsetTop - ulElementOffset;
}
