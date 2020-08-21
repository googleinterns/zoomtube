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

import {seekDiscussion} from './view/discussion/discussion.js';
import {seekTranscript} from './view/transcript/transcript.js';

let lastTime;

const TIME_INTERVAL_MS = 100;

/** Handles when to seek transcript and discussion areas according to video time. */
export default class Synchronizer {
  /**
   * Starts timer which broadcasts current video time every
   * `TIME_INTERVAL_MS` milliseconds.
   */
  startVideoSyncTimer() {
    window.setInterval(() => {
      this.sync(window.videoPlayer.getCurrentTime());
    }, /* ms= */ TIME_INTERVAL_MS);
  }

  /**
   * Calls functions that seek transcript and discussion to `currentTime`
   * (number of seconds since start of video), when the `currentTime`
   * changes from the last time this was called.
   */
  // TODO: Send `currentTime` as ms.
  sync(currentTime) {
    if (currentTime == lastTime) {
      return;
    }
    lastTime = currentTime;
    seekTranscript(currentTime);
    seekDiscussion(currentTime);
  }
}
