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

let /** Element */ currentTranscriptLine;

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
    // appendTextToList(transcriptLine, ulElement);
    ulElement.appendChild(new TranscriptLine(transcriptLine));
  });
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

/** Seeks transcript to {@code currentTime}, which is given in seconds. */
function seekTranscript(currentTime) {
  const currentTimeMs = window.secondsToMilliseconds(currentTime);
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
 * Creates an <li> element containing {@code transcriptLine}'s text, start
 * time, and end time and appends it to {@code ulElement}.
 */
class TranscriptLine extends HTMLLIElement {
  constructor(transcriptLine) {
    super();
    // TODO: Refactor creation of DOM elements to use slots instead.
    const timestampRange = window.createTimestampRange(
        transcriptLine.startTimestampMs, transcriptLine.endTimestampMs);
    const contentDivElement = this.createStyledDivElement();
    appendParagraphToContainer(
        timestampRange, contentDivElement,
        /* classes= */['justify-content-start', 'mb-1']);
    appendParagraphToContainer(
        transcriptLine.content, contentDivElement,
        /* classes= */['ml-4', 'mb-1']);
    this.classList.add('align-self-center', 'mb-2');
    this.appendChild(contentDivElement);
    const hrElement = this.createStyledHrElement();
    this.appendChild(hrElement);
    // TODO: Update to use number once pull request #168 is merged.
    this.startDate = new Date(transcriptLine.start);
    this.endDate = new Date(transcriptLine.end);
    // Sets the current transcript line to be the first line.
    if (currentTranscriptLine == null) {
      currentTranscriptLine = this;
    }
  }

/**
 * Bolds the text if it is not already
 * bolded.
 */
  addBold() {
    if (this.isBolded()) {
      return;
    }
    this.classList.add(BOLD_FONT_WEIGHT);
    this.classList.remove(DEFAULT_FONT_WEIGHT);
  }

  /**
 * Removes bold from the text in `transcriptLineLiElement` if it
 * is currently bolded.
 */
  removeBold() {
    if (!this.isBolded()) {
      return;
    }
    this.classList.add(DEFAULT_FONT_WEIGHT);
    this.classList.remove(BOLD_FONT_WEIGHT);
  }

  /** Returns true if bolded. */
  isBolded() {
    return this.classList.contains(BOLD_FONT_WEIGHT);
  }

  /**
 * Returns true if `currentTimeMs` is within the time range for
 * this transcript line.
 */
  isWithinCurrentTimeRange(currentTimeMs) {
    return this.startTimestampMs <= currentTimeMs &&
      currentTimeMs <= this.endTimestampMs;
  }

  /**
   * Scrolls this transcript line to the top of the transcript area.
   *
   */
  scrollToTopOfTranscript() {
    const transcriptContainer = document.getElementById(TRANSCRIPT_CONTAINER);
    const ulElementOffset = this.parentElement.offsetTop;
    transcriptContainer.scrollTop = this.offsetTop - ulElementOffset;
  }
}

customElements.define('transcript-line', TranscriptLine, {extends: 'li'});
