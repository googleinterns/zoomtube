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

import Video from '../view/video/video.js';

const ENDPOINT_FEEDBACK = '/icon-feedback';

const PARAM_LECTURE_ID = 'lectureId';
const PARAM_TIMESTAMP = 'timestamp';
const PARAM_ICON_TYPE = 'iconType';

export default class IconFeedback {
  /**
   * Sends `iconType`, video time stamp of when icon was clicked,
   * and lecture ID to be stored in database.
   */
  static iconOnClick(iconType) {
    const videoTimeStamp = window.videoPlayer.getCurrentTime();
    const url = new URL(ENDPOINT_FEEDBACK, window.location.origin);
    url.searchParams.append(PARAM_LECTURE_ID, window.LECTURE_ID);
    url.searchParams.append(PARAM_TIMESTAMP, videoTimeStamp);
    url.searchParams.append(PARAM_ICON_TYPE, iconType);
    fetch(url, {method: 'POST'});
  }
}

window.iconOnClick = IconFeedback.iconOnClick;
