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

import EventController from '../event-controller.js';
import PostIconFeedback from '../feedback/post-icon-feedback.js';
import DiscussionArea from './discussion/discussion-area.js';
import TranscriptArea from './transcript/transcript-area.js';
import Video from './video/video.js';

const ENDPOINT_LECTURE = '/lecture';
const HEADER_TEXT = 'header-text';

const PARAM_ID = 'id';

/**
 * Initilises and stores instances related to video, transcript, and
 * disscussion.
 */
export default class LectureView {
  #lecture;
  #eventController;
  #video;
  #transcript;
  #discussion;
  #postIconFeedback;

  constructor(lecture) {
    this.#lecture = lecture;
  }

  /**
   * Initializes the video player, discussion
   * and transcript sections for the lecture view page.
   */
  async initialize() {
    this.setLectureName();

    this.#eventController = new EventController();
    this.#video = new Video(this.#lecture, this.#eventController);
    this.#postIconFeedback = new PostIconFeedback(this.#lecture, this.#video);
    this.#transcript = new TranscriptArea(this.#lecture, this.#eventController);
    this.#discussion = new DiscussionArea(
        this.#lecture, this.#eventController,
        this.#transcript);

    await this.#video.loadVideoApi();
    await this.#transcript.initialize();
    await this.#discussion.initialize();
    this.#postIconFeedback.initialize();
  }

  /** Sets the lecture name in `header-text`. */
  setLectureName() {
    const headerText = document.getElementById(HEADER_TEXT);
    headerText.innerText = this.#lecture.lectureName;
  }
}

/** Lecture ID stored in `window.location.serach`. */
const lectureId = getLectureId(window.location.search);

/** Creates a LectureView with `lecture`. */
getLectureFromDatabase(lectureId).then((lecture) => {
  const lectureView = new LectureView(lecture);
  lectureView.initialize();
});

/**
 * Returns lecture in database associated with `lectureId`
 * obtained from `ENDPOINT_LECTURE`.
 */
async function getLectureFromDatabase(lectureId) {
  const url = new URL(ENDPOINT_LECTURE, window.location.origin);
  url.searchParams.append(PARAM_ID, lectureId);
  const response = await fetch(url);
  return response.json();
}

/**
 * Returns the lecture id from `urlSearchParams`.
 */
function getLectureId(urlSearchParams) {
  const urlParams = new URLSearchParams(urlSearchParams);
  return urlParams.get(PARAM_ID);
}
