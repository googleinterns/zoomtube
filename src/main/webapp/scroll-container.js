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

export class ScrollContainer extends HTMLElement {
  static #SCROLL_BANNER_CLASSES = 'scroll-banner sticky-top p-2 text-center text-white font-weight-bold';
  
  #autoScrollIsActive;
  #scrollBanner;

  constructor() {
    super();
    this.#scrollBanner = ScrollContainer.createScrollBanner();
    this.appendChild(this.#scrollBanner);
    this.onscroll = this.stopAutoScroll();
    this.#autoScrollIsActive = true;
  }

  static createScrollBanner() {
    const scrollBanner = document.createElement('div');
    scrollBanner.innerText = 'Click here to continue auto-scroll';
    scrollBanner.class = this.#SCROLL_BANNER_CLASSES;
    scrollBanner.onclick = this.startAutoScroll();
    return scrollBanner;
  }

  /** De-activates the automatic scrolling of the transcript. */
  stopAutoScroll() {
    this.#autoScrollIsActive = false;
    this.#scrollBanner.style.visibility = 'visible';
  }

  /** Re-activates the automatic scrolling of the transcript. */
  startAutoScroll() {
    this.#autoScrollIsActive = true;
    this.#scrollBanner.style.visibility = 'hidden';
  }
}

export class TranscriptScrollContainer extends ScrollContainer {
  #TRANSCRIPT_CONTAINER_CLASSES = 'mx-5 my-3 bg-light pb-3 rounded';
  #TRANSCRIPT_CONTAINER_ID = 'transcript-lines-container';
  
    constructor() {
      super();
      this.id = this.#TRANSCRIPT_CONTAINER_ID;
      this.class = this.#TRANSCRIPT_CONTAINER_CLASSES;
    }
  }