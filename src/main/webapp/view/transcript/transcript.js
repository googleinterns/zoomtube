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
const TRANSCRIPT_TEMPLATE = 'transcript-line-template';
const ENDPOINT_TRANSCRIPT = '/transcript';
const DEFAULT_FONT_WEIGHT = 'text-muted';
const BOLD_FONT_WEIGHT = 'font-weight-bold';
const STYLE_TIME_RANGE = 'justify-content-start mb-1';
const STYLE_CONTENT = 'ml-4 mb-1';

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
  if (currentTranscriptLine.isWithinCurrentTimeRange(currentTimeMs)) {
    currentTranscriptLine.addBold();
    return;
  }
  currentTranscriptLine.removeBold();
  currentTranscriptLine = currentTranscriptLine.nextElementSibling;
  currentTranscriptLine.scrollToTopOfTranscript();
  currentTranscriptLine.addBold();
  // TODO: Handle the case where the video isn't only playing.
}

/**
 * Creates a transcript line element containing the text,
 * start time, and end time from `transcriptLine`.
 */
class TranscriptLine extends HTMLElement {
  /**
   * Creates a custom HTML element representing a transcript line.
   *
   * <p>Uses the template and slots defined in `TRANSCRIPT_TEMPLATE` to
   * help create the transcript line.
   *
   * @param transcriptLine The transcriptLine from `ENDPOINT_TRANSCRIPT`
   *     whose `attributes` should be used.
   */
  constructor(transcriptLine) {
    super();
    const timestampRange = window.createTimestampRange(
        transcriptLine.startTimestampMs, transcriptLine.endTimestampMs);

    const template = document.getElementById(TRANSCRIPT_TEMPLATE);
    this.attachShadow({mode: 'open'});
    this.shadowRoot.appendChild(template.content.cloneNode(true));
    this.updateSlot('timestamp-range', timestampRange, STYLE_TIME_RANGE);
    this.updateSlot('content', transcriptLine.content, STYLE_CONTENT);

    this.startTimestampMs = transcriptLine.startTimestampMs;
    this.endTimestampMs = transcriptLine.endTimestampMs;
    // Sets the current transcript line to be the first line.
    if (currentTranscriptLine == null) {
      currentTranscriptLine = this;
    }
  }

  /**
   * Updates the template slot `slotName` with `slotValue` and styling
   * from `slotStyle`.
   */
  updateSlot(slotName, slotValue, slotStyle) {
    const span = document.createElement('span');
    span.innerText = slotValue;
    span.slot = slotName;
    span.className = slotStyle;
    this.appendChild(span);
  }

  /** Returns true if the text is already bolded. */
  isBolded() {
    return this.classList.contains(BOLD_FONT_WEIGHT);
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
   * Removes bold from the text if it
   * is currently bolded.
   */
  removeBold() {
    if (!this.isBolded()) {
      return;
    }
    this.classList.add(DEFAULT_FONT_WEIGHT);
    this.classList.remove(BOLD_FONT_WEIGHT);
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
   */
  scrollToTopOfTranscript() {
    const transcriptContainer = document.getElementById(TRANSCRIPT_CONTAINER);
    const ulElementOffset = this.parentElement.offsetTop;
    transcriptContainer.scrollTop = this.offsetTop - ulElementOffset;
  }
}

customElements.define('transcript-line', TranscriptLine);
