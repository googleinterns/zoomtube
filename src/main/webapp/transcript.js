
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
function loadTranscript(paramQueryString) {
  const params = new URLSearchParams(paramQueryString);
  fetch('/transcript', {method: 'POST', body: params})
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
 * Creates an <li> element containing {@code transcriptLine}'s text and
 * start time, and appends it to {@code ulElement}.
 */
function appendTextToList(transcriptLine, ulElement) {
  const liElement = document.createElement('li');
  const infoDivElement = document.createElement('div');

  appendPTagToContainer(transcriptLine.start, infoDivElement);
  liElement.appendChild(infoDivElement);
  appendPTagToContainer(transcriptLine.content, liElement);
  liElement.appendChild(document.createElement('hr'));
  ulElement.appendChild(liElement);
}

/**
 * Creates a <p> tag to store the given {@code text} inside the
 * {@code container} and returns the <p> tag using the given text
 *
 * <p>The <p> tag is returned so that the calling method can add additional
 * attributes to the tag.
 */
function appendPTagToContainer(text, container) {
  const pTag = document.createElement('p');
  pTag.innerText = text;
  container.appendChild(pTag);
  return pTag;
}
