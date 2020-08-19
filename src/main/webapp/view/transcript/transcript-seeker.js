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


/** Controls seeking to parts in the transcript. */
export default class TranscriptSeeker {
  static #TRANSCRIPT_CONTAINER = 'transcript-lines-container';

  #currentTranscriptLine;
  #eventController;

  /**
   * Creates an instance of `TranscriptSeeker` for loading
   * transcript lines onto the DOM.
   *
   * @param eventController An event controller object that
   *     that will be passed into a seekTranscript object.
   */
  constructor(eventController) {
    console.log(eventController);
    this.#eventController = eventController;
    // TODO: Add method to add listeners.
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
    const transcriptContainer =
        document.getElementById(TranscriptSeeker.#TRANSCRIPT_CONTAINER);
    const ulElementOffset = transcriptLine.parentElement.offsetTop;
    transcriptContainer.scrollTop = transcriptLine.offsetTop - ulElementOffset;
  }

  /** Seeks transcript to `currentTime`, which is given in seconds. */
  seekTranscript(currentTime) {
    const currentTimeMs = secondsToMilliseconds(currentTime);
    if (currentTimeMs < this.#currentTranscriptLine.startTimestampMs) {
      return;
    }
    if (isWithinCurrentTimeRange(currentTimeMs)) {
      addBold(this.#currentTranscriptLine);
      return;
    }
    removeBold(this.#currentTranscriptLine);
    this.#currentTranscriptLine = this.#currentTranscriptLine.nextElementSibling;
    scrollToTopOfTranscript(this.#currentTranscriptLine);
    addBold(this.#currentTranscriptLine);
    // TODO: Handle the case where the video isn't only playing.
  }

  // TODO: Move functions getNextTranscript() and findClosestTranscriptLine()
  // into this class once #215 is merged.
}
