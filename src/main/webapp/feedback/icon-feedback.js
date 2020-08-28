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

export default class IconFeedback {
  /**
   * Sends `iconType`, video timestamp of when icon was clicked,
   * and lecture ID to be stored in database.
   */
  static iconOnClick(iconType) {
    const videoTimeStamp = window.video.getCurrentVideoTimeMs();
    const url = new URL(ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(PARAM_LECTURE_ID, window.LECTURE_ID);
    url.searchParams.append(PARAM_TIMESTAMP, videoTimeStamp);
    url.searchParams.append(PARAM_ICON_TYPE, iconType);
    fetch(url, {method: 'POST'});
  }

  /**
   * Fetches avaiable Lectures from `ENDPOINT_FEEDBACK`
   * and sets them in the lecture selection page.
   */
  static async loadIconFeedbackList() {
    const url = new URL(ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(PARAM_LECTURE_ID, window.LECTURE_ID);
    const response = await fetch(url);
    const jsonData = await response.json();
    IconFeedback.parseFeedback(jsonData);
  }

  static parseFeedback(jsonData) {
    const videoDuration = window.video.getVideoDurationMs();
    const parsedData = [[], [], [], []];
    let index = 0;
    for (let interval = 0; interval < videoDuration; interval += 10000) {
      const good = [interval / 1000, 0];
      const bad = [interval / 1000, 0];
      const tooFast = [interval / 1000, 0];
      const tooSlow = [interval / 1000, 0];
      while (index < jsonData.length &&
             jsonData[index].timestampMs < interval) {
        if (jsonData.type == 'GOOD') {
          good[1] = good[1] + 1;
        } else if (jsonData.type == 'BAD') {
          bad[1] = bad[1] + 1;
        } else if (jsonData.type == 'TOO_FAST') {
          tooFast[1] = tooFast[1] + 1;
        } else {
          tooSlow[1] = tooSlow[1] + 1;
        }
        index++;
      }
      parsedData[0].push(good);
      parsedData[1].push(bad);
      parsedData[2].push(tooFast);
      parsedData[3].push(tooSlow);
    }
    return parsedData;
  }
}

window.iconOnClick = IconFeedback.iconOnClick;
