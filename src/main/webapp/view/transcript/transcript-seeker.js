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
      console.log('currentTranscriptLine not defined');
      this.#currentTranscriptLine =
          document.getElementsByTagName('transcript-line')[0];
    }
    console.log('getting the current transcript');
    return this.#currentTranscriptLine;
  }

  /**
   * Scrolls the transcript container so that `transcriptLine` is at the top
   * of the container.
   */
  static scrollToTopOfTranscript(transcriptLine) {
    const transcriptContainer =
        document.getElementById(TranscriptSeeker.#TRANSCRIPT_CONTAINER);
    const ulElementOffset = transcriptLine.parentElement.offsetTop;
    transcriptContainer.scrollTop = transcriptLine.offsetTop - ulElementOffset;
  }

  /** Seeks transcript to `timeMs`. */
  seekTranscript(timeMs) {
    console.log('time to seek' + timeMs);
    if (this.currentTranscriptLine().isWithinTimeRange(timeMs)) {
      console.log('within currentTime seekTranscript');
      this.currentTranscriptLine().addBold();
      return;
    }
    console.log('not within current time seek transcript');
    this.currentTranscriptLine().removeBold();
    this.#currentTranscriptLine = this.transcriptLineWithTime(timeMs);
    TranscriptSeeker.scrollToTopOfTranscript(this.currentTranscriptLine());
    if (this.currentTranscriptLine().isWithinTimeRange(timeMs)) {
      console.log('not within timerange do not bold');
      this.currentTranscriptLine().addBold();
    }
    console.log(this.#currentTranscriptLine);
    console.log(this.currentTranscriptLine());
  }
  /**
   * Returns the next transcript line for `timeMs`.
   */
  transcriptLineWithTime(timeMs) {
    console.log('get with time');
    const nextTranscript = this.currentTranscriptLine().nextElementSibling;
    // If the video is playing normally, the next transcript line
    // is the one immediately after it. This check is done before
    // the search is conducted because it is more time efficient
    // to check the next element than to conduct a search.
    if (nextTranscript.isWithinTimeRange(timeMs)) {
      console.log('isnext transcript');
      return nextTranscript;
    }
    console.log('not is next transcript');
    // This call happens if the user seeks to a certain timestamp instead.
    return this.findClosestTranscriptLine(timeMs);
  }

  /**
   * Searches for and returns the closest transcript line
   * based on `timeMs`.
   */
  findClosestTranscriptLine(timeMs) {
    // TODO: Create a global variable for the list of transcript line elements
    // once the pull request separating transcript.js into classes is merged.
    const transcriptLineElements =
        document.getElementsByTagName('transcript-line');
    let transcriptLinePointer = transcriptLineElements[0];
    console.log(transcriptLinePointer != null);
    console.log(!transcriptLinePointer.isWithinTimeRange(timeMs));
    console.log(transcriptLinePointer.isBeforeTimeMs(timeMs));
    while (transcriptLinePointer != null &&
           !transcriptLinePointer.isWithinTimeRange(timeMs) &&
           !transcriptLinePointer.isBeforeTimeMs(timeMs)) {
      console.log('loop to find closest');
      transcriptLinePointer = transcriptLinePointer.nextElementSibling;
    }
    // This happens when `timeMs` is after the last transcriptLine's ending
    // timestamp. `TranscriptLinePointer` is updated to be the last
    // transcriptLine because it is the closest line that the transcript can
    // scroll to.
    if (transcriptLinePointer === null) {
      console.log('is null findClosestTranscriptLine');
      transcriptLinePointer =
          transcriptLineElements[transcriptLineElements.length - 1];
    }
    console.log('findcloesst');
    console.log(transcriptLinePointer);
    return transcriptLinePointer;
  }

  // TODO: Add a method that adds the eventListeners.
}