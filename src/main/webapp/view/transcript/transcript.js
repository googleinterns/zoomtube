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

import {timestampRangeToString} from '../../timestamps.js';

const TRANSCRIPT_CONTAINER = 'transcript-lines-container';
const DEFAULT_FONT_WEIGHT = 'text-muted';
const BOLD_FONT_WEIGHT = 'font-weight-bold';
const TRANSCRIPT_TEMPLATE = 'transcript-line-template';
const TRANSCRIPT_SLOT_TIME_RANGE = 'timestamp-range';
const TRANSCRIPT_SLOT_CONTENT = 'content';
const CUSTOM_ELEMENT_TRANSCRIPT_LINE = 'transcript-line';

let /** Element */ currentTranscriptLine;
// TODO: Create an instance reference to currentTranscriptLine when
// the code for seeking the transcript is refactored into a class.

/**
 * Sends a POST request to delete all of the transcript lines from datastore.
 */
export function deleteTranscript() {
  fetch('/delete-transcript', {method: 'POST'});
}

/** Seeks transcript to `timeMs`. */
export function seekTranscript(timeMs) {
  if (currentTranscriptLine == null) {
    currentTranscriptLine = document.getElementsByTagName('transcript-line')[0];
  }
  if (timeMs < currentTranscriptLine.transcriptLine.startTimestampMs) {
    return;
  }
  if (currentTranscriptLine.isWithinTimeRange(timeMs)) {
    currentTranscriptLine.addBold();
    return;
  }
  currentTranscriptLine.removeBold();
  currentTranscriptLine = transcriptLineWithTime(timeMs);
  scrollToTopOfTranscript(currentTranscriptLine);
  currentTranscriptLine.addBold();
  // TODO: Handle the case where the video isn't only playing.
  // TODO: Check if currentTranscriptLine is within the time range. If
  // it isn't, do not bold the transcript line.
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
 * Creates a transcript line element containing the text,
 * start time, and end time.
 */
export class TranscriptLineElement extends HTMLElement {
  /**
   * Creates a custom HTML element representing `transcriptLine`.
   *
   * @param template The template that will be cloned to create the
   *     transcript line.
   * @param transcriptLine The transcriptLine from `ENDPOINT_TRANSCRIPT`
   *     whose `attributes` should be used.
   */
  constructor(template, transcriptLine) {
    super();
    this.attachShadow({mode: 'open'});
    this.shadowRoot.appendChild(template.content.cloneNode(true));
    this.transcriptLine = transcriptLine;
  }

  /**
   * Creates a custom HTML element representing `transcriptLine` with
   * the text and time range appended to the element.
   *
   * <p>Uses the template and slots defined in `TRANSCRIPT_TEMPLATE` to
   * help create the transcript line.
   *
   * @param transcriptLine The transcriptLine from `ENDPOINT_TRANSCRIPT`
   *     whose `attributes` should be used.
   */
  static createTranscriptLineElement(transcriptLine) {
    const template = document.getElementById(TRANSCRIPT_TEMPLATE);
    const transcriptLineElement =
        new TranscriptLineElement(template, transcriptLine);
    const timestampRange = timestampRangeToString(
        transcriptLine.startTimestampMs, transcriptLine.endTimestampMs);
    transcriptLineElement.updateTemplateSlot(
        TRANSCRIPT_SLOT_TIME_RANGE, timestampRange);
    transcriptLineElement.updateTemplateSlot(
        TRANSCRIPT_SLOT_CONTENT, transcriptLine.content);
    return transcriptLineElement;
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

  /** Returns true if this element is bolded. */
  isBolded() {
    return this.classList.contains(BOLD_FONT_WEIGHT);
  }

  /**
   * Bolds this element if it is not already bolded.
   */
  addBold() {
    if (this.isBolded()) {
      return;
    }
    this.classList.add(BOLD_FONT_WEIGHT);
    this.classList.remove(DEFAULT_FONT_WEIGHT);
  }

  /**
   * Removes bold from this element if it is currently bolded.
   */
  removeBold() {
    if (!this.isBolded()) {
      return;
    }
    this.classList.add(DEFAULT_FONT_WEIGHT);
    this.classList.remove(BOLD_FONT_WEIGHT);
  }

  /**
   * Returns true if `timestampMs` is within the time range of
   * this transcript line element.
   */
  isWithinTimeRange(timestampMs) {
    return this.transcriptLine.startTimestampMs <= timestampMs &&
        timestampMs <= this.transcriptLine.endTimestampMs;
  }

  /**
   * Returns true if the starting time of this element is before `timeMs`.
   */
  isBeforeTimeMs(timeMs) {
    return this.startTimestampMs < timeMs;
  }
}

/**
 * Returns the next transcript line for `timeMs`.
 */
function transcriptLineWithTime(timeMs) {
  const nextTranscript = currentTranscriptLine.nextElementSibling;
  // If the video is playing normally, the next transcript line
  // is the one immediately after it. This check is done before
  // the search is conducted because it is more time efficient
  // to check the next element than to conduct a search.
  if (nextTranscript.isWithinTimeRange(timeMs)) {
    return nextTranscript;
  }
  // This call happens if the user seeks to a certain timestamp instead.
  return findClosestTranscriptLine(timeMs);
}

/**
 * Searches for and returns the closest transcript line
 * based on `timeMs`.
 */
function findClosestTranscriptLine(timeMs) {
  // TODO: Create a global variable for the list of transcript line elements
  // once the pull request separating transcript.js into classes is merged.
  const transcriptLineElements = document.getElementsByTagName('li');
  let transcriptLinePointer = transcriptLineElements[0];
  while (transcriptLinePointer != null &&
         !transcriptLinePointer.isWithinTimeRange(timeMs) &&
         transcriptLinePointer.isBeforeTimeMs(timeMs)) {
    transcriptLinePointer = transcriptLinePointer.nextElementSibling;
  }
  // This happens when `timeMs` is after the last transcriptLine's ending
  // timestamp. `TranscriptLinePointer` is updated to be the last transcriptLine
  // because it is the closest line that the transcript can scroll to.
  if (transcriptLinePointer === null) {
    transcriptLinePointer =
        transcriptLineElements[transcriptLineElements.length - 1];
  }
  return transcriptLinePointer;
}

customElements.define(CUSTOM_ELEMENT_TRANSCRIPT_LINE, TranscriptLineElement);
