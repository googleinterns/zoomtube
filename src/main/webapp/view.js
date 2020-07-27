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

const PARAM_ID = 'id';
window.LECTURE_ID = getLectureId();

initialize();

/**
 * Initializes the discussion section of the lecture view page.
 */
async function initialize() {
  window.loadDiscussion();
  window.sendPostToTranscript();
  // TODO: Initialize the video and trancript sections once they are added.
}

/**
 * Returns the lecture id obtained from {@code window.location}.
 */
function getLectureId() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(PARAM_ID);
}