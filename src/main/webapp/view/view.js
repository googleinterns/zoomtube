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

import Synchronizer from '../synchronizer.js';

import {intializeDiscussion} from '../view/discussion/discussion.js';
import TranscriptArea from './transcript/transcript-area.js';
import Video from './video/video.js';

const ENDPOINT_LECTURE = '/lecture';

const PARAM_ID = 'id';

// TODO: Remove global scope and add to view object.
window.video = new Video();

/* exported LECTURE_ID */
window.LECTURE_ID = getLectureId();

// TODO: Remove global scope and link to a View object.
window.synchronizer = new Synchronizer();

/** Sets {@code window.LECTURE} as Lecture for view page. */
getLecture().then((lecture) => {
  window.LECTURE = lecture;
  initialize();
});

/**
 * Initializes the video player, discussion
 * and transcript sections for the lecture view page.
 */
async function initialize() {
  setLectureName();
  window.video.loadVideoApi();
  // TODO: Move TranscriptArea initialization outside of initialize()
  // and replace string parameter with a controller object.
  const transcript = new TranscriptArea('event controller');
  intializeDiscussion(transcript.transcriptSeeker());
  await transcript.loadTranscript();
}

/**
 * Returns lecture in database associated with `window.LECTURE_ID`
 * obtained from `ENDPOINT_LECTURE`.
 */
async function getLecture() {
  const url = new URL(ENDPOINT_LECTURE, window.location.origin);
  url.searchParams.append(PARAM_ID, window.LECTURE_ID);
  const response = await fetch(url);
  return response.json();
}

/**
 * Returns the lecture id obtained from the current page's URL parameters.
 */
function getLectureId() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(PARAM_ID);
}

/** Sets the lecture name in `header-text`. */
function setLectureName() {
  const headerText = document.getElementById('header-text');
  headerText.innerText = window.LECTURE.lectureName;
}
