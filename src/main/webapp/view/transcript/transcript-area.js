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

import {ScrollContainer} from '../../scroll-container.js';
import TranscriptSeeker from './transcript-seeker.js';
import {TranscriptLineElement} from './transcript.js';

/** Loads the transcript lines onto the DOM. */
export default class TranscriptArea {
  static #ENDPOINT_TRANSCRIPT = '/transcript';
  static #TRANSCRIPT_CONTAINER = 'transcript-lines-container';
  static #TRANSCRIPT_PARENT_CONTAINER = 'transcript-container';
  static #transcriptContainer;
  static #PARAM_ID = 'id';
  static #TRANSCRIPT_ERROR_MESSAGE =
      'Sorry, there is no transcript available for this lecture recording. :(';

  #lecture
  #eventController;
  #transcriptSeeker;

  /**
   * Creates an instance of `TranscriptArea` for loading
   * transcript lines onto the DOM.
   *
   * @param eventController An event controller object that
   *     that will be passed into a seekTranscript object.
   */
  constructor(lecture, eventController) {
    this.#lecture = lecture;
    this.#eventController = eventController;
    this.#transcriptSeeker = new TranscriptSeeker(eventController);
  }

  /**
   * Adds event listener for seeking and initializes the transcript area by
   * loading the transcript lines.
   */
  async initialize() {
    this.#transcriptSeeker.addSeekingListener();
    await this.loadTranscript();
  }

  /**
   * Fetches the transcript lines from `ENDPOINT_TRANSCRIPT`. If there
   * are no transcript lines, an error message is displayed in the
   * transcript container instead.
   *
   * <p>This function assumes that if there is a transcript for the
   * current lecture, the lines have already been added to the datastore.
   */
  async loadTranscript() {
    const url =
        new URL(TranscriptArea.#ENDPOINT_TRANSCRIPT, window.location.origin);
    url.searchParams.append(TranscriptArea.#PARAM_ID, this.#lecture.key.id);
    const transcriptResponse = await fetch(url);
    const transcriptLines = await transcriptResponse.json();
    // No transcript lines are available for this lecture.
    if (transcriptLines.length == 0) {
      TranscriptArea.displayNoTranscriptMessage();
      return;
    }
    TranscriptArea.addTranscriptLinesToDom(transcriptLines);
  }

  /**
   * Displays a message in the transcript container if there is no
   * transcript available for the lecture recording.
   */
  static displayNoTranscriptMessage() {
    const transcriptContainer = TranscriptArea.transcriptScrollContainer();
    transcriptContainer.innerText = TranscriptArea.#TRANSCRIPT_ERROR_MESSAGE;
    transcriptContainer.classList.add('text-center');
  }

  /**
   * Adds `transcriptLines` to the DOM as list elements.
   *
   * <p>This is a private method that should only be called in
   * `loadTranscript()`.
   */
  static addTranscriptLinesToDom(transcriptLines) {
    const transcriptContainer = TranscriptArea.transcriptScrollContainer();
    const ulElement = document.createElement('ul');
    // TODO: Move the class assignment to the HTML.
    ulElement.class = 'mx-auto';
    transcriptContainer.appendChild(ulElement);
    transcriptLines.forEach((transcriptLine) => {
      ulElement.appendChild(
          TranscriptLineElement.createTranscriptLineElement(transcriptLine));
    });
  }

  /**
   * Returns the container storing the transcript.
   *
   * <p>If the container is undefined, a new ScrollContainer is
   * created and returned.
   */
  static transcriptScrollContainer() {
    if (this.#transcriptContainer == null) {
      this.#transcriptContainer = new ScrollContainer();
      this.#transcriptContainer.id = TranscriptArea.#TRANSCRIPT_CONTAINER;
      const parentContainer =
          document.getElementById(TranscriptArea.#TRANSCRIPT_PARENT_CONTAINER);
      parentContainer.appendChild(this.#transcriptContainer);
    }
    return this.#transcriptContainer;
  }

  /**
   * Returns the `transcriptSeeker`.
   */
  transcriptSeeker() {
    return this.#transcriptSeeker;
  }
}
