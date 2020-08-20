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

/** Seeks to a line in the transcript. */
export default class TranscriptSeeker {
  static #TRANSCRIPT_CONTAINER = 'transcript-lines-container';

  #currentTranscriptLine;
  #eventController;

  /**
   * Creates an instance of `TranscriptSeeker` for seeking
   * to a line in the transcript.
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
   */
  currentTranscriptLine() {
    if (this.#currentTranscriptLine == null) {
      // If the first transcript line doesn't exist, `currentTranscriptLine` is
      // assigned to be undefined.
      this.#currentTranscriptLine =
          document.getElementsByTagName('transcript-line')[0];
    }
    return this.#currentTranscriptLine;
  }

  /**
   * Scrolls the transcript area so that `transcriptLine` is at the top.
   */
  static scrollToTopOfTranscript(transcriptLine) {
    const transcriptContainer =
        document.getElementById(TranscriptSeeker.#TRANSCRIPT_CONTAINER);
    const ulElementOffset = transcriptLine.parentElement.offsetTop;
    transcriptContainer.scrollTop = transcriptLine.offsetTop - ulElementOffset;
  }

  /** Seeks transcript to `timeMs`. */
  seekTranscript(timeMs) {
    // TODO: Refactor this method once the helper methods in #215
    // and #228 are merged into master.
    if (timeMs < this.currentTranscriptLine().transcriptLine.startTimestampMs) {
      return;
    }
    if (this.currentTranscriptLine().isWithinTimeRange(timeMs)) {
      this.currentTranscriptLine().addBold();
      return;
    }
    this.currentTranscriptLine().removeBold();
    this.#currentTranscriptLine =
        this.currentTranscriptLine().nextElementSibling;
    TranscriptSeeker.scrollToTopOfTranscript(this.currentTranscriptLine());
    this.currentTranscriptLine().addBold();
    // TODO: Handle the case where the video isn't only playing.
  }
  // TODO: Move functions getNextTranscript() and findClosestTranscriptLine()
  // into this class once #215 is merged.

  // TODO: Add a method that adds the eventListeners.
}
