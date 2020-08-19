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

import {secondsToMilliseconds} from '../timestamps.js';
import {seekDiscussion} from './view/discussion/discussion.js';
import {seekTranscript} from './view/transcript/transcript.js';

let lastSyncedTimeMs;

const TIME_INTERVAL_MS = 100;

/**
 * Starts timer which broadcasts current video time every
 * `TIME_INTERVAL_MS` milliseconds.
 */
export function startVideoSyncTimer() {
  setInterval(() => {
    const currentTimeSeconds = window.videoPlayer.getCurrentTime();
    sync(secondsToMilliseconds(currentTimeSeconds));
  }, /* ms= */ TIME_INTERVAL_MS);
}

/**
 * Calls functions that seek transcript, and discussion to `currentVideoTimeMs`
 * if the `currentVideoTimeMs` changed from the last time this method was
 * called.
 */
function sync(currentVideoTimeMs) {
  if (currentVideoTimeMs == lastSyncedTimeMs) {
    return;
  }
  lastSyncedTimeMs = currentVideoTimeMs;
  seekTranscript(currentVideoTimeMs);
  seekDiscussion(currentVideoTimeMs);
}
