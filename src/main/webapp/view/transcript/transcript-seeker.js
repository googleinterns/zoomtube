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

import {secondsToMilliseconds} from '../../timestamps.js';

// TODO: Update imports below once pull request #192
// is merged.
import {addBold, isWithinCurrentTimeRange, removeBold} from './transcript.js';

/** Seeks to parts of the transcript. */
export default class TranscriptSeeker {
  static #TRANSCRIPT_CONTAINER = 'transcript-lines-container';

  #currentTranscriptLine;
  #eventController;

  /**
   * Creates an instance of `TranscriptSeeker` for loading
   * transcript lines onto the DOM.
   *
   * @param eventController An event controller instance that
   *    will help relay the current time to other objects.
   */
  constructor(eventController) {
    this.#eventController = eventController;
    // TODO: Add the event listeners.
  }

  /**
   * Returns the `currentTranscriptLine` if it exists. Else, returns
   * undefined.
   *
   * <p>This is a public getter method for the retrieving the
   * `currentTranscriptLine`.
   */
  currentTranscriptLine() {
    if (this.#currentTranscriptLine == null) {
      // TODO: Update the query to find transcript-line elements once
      // pull request #192 is merged.
      // If the first transcript line doesn't exist, currentTranscript is
      // assigned to be undefined.
      this.#currentTranscriptLine = document.getElementsByTagName('li')[0];
    }
    return this.#currentTranscriptLine;
  }

  setCurrentTranscriptLine(currentTranscriptLine) {
    this.#currentTranscriptLine = currentTranscriptLine;
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
    // TODO: Refactor this method once the helper methods in #215
    // and #228 are merged into master.
    const currentTimeMs = secondsToMilliseconds(currentTime);
    if (currentTimeMs < this.currentTranscriptLine().startTimestampMs) {
      return;
    }
    if (isWithinCurrentTimeRange(currentTimeMs)) {
      addBold(this.currentTranscriptLine());
      return;
    }
    removeBold(this.currentTranscriptLine());
    this.setCurrentTranscriptLine(
        this.currentTranscriptLine().nextElementSibling);
    this.scrollToTopOfTranscript(this.currentTranscriptLine());
    addBold(this.currentTranscriptLine());
    // TODO: Handle the case where the video isn't only playing.
  }

  // TODO: Move functions getNextTranscript() and findClosestTranscriptLine()
  // into this class once #215 is merged.

  // TODO: Add a method that adds the eventListeners.
}
