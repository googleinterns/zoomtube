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

const ENDPOINT_FEEDBACK = '/icon-feedback';

const PARAM_LECTURE_ID = 'lectureId';
const PARAM_TIMESTAMP = 'timestampMs';
const PARAM_ICON_TYPE = 'iconType';

export default class IconFeedbackLoad {
  /**
   * Fetches avaiable Lectures from `ENDPOINT_FEEDBACK`
   * and sets them in the lecture selection page.
   */
  static async loadIconFeedbackList(lectureId) {
    const url = new URL(ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(PARAM_LECTURE_ID, lectureId);
    const response = await fetch(url);
    const jsonData = await response.json();
    const parsedData = IconFeedbackLoad.parseFeedback(jsonData);
    console.log(jsonData);
    IconFeedbackLoad.makeGraph(parsedData);
  }

  static parseFeedback(jsonData) {
    const videoDuration = 227000;
    const parsedData = [[], [], [], [], []];
    let index = 0;
    for (let interval = 0; interval < videoDuration; interval += 10000) {
      let good = 0;
      let bad = 0;
      let tooFast = 0;
      let tooSlow = 0;
      while (index < jsonData.length &&
             jsonData[index].timestampMs < interval) {
        if (jsonData[index].type == 'GOOD') {
          good = good + 1;
        } else if (jsonData[index].type == 'BAD') {
          bad = bad + 1;
        } else if (jsonData[index].type == 'TOO_FAST') {
          tooFast = tooFast + 1;
        } else {
          tooSlow = tooSlow + 1;
        }
        index++;
      }
      parsedData[0].push(good);
      parsedData[1].push(bad);
      parsedData[2].push(tooFast);
      parsedData[3].push(tooSlow);
      parsedData[4].push(interval / 1000);
    }
    console.log(parsedData);
    return parsedData;
  }

  static makeGraph(parsedData) {
    var ctx = document.getElementById('myChart')
    var myLineChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: parsedData[4],
        datasets: [
          {
            label: 'GOOD',
            backgroundColor: window.chartColors.red,
            borderColor: window.chartColors.red,
            data: parsedData[0],
            fill: false,
          },
          {
            label: 'BAD',
            backgroundColor: window.chartColors.blue,
            borderColor: window.chartColors.blue,
            data: parsedData[1],
            fill: false,
          },
          {
            label: 'TOO_FAST',
            backgroundColor: window.chartColors.orange,
            borderColor: window.chartColors.orange,
            data: parsedData[2],
            fill: false,
          },
          {
            label: 'TOO_SLOW',
            backgroundColor: window.chartColors.green,
            borderColor: window.chartColors.green,
            data: parsedData[3],
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

IconFeedbackLoad.loadIconFeedbackList(lectureId);

/**
 * Returns the lecture id from `urlSearchParams`.
 */
function getLectureId(urlSearchParams) {
  const urlParams = new URLSearchParams(urlSearchParams);
  return urlParams.get(PARAM_ID);
}
