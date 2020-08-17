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

let videoSyncTimer;
let lastVideoTimeMs;

const TIME_INTERVAL_MS = 100;

/**
 * Starts timer which broadcasts current video time every
 * `TIME_INTERVAL_MS` milliseconds.
 */
function startVideoSyncTimer() {
  videoSyncTimer = window.setInterval(() => {
    const currentTimeSeconds = window.videoPlayer.getCurrentTime();
    sync(window.secondsToMilliseconds(currentTimeSeconds));
  }, /* ms= */ TIME_INTERVAL_MS);
}

/**
 * Calls functions that seek transcript, and discussion to `currentVideoTimeMs`
 * if the `currentTimeMs` changes from the last time this was called.
 */
function sync(currentVideoTimeMs) {
  if (currentVideoTimeMs == lastVideoTimeMs) {
    return;
  }
  lastVideoTimeMs = currentVideoTimeMs;
  window.seekTranscript(currentVideoTimeMs);
  window.seekDiscussion(currentVideoTimeMs);
}
