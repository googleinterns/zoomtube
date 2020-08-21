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

const DEFAULT_FONT_WEIGHT = 'text-muted';
const BOLD_FONT_WEIGHT = 'font-weight-bold';
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
  static #DEFAULT_FONT_WEIGHT = 'text-muted';
  static #BOLD_FONT_WEIGHT = 'font-weight-bold';
  static #CUSTOM_ELEMENT_TRANSCRIPT_LINE = 'transcript-line';

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
    return new TranscriptLineElement(timestampRange, transcriptLine);
  }

  /**
   * Creates a custom HTML element representing `transcriptLine`.
   *
   * @param timestampRange The timestamp for the transcript line.
   * @param transcriptLine The transcriptLine from `ENDPOINT_TRANSCRIPT`
   *     whose `attributes` should be used.
   */
  constructor(timestampRange, transcriptLine) {
    super();
    const contentDivElement = TranscriptLineElement.createContentDivElement();
    TranscriptLineElement.appendParagraphToContainer(
        timestampRange, contentDivElement, ['justify-content-start', 'mb-1']);
    TranscriptLineElement.appendParagraphToContainer(
        transcriptLine.content, contentDivElement, ['ml-4', 'mb-1']);
    this.classList.add('align-self-center', 'mb-2', TranscriptLineElement.#DEFAULT_FONT_WEIGHT);
    this.appendChild(contentDivElement);
    this.appendChild(TranscriptLineElement.createHrElement());
    this.transcriptLine = transcriptLine;
  }

  /**
   * Creates a stylized div element that will be used to store
   * the time range and text in a `TranscriptLineElement`.
   */
  static createContentDivElement() {
    const contentDivElement = document.createElement('div');
    contentDivElement.classList.add('d-flex', 'flex-row', 'mb-1');
    return contentDivElement;
  }

  /**
   * Creates a stylized hr element that will be used to create a
   * `TranscriptLineElement`.
   */
  static createHrElement() {
    const hrElement = document.createElement('hr');
    hrElement.classList.add('my-1', 'align-middle', 'mr-5');
    return hrElement;
  }

  /**
   * Creates a p tag to store the given `text` inside the
   * `container`.
   *
   * <p>Adds classes the the p tag if `classList` is provided.
   */
  static appendParagraphToContainer(text, container, classes = []) {
    const pTag = document.createElement('p');
    pTag.innerText = text;
    container.appendChild(pTag);

    if (classes.length == 0) {
      return;
    }
    pTag.classList.add(...classes);
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
    this.classList.add(TranscriptLineElement.#BOLD_FONT_WEIGHT);
    this.classList.remove(TranscriptLineElement.#DEFAULT_FONT_WEIGHT);
  }

  /**
   * Removes bold from this element if it is currently bolded.
   */
  removeBold() {
    if (!this.isBolded()) {
      return;
    }
    this.classList.add(TranscriptLineElement.#DEFAULT_FONT_WEIGHT);
    this.classList.remove(TranscriptLineElement.#BOLD_FONT_WEIGHT);
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
    return this.transcriptLine.startTimestampMs < timeMs;
  }
}

customElements.define(CUSTOM_ELEMENT_TRANSCRIPT_LINE, TranscriptLineElement);
