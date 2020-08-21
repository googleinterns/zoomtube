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

export class ScrollContainer extends HTMLDivElement {
  static #SCROLL_BANNER_CLASSES =
      'scroll-banner sticky-top p-2 text-center text-white font-weight-bold';

  #autoScrollIsActive;
  #scrollBanner;

  constructor() {
    super();
    this.appendChild(this.createScrollBanner());
    console.log(this.getElementsByTagName('div'));
    this.onscroll = this.stopAutoScroll();
    this.#autoScrollIsActive = true;
  }

  createScrollBanner() {
    this.#scrollBanner = document.createElement('div');
    this.#scrollBanner.innerText = 'Click here to continue auto-scroll';
    this.#scrollBanner.class = ScrollContainer.#SCROLL_BANNER_CLASSES;
    this.#scrollBanner.onclick = this.startAutoScroll();
    console.log(this.#scrollBanner);
    return this.#scrollBanner;
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

customElements.define('scroll-container', ScrollContainer, {extends: 'div'});

export class TranscriptScrollContainer extends ScrollContainer {
  static #TRANSCRIPT_CONTAINER_CLASSES = 'mx-5 my-3 bg-light pb-3 rounded';
  #TRANSCRIPT_CONTAINER_ID = 'transcript-lines-container';

  constructor() {
    super();
    this.id = this.#TRANSCRIPT_CONTAINER_ID;
    this.className = TranscriptScrollContainer.#TRANSCRIPT_CONTAINER_CLASSES;
    console.log(this.class);
  }
}

customElements.define(
    'transcript-scroll-container', TranscriptScrollContainer, {extends: 'div'});
    