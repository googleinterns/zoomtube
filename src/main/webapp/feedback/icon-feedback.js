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
    const feedbackArray = IconFeedback.parseFeedback(jsonData);
    console.log(feedbackArray);
  }

  static parseFeedback(jsonData) {
    const videoDuration = window.video.getVideoDurationMs();
    let parsedData = [];
    let index = 0;
    for (let interval = 0; interval < videoDuration; interval += 10000) {
      let good = ['GOOD', 0, interval];
      let bad = ['BAD', 0, interval];
      let too_fast = ['TOO_FAST', 0, interval];
      let too_slow = ['TOO_SLOW', 0, interval];
      while (index < jsonData.length &&
             jsonData[index].timestampMs < interval) {
        if (jsonData.type == good[0]) {
          good[1] = good[1] + 1;
        } else if (jsonData.type == bad[0]) {
          bad[1] = bad[1] + 1;
        } else if (jsonData.type == too_fast[0]) {
          too_fast[1] = too_fast[1] + 1;
        } else {
          too_slow[1] = too_slow[1] + 1;
        }
        index++;
      }
      parsedData.push(good);
      parsedData.push(bad);
      parsedData.push(too_fast);
      parsedData.push(too_slow);
    }
    return parsedData;
  }
}

window.iconOnClick = IconFeedback.iconOnClick;
