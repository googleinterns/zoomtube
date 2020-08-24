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

import Synchronizer from '../../synchronizer.js';
import {secondsToMilliseconds} from '../../timestamps.js';

const SCRIPT = 'script';

/** Initializes and stores video player information. */
export default class Video {
  #lecture;
  #synchronizer;

  /** Creates a new `Synchronizer` linked to `this`. */
  constructor(lecture) {
    this.#lecture = lecture;
    this.#synchronizer = new Synchronizer(this);
  }

  /** Loads YouTube iFrame API. */
  async loadVideoApi() {
    window.onYouTubeIframeAPIReady = this.onYouTubeIframeAPIReady.bind(this);
    window.onPlayerReady = this.onPlayerReady.bind(this);
    const videoApiScript = document.createElement(SCRIPT);
    const firstScriptTag = document.getElementsByTagName(SCRIPT)[0];
    videoApiScript.src = 'https://www.youtube.com/iframe_api';
    firstScriptTag.parentNode.insertBefore(videoApiScript, firstScriptTag);
  }

  /**
   * Creates a YouTube Video iFrame that plays lecture video after
   * the API calls it. This is a required callback from the API.
   */
  // TODO: Support dynamic video height and width.
  onYouTubeIframeAPIReady() {
    this.videoPlayer = new window.YT.Player('player', {
      height: '390',
      width: '640',
      videoId: this.#lecture.videoId,
      events: {
        onReady: window.onPlayerReady,
      },
    });
  }

  /** `event` plays the YouTube video. */
  onPlayerReady(event) {
    event.target.playVideo();
    this.#synchronizer.startVideoSyncTimer();
  }

  /** Returns current video time of 'videoPlayer' in milliseconds. */
  getCurrentVideoTimeMs() {
    return secondsToMilliseconds(this.videoPlayer.getCurrentTime());
  }

  /** Seeks video to `currentTime`. */
  seekVideo(timeMs) {
    // TODO: Removed and implement.
    console.log('SEEKING VIDEO TO: ' + timeMs);
  }
}
