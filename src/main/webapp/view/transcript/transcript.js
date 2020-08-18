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
const URL_PARAM_ID = 'id';
const TRANSCRIPT_SLOT_TIME_RANGE = 'timestamp-range';
const TRANSCRIPT_SLOT_CONTENT = 'content';
const CUSTOM_ELEMENT_TRANSCRIPT_LINE = 'transcript-line';

let /** Element */ currentTranscriptLine;
//TODO: Create an instance reference to the current transcriptLine when 
// the code for seeking the transcript is refactored into a class.

/**
 * Fetches the transcript lines from `ENDPOINT_TRANSCRIPT`.
 *
 * <p>This function assumes that the transcript lines have already
 * been added to the datastore.
 */
function loadTranscript() {
  const url = new URL(ENDPOINT_TRANSCRIPT, window.location.origin);
  url.searchParams.append(URL_PARAM_ID, window.LECTURE_ID);
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
    ulElement.appendChild(new TranscriptLine(transcriptLine));
  });
}

/**
 * Sends a POST request to delete all of the transcript lines from datastore.
 */
function deleteTranscript() {
  fetch('/delete-transcript', {method: 'POST'});
}

/** Seeks transcript to `currentTime`, which is given in seconds. */
function seekTranscript(currentTime) {
  const currentTimeMs = window.secondsToMilliseconds(currentTime);
  if (currentTimeMs < currentTranscriptLine.startTimestampMs) {
    return;
  }
  if (currentTranscriptLine.isWithinTimeRange(currentTimeMs)) {
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
 * start time, and end time.
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
    this.updateTemplateSlot(TRANSCRIPT_SLOT_TIME_RANGE, timestampRange);
    this.updateTemplateSlot(TRANSCRIPT_SLOT_CONTENT, transcriptLine.content);

    this.transcriptLine = transcriptLine;
  }

  /**
   * Updates the template slot `slotName` with `slotValue`.
   */
  updateTemplateSlot(slotName, slotValue) {
    const span = document.createElement('span');
    span.innerText = slotValue;
    span.slot = slotName;
    this.appendChild(span);
  }

  /** Returns true if the element is already bolded. */
  isBolded() {
    return this.classList.contains(BOLD_FONT_WEIGHT);
  }

  /**
   * Bolds the element if it is not already
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
   * Removes bold from the element if it
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
   * Returns true if `timeMs` is within the time range for
   * this transcript line.
   */
  isWithinTimeRange(timeMs) {
    return this.transcriptLine.startTimestampMs <= timeMs &&
    timeMs <= this.transcriptLine.endTimestampMs;
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

customElements.define(CUSTOM_ELEMENT_TRANSCRIPT_LINE, TranscriptLine);
