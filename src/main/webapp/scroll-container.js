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
  static #SCROLL_CONTAINER_CLASSES =
      'mx-5 my-3 bg-light pb-3 rounded smooth-scrolling';
  static #AUTO_SCROLL_MESSAGE = 'Jump back to video';
  /**
   * Scroll events within this time of each other are treated
   * as having the same trigger.
   */
  static #SCROLL_TOLERANCE_MS = 50;
  /**
   * Scroll distances greater than this (in pixels) will not be smoothly
   * animated.
   */
  static #SMOOTH_SCROLL_LIMIT = 200;

  #autoScrollIsActive;
  #scrollBanner;
  #browserScrolled;
  #currentElement;
  #lastBrowserScrollTimestampMs;

  /** Creates a `ScrollContainer`. */
  constructor() {
    super();
    this.#scrollBanner = this.createScrollBanner();
    this.appendChild(this.#scrollBanner);
    this.onscroll = this.filterScrollEvent.bind(this);
    this.className = ScrollContainer.#SCROLL_CONTAINER_CLASSES;
    this.#autoScrollIsActive = true;
    this.#lastBrowserScrollTimestampMs = 0;
  }

  /** Creates a banner for scrolling. */
  createScrollBanner() {
    const scrollBanner = document.createElement('div');
    scrollBanner.innerText = ScrollContainer.#AUTO_SCROLL_MESSAGE;
    scrollBanner.className = ScrollContainer.#SCROLL_BANNER_CLASSES;
    scrollBanner.onclick = this.startAutoScroll.bind(this);
    return scrollBanner;
  }

  /**
   * Disables automatic scrolling if the scroll `event` was
   * not triggered `this.scrollToTopOfContainer`.
   *
   * <p>With smooth scrolling, hundreds of scroll events are fired from a
   * single scroll. This filters events that occured at the same time and
   * treats them as a single event to determine if they were triggered by a user
   * or not.
   */
  filterScrollEvent(event) {
    if (this.#browserScrolled) {
      this.#lastBrowserScrollTimestampMs = event.timeStamp;
      this.#browserScrolled = false;
    }

    const timeDifferenceMs =
        event.timeStamp - this.#lastBrowserScrollTimestampMs;
    if (timeDifferenceMs < ScrollContainer.#SCROLL_TOLERANCE_MS) {
      this.#lastBrowserScrollTimestampMs = event.timeStamp;
      return;
    }

    this.stopAutoScroll();
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
    this.#browserScrolled = true;
    const innerContainer = element.parentElement;
    const scrollGoal = element.offsetTop - innerContainer.offsetTop;
    const scrollDistance = Math.abs(this.scrollTop - scrollGoal);
    if (scrollDistance > ScrollContainer.#SMOOTH_SCROLL_LIMIT) {
      this.style.scrollBehavior = 'normal';
      this.scrollTop = scrollGoal;
      this.style.scrollBehavior = 'smooth';
    }
    this.scrollTop = scrollGoal;
  }
}

customElements.define('scroll-container', ScrollContainer, {extends: 'div'});
