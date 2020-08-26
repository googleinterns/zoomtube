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

/**
 * Handles when to seek transcript and discussion areas according to video
 * time.
 */
export default class Synchronizer {
  static #TIME_INTERVAL_MS = 100;

  #video;
  #eventController;
  #lastSyncedTimeMs;

  constructor(video, eventController) {
    this.#video = video;
    this.#eventController = eventController;
  }

  /**
   * Starts timer which broadcasts current video time every
   * `TIME_INTERVAL_MS` milliseconds.
   */
  startVideoSyncTimer() {
    setInterval(() => {
      this.sync(this.#video.getCurrentVideoTimeMs());
    }, /* ms= */ Synchronizer.#TIME_INTERVAL_MS);
  }

  /**
   * Broadcasts event that seeks transcript and discussion to
   * `currentVideoTimeMs`, if the `currentVideoTimeMs` changed from the last
   * time this method was called.
   */
  sync(currentVideoTimeMs) {
    if (currentVideoTimeMs == this.#lastSyncedTimeMs) {
      return;
    }
    this.#lastSyncedTimeMs = currentVideoTimeMs;
    this.#eventController.broadcastEvent('seek', currentVideoTimeMs);
  }
}
