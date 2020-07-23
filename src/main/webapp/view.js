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
const PARAM_VIDEO_ID = 'video-id';

/* Video ID for specific lecture. */
window.VIDEO_ID = getVideoId();
/* Database ID for specific lecture. */
window.LECTURE_ID = getLectureId();

initialize();

/**
 * Initializes the video, discussion, and transcript sections of the
 * lecture view page.
 */
async function initialize() {
  window.loadDiscussion();
  window.loadApi();
  window.sendPostToTranscript();
}

/**
 * Returns the lecture id obtained from {@code window.location}.
 */
function getLectureId() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(PARAM_ID);
}

/** Returns video ID for lecture. */
function getVideoId() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(PARAM_VIDEO_ID);
}
