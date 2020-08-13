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
const TIME_INTERVAL = 100;

/**
 * Starts timer which broadcasts current video time every
 * {@code TIME_INTERVAL} milliseconds.
 */
function startVideoSyncTimer() {
  videoSyncTimer = window.setInterval(() => {
    sync(window.videoPlayer.getCurrentTime());
  }, /* ms= */ TIME_INTERVAL);
}

/**
 * Calls functions that seek transcript, and discussion to {@code currentTime}
 * (number of seconds since video started playing).
 */
function sync(currentTime) {
  window.seekTranscript(currentTime);
  window.seekDiscussion(currentTime);
}
