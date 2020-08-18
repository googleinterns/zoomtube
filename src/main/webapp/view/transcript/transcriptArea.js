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

import {appendTextToList} from '/transcript.js';

/** Loads the transcript lines onto the DOM. */
export class TranscriptArea {
  static #ENDPOINT_TRANSCRIPT = '/transcript';
  static #TRANSCRIPT_CONTAINER = 'transcript-lines-container';
  static #PARAM_ID = 'id';

  #transcriptSeeker;git s
  #eventController;

  /**
   * Creates an instance of `TranscriptArea` for loading
   * transcript lines onto the DOM.
   *
   * @param eventController An event controller object that
   *     that will be passed into
   */
  constructor(eventController) {
    this.#eventController = eventController;
    // TODO: Create a transcriptSeeker object.
    // TODO: Assign EventController and call addListener.
  }

  /**
   * Fetches the transcript lines from `ENDPOINT_TRANSCRIPT`.
   *
   * <p>This function assumes that the transcript lines have already
   * been added to the datastore.
   */
  loadTranscript() {
    const url =
        new URL(TranscriptArea.#ENDPOINT_TRANSCRIPT, window.location.origin);
    url.searchParams.append(TranscriptArea.#PARAM_ID, window.LECTURE_ID);
    fetch(url).then((response) => response.json()).then((transcriptLines) => {
      TranscriptArea.addMultipleTranscriptLinesToDom(transcriptLines);
    });
  }

  /**
   * Adds `transcriptLines` to the DOM as list elements.
   */
  static addMultipleTranscriptLinesToDom(transcriptLines) {
    const transcriptContainer =
        document.getElementById(TranscriptArea.#TRANSCRIPT_CONTAINER);
    if (transcriptContainer.firstChild) {
      transcriptContainer.removeChild(transcriptContainer.firstChild);
    }
    const ulElement = document.createElement('ul');
    ulElement.class = 'mx-auto';
    transcriptContainer.appendChild(ulElement);
    transcriptLines.forEach((transcriptLine) => {
      appendTextToList(transcriptLine, ulElement);
    });
  }

  // TODO: Add a getter method for the transcriptSeeker object.
}