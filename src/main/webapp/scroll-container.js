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

/**
 * Responsible for hiding and displaying a clickable banner to
 * restart automatic scrolling.
 *
 * <p>Automatic scrolling is when the elements in the container are scrolled
 * to the top of the container automatically.
 */
export class ScrollContainer extends HTMLDivElement {
  static #SCROLL_BANNER_CLASSES =
      'scroll-banner sticky-top p-2 text-center text-white font-weight-bold';
  static #SCROLL_CONTAINER_CLASSES = 'mx-5 my-3 bg-light pb-3 rounded';
  static #AUTO_SCROLL_MESSAGE = 'Jump back to video';

  #autoScrollIsActive;
  #scrollBanner;
  #browserScrolled;
  #currentElement;

  /** Creates a `ScrollContainer`. */
  constructor() {
    super();
    this.#scrollBanner = this.createScrollBanner();
    this.appendChild(this.#scrollBanner);
    this.onscroll = this.stopAutoScroll.bind(this);
    this.className = ScrollContainer.#SCROLL_CONTAINER_CLASSES;
    this.#autoScrollIsActive = true;
  }

  /** Creates a banner for scrolling. */
  createScrollBanner() {
    const scrollBanner = document.createElement('div');
    scrollBanner.innerText = ScrollContainer.#AUTO_SCROLL_MESSAGE;
    scrollBanner.className = ScrollContainer.#SCROLL_BANNER_CLASSES;
    scrollBanner.onclick = this.startAutoScroll.bind(this);
    return scrollBanner;
  }

  /** De-activates the automatic scrolling of the transcript. */
  stopAutoScroll() {
    if (this.#browserScrolled) {
      this.#browserScrolled = false;
      return;
    }
    this.#autoScrollIsActive = false;
    this.#scrollBanner.style.visibility = 'visible';
  }

  /** Re-activates the automatic scrolling of the transcript. */
  startAutoScroll() {
    this.#autoScrollIsActive = true;
    this.#scrollBanner.style.visibility = 'hidden';
    this.scrollToTopOfContainer(this.#currentElement);
  }

  /**
   * If automatic scrolling is enabled or `forceScroll` is true, then scrolls
   * the container such that `element` is at the top of the container.
   * Otherwise, does nothing.
   *
   */
  scrollToTopOfContainer(element, forceScroll = false) {
    this.#currentElement = element;
    if (!this.#autoScrollIsActive || forceScroll) {
      return;
    }
    const innerContainer = element.parentElement;
    this.scrollTop = element.offsetTop - innerContainer.offsetTop;
    this.#browserScrolled = true;
  }
}

customElements.define('scroll-container', ScrollContainer, {extends: 'div'});
