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

/** Stores icon feedback data in the database. */
export default class PostIconFeedback {
  static #ENDPOINT_FEEDBACK = '/icon-feedback';
  static #PARAM_LECTURE_ID = 'lectureId';
  static #PARAM_TIMESTAMP = 'timestampMs';
  static #PARAM_ICON_TYPE = 'iconType';

  #lecture;
  #video;

  constructor(lecture, video) {
    this.#lecture = lecture;
    this.#video = video;
  }

  initialize() {
    window.iconOnClick = this.iconOnClick.bind(this);
  }

  /**
   * Sends `iconType`, video timestamp of when icon was clicked,
   * and lecture ID to be stored in database.
   */
  iconOnClick(iconType) {
    const videoTimeStamp = this.#video.getCurrentVideoTimeMs();
    const url =
        new URL(PostIconFeedback.#ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(
        PostIconFeedback.#PARAM_LECTURE_ID, this.#lecture.key.id);
    url.searchParams.append(PostIconFeedback.#PARAM_TIMESTAMP, videoTimeStamp);
    url.searchParams.append(PostIconFeedback.#PARAM_ICON_TYPE, iconType);
    fetch(url, {method: 'POST'});
  }
}
