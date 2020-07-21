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

const SELECTOR_DISCUSSION = '#discussion';
const LECTURE_KEY = getLectureKey();

initialize();

/**
 * Initializes the various parts of the lecture viewer.
 */
async function initialize() {
  loadDiscussion();
}

/**
 * Parses the lecture key out of {@code window.location}
 */
function getLectureKey() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('key');
}
