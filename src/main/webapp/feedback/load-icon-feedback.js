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

import TimestampUtil from '../timestamp-util.js';
import IconFeedbackUtil from './icon-feedback-util.js';
import ParsedIconFeedback from './parsed-icon-feedback.js';

/* Handles loading IconFeedback from database and parses data for graph. */
export default class LoadIconFeedback {
  static #ENDPOINT_FEEDBACK = '/icon-feedback';
  static #PARAM_LECTURE_ID = 'lectureId';

  /* Each interval is 10 seconds, used to increment interval. */
  static #INCREMENT_INTERVAL_MS = 10000;

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
   * Fetches available IconFeedback from `ENDPOINT_FEEDBACK`
   * and parses the data for it to be graphed.
   */
  async loadIconFeedbackList() {
    const url =
        new URL(LoadIconFeedback.#ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(
        LoadIconFeedback.#PARAM_LECTURE_ID, this.#lectureId);
    const response = await fetch(url);
    const jsonData = await response.json();
    console.log(jsonData);
    this.parseFeedback(jsonData);
  }

  /**
   * Parses `iconFeedbackJson` by couting how many times each IconFeedback type
   * is clicked in each 10 second interval and stores that data in a
   * ParseIconFeedback object.
   */
  parseFeedback(iconFeedbackJson) {
    let intervalLowerBound = LoadIconFeedback.#INCREMENT_INTERVAL_MS;
    let typeCountsAndInterval = this.makeCountDictionary(intervalLowerBound);
    for (let index = 0; index < iconFeedbackJson.length;) {
      const iconFeedback = iconFeedbackJson[index];
      if (intervalLowerBound < iconFeedback.timestampMs &&
          iconFeedback.timestampMs >
              intervalLowerBound + LoadIconFeedback.#INCREMENT_INTERVAL_MS) {
        this.#parsedIconFeedback.appendTypeCountsAndInterval(
            typeCountsAndInterval);
        intervalLowerBound += LoadIconFeedback.#INCREMENT_INTERVAL_MS;
        typeCountsAndInterval = this.makeCountDictionary(intervalLowerBound);
      } else {
        const type = iconFeedback.type;
        typeCountsAndInterval[type]++;
        index++;
      }
    }
    this.#parsedIconFeedback.appendTypeCountsAndInterval(typeCountsAndInterval);
  }

  /**
   * Returns a dictionary mapping each icon type to a value which represents
   * how many times that icon was clicked in an `intervalLowerBound`.
   */
  makeCountDictionary(intervalLowerBound) {
    return {
      [IconFeedbackUtil.TYPE_GOOD]: 0,
      [IconFeedbackUtil.TYPE_BAD]: 0,
      [IconFeedbackUtil.TYPE_TOO_FAST]: 0,
      [IconFeedbackUtil.TYPE_TOO_SLOW]: 0,
      [IconFeedbackUtil.INTERVAL]:
          TimestampUtil.millisecondsToSeconds(intervalLowerBound),
    };
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
