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
  static #SCROLL_CONTAINER_CLASSES = 'mx-5 my-3 bg-light pb-3 rounded';

  #autoScrollIsActive;
  #scrollBanner;

  constructor() {
    super();
    this.appendChild(this.createScrollBanner());
    this.onscroll = function() {
      this.stopAutoScroll();
    };
    this.className = ScrollContainer.#SCROLL_CONTAINER_CLASSES;
    this.#autoScrollIsActive = true;
  }

  /** Creates a banner for scrolling.*/
  createScrollBanner() {
    this.#scrollBanner = document.createElement('div');
    this.#scrollBanner.innerText = 'Click here to continue auto-scroll';
    this.#scrollBanner.className = ScrollContainer.#SCROLL_BANNER_CLASSES;
    const container = this;
    this.#scrollBanner.onclick = function() {
      container.startAutoScroll();
    };
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
