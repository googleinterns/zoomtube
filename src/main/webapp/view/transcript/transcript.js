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

import {secondsToMilliseconds, timestampRangeToString} from '../../timestamps.js';

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
// TODO: Create an instance reference to currentTranscriptLine when
// the code for seeking the transcript is refactored into a class.

/**
 * Fetches the transcript lines from `ENDPOINT_TRANSCRIPT`.
 *
 * <p>This function assumes that the transcript lines have already
 * been added to the datastore.
 */
export function loadTranscript() {
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
  console.log(transcriptLines);
  console.log(transcriptLines[0]);
  transcriptLines.forEach((transcriptLine) => {
    ulElement.appendChild(
        TranscriptLineElement.createTranscriptLineElement(transcriptLine));
  });
}

/**
 * Sends a POST request to delete all of the transcript lines from datastore.
 */
export function deleteTranscript() {
  fetch('/delete-transcript', {method: 'POST'});
}

/** Seeks transcript to `currentTime`, which is given in seconds. */
export function seekTranscript(currentTime) {
  if (currentTranscriptLine == null) {
    currentTranscriptLine = document.getElementsByTagName('transcript-line')[0];
  }
  const currentTimeMs = secondsToMilliseconds(currentTime);
  if (currentTimeMs < currentTranscriptLine.transcriptLine.startTimestampMs) {
    return;
  }
  if (currentTranscriptLine.isWithinTimeRange(currentTimeMs)) {
    currentTranscriptLine.addBold();
    return;
  }
  currentTranscriptLine.removeBold();
  currentTranscriptLine = currentTranscriptLine.nextElementSibling;
  scrollToTopOfTranscript(currentTranscriptLine);
  currentTranscriptLine.addBold();
  // TODO: Handle the case where the video isn't only playing.
}

/**
 * Scrolls 'transcriptLineElement` to the top of the transcript area.
 */
// TODO: Make this function static and move it into the
// class that seeks the transcript.
function scrollToTopOfTranscript(transcriptLineElement) {
  const transcriptContainer = document.getElementById(TRANSCRIPT_CONTAINER);
  const ulElementOffset = transcriptLineElement.parentElement.offsetTop;
  transcriptContainer.scrollTop =
      transcriptLineElement.offsetTop - ulElementOffset;
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
 * Creates a transcript line element containing the text,
 * start time, and end time.
 */
class TranscriptLineElement extends HTMLElement {
  /**
   * Creates a custom HTML element representing `transcriptLine`.
   *
   * @param timestampRange The timestamp for the transcript line.
   * @param transcriptLine The transcriptLine from `ENDPOINT_TRANSCRIPT`
   *     whose `attributes` should be used.
   */
  constructor(timestampRange, transcriptLine) {
    super();
    const contentDivElement = this.createContentDivElement();
    appendParagraphToContainer(
        timestampRange, contentDivElement, ['justify-content-start', 'mb-1']);
    appendParagraphToContainer(
        transcriptLine.content, contentDivElement, ['ml-4', 'mb-1']);
    this.classList.add('align-self-center', 'mb-2');
    this.appendChild(contentDivElement);
    const hrElement = this.createHrElement();
    this.appendChild(hrElement);
    this.transcriptLine = transcriptLine;
  }

  /**
   * Creates a custom HTML element representing `transcriptLine` with
   * the text and time range appended to the element.
   *
   * @param transcriptLine The transcriptLine from `ENDPOINT_TRANSCRIPT`
   *     whose `attributes` should be used.
   */
  static createTranscriptLineElement(transcriptLine) {
    const timestampRange = timestampRangeToString(
        transcriptLine.startTimestampMs, transcriptLine.endTimestampMs);
    const transcriptLineElement =
        new TranscriptLineElement(timestampRange, transcriptLine);
    return transcriptLineElement;
  }

  /**
   * Creates a stylized div element that will be used to store
   * the time range and text in a `TranscriptLineElement`.
   */
  createContentDivElement() {
    const contentDivElement = document.createElement('div');
    contentDivElement.classList.add('d-flex', 'flex-row', 'mb-1');
    return contentDivElement;
  }

  /**
   * Creates a stylized hr element that will be used to create a
   * `TranscriptLineElement`.
   */
  createHrElement() {
    const hrElement = document.createElement('hr');
    hrElement.classList.add('my-1', 'align-middle', 'mr-5');
    return hrElement;
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
   * Bolds the element if it is not already bolded.
   */
  addBold() {
    if (this.isBolded()) {
      return;
    }
    this.classList.add(BOLD_FONT_WEIGHT);
    this.classList.remove(DEFAULT_FONT_WEIGHT);
  }

  /**
   * Removes bold from the element if it is currently bolded.
   */
  removeBold() {
    if (!this.isBolded()) {
      return;
    }
    this.classList.add(DEFAULT_FONT_WEIGHT);
    this.classList.remove(BOLD_FONT_WEIGHT);
  }

  /**
   * Returns true if `timestampMs` is within the time range for
   * this transcript line.
   */
  isWithinTimeRange(timestampMs) {
    return this.transcriptLine.startTimestampMs <= timestampMs &&
        timestampMs <= this.transcriptLine.endTimestampMs;
  }
}

customElements.define(CUSTOM_ELEMENT_TRANSCRIPT_LINE, TranscriptLineElement);
