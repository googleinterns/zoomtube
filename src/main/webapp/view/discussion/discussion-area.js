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

import {timestampToString} from '../../timestamps.js';
import DiscussionManager from './discussion-manager.js';
import {COMMENT_TYPE_QUESTION} from './discussion.js';
import {DiscussionComment} from './discussion.js';

export const ELEMENT_DISCUSSION =
    document.querySelector('#discussion-comments');

/*
 * Displays the entire Discussion Area UI, and implements posting
 * new comments and loading existing ones to the current lecture.
 */
export default class DiscussionArea {
  static #ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');
  static #ELEMENT_TIMESTAMP_SPAN = document.querySelector('#timestamp-span');
  static #TIME_TOLERANCE_MS = 10000;  // 10 seconds.
  #lecture;
  #manager;
  #currentTimeMs;
  #currentRootCommentElements;

  /**
   * @param lecture The lecture that this discussion is about.
   */
  constructor(lecture) {
    this.#lecture = lecture;
    this.#manager = new DiscussionManager(this.#lecture);
    this.#currentTimeMs = 0;
    this.#currentRootCommentElements = [];
  }

  /**
   * Initialize the discussion area by loading the current comments.
   */
  async initialize() {
    await this.loadDiscussion();
  }

  /**
   * Fetches and displays the current comments.
   */
  async loadDiscussion() {
    // Clear any existing comments before loading.
    ELEMENT_DISCUSSION.textContent = '';
    this.#currentRootCommentElements = [];

    const rootComments = await this.#manager.fetchRootComments();
    for (const rootComment of rootComments) {
      const rootCommentElement = new DiscussionComment(rootComment, this);
      this.#currentRootCommentElements.push(rootCommentElement);
      ELEMENT_DISCUSSION.appendChild(rootCommentElement);
    }
  }

  /**
   * Returns an array of the `DiscussionComment`s with timestamps near
   * `timestampMs`. This returns an empty array if no elements are nearby.
   *
   * <p>A comment is nearby if it is within `TIME_TOLERANCE_MS`.
   */
  getNearbyDiscussionComments(timestampMs) {
    const nearby = [];
    // currentRootCommentElements is already sorted by timestamp.
    for (const element of this.#currentRootCommentElements) {
      const commentTime = element.comment.timestampMs.value;
      if (commentTime < timestampMs - DiscussionArea.#TIME_TOLERANCE_MS) {
        // Before the start of the range, continue to next.
        continue;
      }
      if (commentTime > timestampMs + DiscussionArea.#TIME_TOLERANCE_MS) {
        // Outside of range, there will be no more.
        return nearby;
      }
      nearby.push(element);
    }
    return nearby;
  }

  /**
   * Seeks the discussion area to `timeMs`.  This involves scrolling the
   * comments, and updating the time displayed in the new comment area.
   */
  seek(timeMs) {
    this.#currentTimeMs = timeMs;
    DiscussionArea.#ELEMENT_TIMESTAMP_SPAN.innerText =
        timestampToString(timeMs);
    const nearbyComments = this.getNearbyDiscussionComments(timeMs);
    if (nearbyComments.length == 0) {
      return;
    }
    nearbyComments[0].scrollToTopOfDiscussion();
  }

  /**
   * Posts the comment in the new comment area, and reloads the discussion.
   */
  postNewComment() {
    this.#manager
        .postRootComment(
            DiscussionArea.#ELEMENT_POST_TEXTAREA.value, this.#currentTimeMs,
            COMMENT_TYPE_QUESTION)
        .then(() => {
          this.loadDiscussion();
        });
  }

  /**
   * Posts `content` as a reply to `parentId`, and reloads the discussion.
   */
  postReply(content, parentId) {
    this.#manager.postReply(content, parentId).then(() => {
      this.loadDiscussion();
    });
  }
}
