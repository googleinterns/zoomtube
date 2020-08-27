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

import TranscriptSeeker from './transcript-seeker.js';
import {TranscriptLineElement} from './transcript.js';

/** Loads the transcript lines onto the DOM. */
export default class TranscriptArea {
  static #ENDPOINT_TRANSCRIPT = '/transcript';
  static #TRANSCRIPT_CONTAINER = 'transcript-lines-container';
  static #PARAM_ID = 'id';

  static transcriptLineMap = new Map();

  #transcriptSeeker;
  #eventController;


  /**
   * Creates an instance of `TranscriptArea` for loading
   * transcript lines onto the DOM.
   *
   * @param eventController An event controller object that
   *     that will be passed into a seekTranscript object.
   */
  constructor(eventController) {
    this.#eventController = eventController;
    this.#transcriptSeeker = new TranscriptSeeker(eventController);
    // eventController as the parameter.
  }

  /**
   * Fetches the transcript lines from `ENDPOINT_TRANSCRIPT`.
   *
   * <p>This function assumes that the transcript lines have already
   * been added to the datastore.
   */
  async loadTranscript() {
    const url =
        new URL(TranscriptArea.#ENDPOINT_TRANSCRIPT, window.location.origin);
    url.searchParams.append(TranscriptArea.#PARAM_ID, window.LECTURE_ID);
    const transcriptResponse = await fetch(url);
    const transcriptLines = await transcriptResponse.json();
    TranscriptArea.addTranscriptLinesToDom(transcriptLines);
  }

  /**
   * Adds `transcriptLines` to the DOM as list elements.
   *
   * <p>This is a private method that should only be called in
   * `loadTranscript()`.
   */
  static addTranscriptLinesToDom(transcriptLines) {
    const transcriptContainer =
        document.getElementById(TranscriptArea.#TRANSCRIPT_CONTAINER);
    // Removes the transcript lines from the container if there are any.
    // This prevents having multiple sets of ul tags every time the page
    // is refreshed.
    if (transcriptContainer.firstChild) {
      transcriptContainer.removeChild(transcriptContainer.firstChild);
    }
    const ulElement = document.createElement('ul');
    // TODO: Move the class assignment to the HTML.
    ulElement.class = 'mx-auto';
    transcriptContainer.appendChild(ulElement);
    transcriptLines.forEach((transcriptLine) => {
      const transcriptLineElement =
          TranscriptLineElement.createTranscriptLineElement(transcriptLine);
      ulElement.appendChild(transcriptLineElement);
      TranscriptArea.transcriptLineMap.set(
          transcriptLine.transcriptKey.id, transcriptLineElement);
    });
  }

  /** Increments the indicator corresponding to `transcriptLineKey` by 1. */
  static incrementCommentIndicatorAt(transcriptLineKeyId) {
    if (!TranscriptArea.transcriptLineMap.has(transcriptLineKeyId)) {
      return;
    }
    const commentIndicatorElement =
        TranscriptArea.transcriptLineMap.get(transcriptLineKeyId)
            .commentIndicator;
    commentIndicatorElement.innerText =
        parseInt(commentIndicatorElement.innerText) + 1;
    commentIndicatorElement.style.visibility = 'visible';
  }

  /**
   * Returns the `transcriptSeeker`.
   */
  transcriptSeeker() {
    return this.#transcriptSeeker;
  }
}
