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

import DiscussionArea from './discussion/discussion-area.js';
import TranscriptArea from './transcript/transcript-area.js';
import Video from './video/video.js';

const ENDPOINT_LECTURE = '/lecture';

const PARAM_ID = 'id';

export default class View {
  #lecture;
  #lectureId;

  /** Sets `lectureId`, `lecture` and initializes view page components. */
  constructor() {
    this.#lectureId = this.getLectureId();
    this.getLecture().then((lecture) => {
      this.#lecture = lecture;
      this.initialize();
    });
  }

  /**
   * Initializes the video player, discussion
   * and transcript sections for the lecture view page.
   */
  async initialize() {
    this.setLectureName();

    // TODO: Make these private once event controller is created.
    View.video = new Video(this.#lecture);
    // TODO: Move TranscriptArea initialization outside of initialize()
    // and replace string parameter with a controller object.
    View.transcript = new TranscriptArea(this.#lecture, 'event controller');
    View.discussion = new DiscussionArea(this.#lecture);

    await View.video.loadVideoApi();
    await View.transcript.loadTranscript();
    await View.discussion.initialize();

    // This is used as the `onclick` handler of the new comment area submit
    // button. It must be set after discussion is initialized.
    window.postNewComment =
        View.discussion.postNewComment.bind(View.discussion);
  }

  /**
   * Returns lecture in database associated with `View.lectureId`
   * obtained from `ENDPOINT_LECTURE`.
   */
  async getLecture() {
    const url = new URL(ENDPOINT_LECTURE, window.location.origin);
    url.searchParams.append(PARAM_ID, this.#lectureId);
    const response = await fetch(url);
    return response.json();
  }

  /**
   * Returns the lecture id obtained from the current page's URL parameters.
   */
  getLectureId() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(PARAM_ID);
  }

  /** Sets the lecture name in `header-text`. */
  setLectureName() {
    const headerText = document.getElementById('header-text');
    headerText.innerText = this.#lecture.lectureName;
  }
}

window.lectureView = new View();
