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

import ParsedIconFeedback from './parsed-icon-feedback.js';

export default class LoadIconFeedback {
  static #ENDPOINT_FEEDBACK = '/icon-feedback';
  static #PARAM_LECTURE_ID = 'lectureId';
  static #INCREMENT_INTERVAL = 10000;

  #lectureId;
  #parsedIconFeedback;

  constructor(lectureId) {
    this.#lectureId = lectureId;
  }

  async initialize() {
    this.#parsedIconFeedback = new ParsedIconFeedback();
    await this.loadIconFeedbackList();
  }

  /**
   * Fetches avaiable Lectures from `ENDPOINT_FEEDBACK`
   * and sets them in the lecture selection page.
   */
  async loadIconFeedbackList() {
    const url =
        new URL(LoadIconFeedback.#ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(
        LoadIconFeedback.#PARAM_LECTURE_ID, this.#lectureId);
    const response = await fetch(url);
    const jsonData = await response.json();
    this.parseFeedback(jsonData);
    console.log(this.#parsedIconFeedback);
  }

  /**
   * Parses `iconFeedbackJson` by couting how many times each IconFeedback type
   * is clicked in each 10 second interval and stores that data in a
   * ParseIconFeedback object.
   */
  parseFeedback(iconFeedbackJson) {
    let index = 0;
    let interval = 0;
    while (index < iconFeedbackJson.length) {
      let goodCount = 0;
      let badCount = 0;
      let tooFastCount = 0;
      let tooSlowCount = 0;
      while (index < iconFeedbackJson.length &&
             iconFeedbackJson[index].timestampMs < interval) {
        if (iconFeedbackJson[index].type == 'GOOD') {
          goodCount++;
        } else if (iconFeedbackJson[index].type == 'BAD') {
          badCount++;
        } else if (iconFeedbackJson[index].type == 'TOO_FAST') {
          tooFastCount++;
        } else {
          tooSlowCount++;
        }
        index++;
      }
      this.#parsedIconFeedback.appendGoodCount(goodCount);
      this.#parsedIconFeedback.appendBadCount(badCount);
      this.#parsedIconFeedback.appendTooFastCount(tooFastCount);
      this.#parsedIconFeedback.appendTooSlowCount(tooSlowCount);
      // TODO: Use timestamp to seconds function here.
      this.#parsedIconFeedback.appendInterval(interval / 1000);
      interval += LoadIconFeedback.#INCREMENT_INTERVAL;
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
