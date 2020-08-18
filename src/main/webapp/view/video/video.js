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

import {startVideoSyncTimer} from '../../synchronizer.js';

const SCRIPT = 'script';

export default class Video {
  constructor() {
    console.log('Constructor called.');
    this.loadVideoApi();
  }

  /** Loads YouTube iFrame API. */
  async loadVideoApi() {
    console.log('LoadVideoApi called.');
    window.onYouTubeIframeAPIReady = this.onYouTubeIframeAPIReady();
    const videoApiScript = document.createElement(SCRIPT);
    const firstScriptTag = document.getElementsByTagName(SCRIPT)[0];
    videoApiScript.src = 'https://www.youtube.com/iframe_api';
    firstScriptTag.parentNode.insertBefore(videoApiScript, firstScriptTag);
  }

  /**
   * Creates a YouTube Video iFrame that plays lecture video after
   * the API calls it. This is a required callback from the API.
   */
  // TODO: Change height and width.
  onYouTubeIframeAPIReady() {
    console.log('onYouTubeIframeAPIReady called.');
    const videoPlayer = new window.YT.Player('player', {
      height: '390',
      width: '640',
      videoId: window.LECTURE.videoId,
      events: {
        onReady: window.video.onPlayerReady,
      },
    });
  }

  /** `event` plays the YouTube video. */
  onPlayerReady(event) {
    event.target.playVideo();
    startVideoSyncTimer();
  }

  /** Seeks video to `currentTime`. */
  seekVideo(currentTime) {
    // TODO: Removed and implement.
    console.log('SEEKING VIDEO TO: ' + currentTime);
  }
}