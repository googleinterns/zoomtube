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

/**
 * Sends a POST request to delete all of the transcript lines from datastore.
 */
export function deleteTranscript() {
  fetch('/delete-transcript', {method: 'POST'});
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
