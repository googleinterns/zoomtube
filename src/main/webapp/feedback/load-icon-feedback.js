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
    this.parseFeedback(jsonData);
    console.log(this.#parsedIconFeedback);
    this.makeGraph();
  }

  /**
   * Parses `iconFeedbackJson` by couting how many times each IconFeedback type
   * is clicked in each 10 second interval and stores that data in a
   * ParseIconFeedback object.
   */
  parseFeedback(iconFeedbackJson) {
    let intervalLowerBound = 0;
    let typeCountsAndInterval = this.makeCountDictionary(intervalLowerBound);
    for (const iconFeedback of iconFeedbackJson) {
      if (intervalLowerBound < iconFeedback.timestampMs) {
        this.#parsedIconFeedback.appendTypeCountsAndInterval(
            typeCountsAndInterval);
        intervalLowerBound += LoadIconFeedback.#INCREMENT_INTERVAL_MS;
        typeCountsAndInterval = this.makeCountDictionary(intervalLowerBound);
      } else {
        const type = iconFeedback.type;
        typeCountsAndInterval[type]++;
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

  makeGraph(parsedData) {
    window.chartColors = {
      red: 'rgb(255, 99, 132)',
      orange: 'rgb(255, 159, 64)',
      yellow: 'rgb(255, 205, 86)',
      green: 'rgb(75, 192, 192)',
      blue: 'rgb(54, 162, 235)',
      purple: 'rgb(153, 102, 255)',
      grey: 'rgb(201, 203, 207)',
    };

    var ctx = document.getElementById('myChart')
    var myLineChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: this.#parsedIconFeedback.getInterval(),
        datasets: [
          {
            label: 'GOOD',
            backgroundColor: window.chartColors.red,
            borderColor: window.chartColors.red,
            data: this.#parsedIconFeedback.getGoodCounts(),
            fill: false,
          },
          {
            label: 'BAD',
            backgroundColor: window.chartColors.blue,
            borderColor: window.chartColors.blue,
            data: this.#parsedIconFeedback.getBadCounts(),
            fill: false,
          },
          {
            label: 'TOO_FAST',
            backgroundColor: window.chartColors.orange,
            borderColor: window.chartColors.orange,
            data: this.#parsedIconFeedback.getTooFastCounts(),
            fill: false,
          },
          {
            label: 'TOO_SLOW',
            backgroundColor: window.chartColors.green,
            borderColor: window.chartColors.green,
            data: this.#parsedIconFeedback.getTooSlowCounts(),
            fill: false,
          },
        ],
      },
      options: {
        responsive: true,
        title: {
          display: true,
          text: 'Icon Feedback Line Chart',
        },
        tooltips: {
          mode: 'index',
          intersect: false,
        },
        hover: {
          mode: 'nearest',
          intersect: true,
        },
        scales: {
          xAxes: [
            {
              display: true,
              scaleLabel: {
                display: true,
                labelString: 'Timestamp (seconds)',
              },
            },
          ],
          yAxes: [
            {
              display: true,
              scaleLabel: {
                display: true,
                labelString: 'Number of clicks',
              },
            },
          ],
        },
      },
    });
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
