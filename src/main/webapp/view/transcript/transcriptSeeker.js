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

// TODO: Update this import statement once TranscriptLineElement is in master.
import {appendTextToList} from '/transcript.js';

/** Loads the transcript lines onto the DOM. */
export class TranscriptArea {
  static #ENDPOINT_TRANSCRIPT = '/transcript';
  static #TRANSCRIPT_CONTAINER = 'transcript-lines-container';
  static #PARAM_ID = 'id';

  #currentTranscriptLine;
  #eventController;

  /**
   * Creates an instance of `TranscriptArea` for loading
   * transcript lines onto the DOM.
   *
   * @param eventController An event controller object that
   *     that will be passed into a seekTranscript object.
   */
  constructor(eventController) {
    console.log(eventController);
    this.#eventController = eventController;
    // TODO: Create a transcriptSeeker object with the
    // eventController as the parameter.
  }

  /**
   * Returns the `currentTranscriptLine` if it exists. Else, returns
   * undefined.
   */
  currentTranscriptLine() {
    if (this.#currentTranscriptLine == null) {
      // TODO: Update the query to find transcript-line elements once
      // pull request #192 is merged.
      // If there are no elements, currentTranscript is assigned to be
      // undefined.
      this.#currentTranscriptLine = document.getElementsByTagName('li')[0];
    }
    return this.#currentTranscriptLine;
  }

  /**
   * Scrolls `transcriptLine` to the top of the transcript area.
   */
  scrollToTopOfTranscript(transcriptLine) {
    const transcriptContainer = document.getElementById(TRANSCRIPT_CONTAINER);
    const ulElementOffset = transcriptLine.parentElement.offsetTop;
    transcriptContainer.scrollTop = transcriptLine.offsetTop - ulElementOffset;
  }

  // TODO: Move functions getNextTranscript() and findClosestTranscriptLine()
  // once #215 is merged.
}
