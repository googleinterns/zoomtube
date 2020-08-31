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

import IconFeedback from './icon-feedback.js';

const ENDPOINT_FEEDBACK = '/icon-feedback';

const PARAM_LECTURE_ID = 'lectureId';
const PARAM_TIMESTAMP = 'timestampMs';
const PARAM_ICON_TYPE = 'iconType';

export default class LoadIconFeedback {
  #lectureId;
  #iconFeedback;

  constructor(lectureId) {
    this.#lectureId = lectureId;
  }

  async initialize() {
    this.#iconFeedback = new IconFeedback();
    await this.loadIconFeedbackList();
  }

  /**
   * Fetches avaiable Lectures from `ENDPOINT_FEEDBACK`
   * and sets them in the lecture selection page.
   */
  async loadIconFeedbackList(lectureId) {
    const url = new URL(ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(PARAM_LECTURE_ID, this.#lectureId);
    const response = await fetch(url);
    const jsonData = await response.json();
    this.parseFeedback(jsonData);
    console.log(this.#iconFeedback);
  }

  parseFeedback(jsonData) {
    let index = 0;
    let interval = 0;
    while (index < jsonData.length) {
      let goodCount = 0;
      let badCount = 0;
      let tooFastCount = 0;
      let tooSlowCount = 0;
      while (index < jsonData.length &&
             jsonData[index].timestampMs < interval) {
        if (jsonData[index].type == 'GOOD') {
          goodCount++;
        } else if (jsonData[index].type == 'BAD') {
          badCount++;
        } else if (jsonData[index].type == 'TOO_FAST') {
          tooFastCount++;
        } else {
          tooSlowCount++;
        }
        index++;
      }
      this.#iconFeedback.appendGood(goodCount);
      this.#iconFeedback.appendBad(badCount);
      this.#iconFeedback.appendTooFast(tooFastCount);
      this.#iconFeedback.appendTooSlow(tooSlowCount);
      this.#iconFeedback.appendInterval(interval / 1000);
      interval += 10000;
    }
  }
}

const PARAM_ID = 'id';

/** Lecture ID stored in `window.location.serach`. */
const lectureId = getLectureId(window.location.search);

/**
 * Returns the lecture id from `urlSearchParams`.
 */
function getLectureId(urlSearchParams) {
  const urlParams = new URLSearchParams(urlSearchParams);
  return urlParams.get(PARAM_ID);
}

const loadIconFeedback = new LoadIconFeedback(lectureId);
loadIconFeedback.initialize();
