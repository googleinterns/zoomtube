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

import {DiscussionComment} from './discussion-comment.js';
import DiscussionManager from './discussion-manager.js';
import {COMMENT_TYPE_QUESTION} from './discussion.js';

export const ELEMENT_DISCUSSION =
    document.querySelector('#discussion-comments');

export default class DiscussionArea {
  static #ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');
  static #ELEMENT_TIMESTAMP_SPAN = document.querySelector('#timestamp-span');
  #lecture;
  #manager;
  #currentTimeMs;
  #currentRootCommentElements;
  #nearestComments;

  constructor(lecture) {
    this.#lecture = lecture;
    this.#manager = new DiscussionManager(this.#lecture);
    this.#currentTimeMs = 0;
    this.#currentRootCommentElements = [];
    this.#nearestComments = [];
  }

  async initialize() {
    await this.loadDiscussion();
  }

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
   * Returns an array of the `DiscussionComment`s with the nearest time to
   * `timeMs`. This typically returns an array with a single element, but
   * if there are multiple comments the same distance away, they will all be
   * returned. This can also return an empty array if there are no comments.
   */
  getNearestDiscussionComments(timeMs) {
    let nearest = [];
    let nearestDistance = Infinity;
    // #currentRootCommentElements is sorted by timestamp.
    for (const element of this.#currentRootCommentElements) {
      const commentTimeMs = element.comment.timestampMs.value;
      const distance = Math.abs(timeMs - commentTimeMs);
      if (nearest.length == 0) {
        nearest = [element];
        nearestDistance = distance;
        continue;
      }
      if (distance < nearestDistance) {
        nearest = [element];
        nearestDistance = distance;
        continue;
      }
      if (distance == nearestDistance) {
        nearest.push(element);
        continue;
      }
      if (distance > nearestDistance && commentTimeMs > timeMs) {
        break;
      }
    }
    return nearest;
  }

  seek(timeMs) {
    this.#currentTimeMs = timeMs;
    DiscussionArea.#ELEMENT_TIMESTAMP_SPAN.innerText =
        timestampToString(timeMs);

    // Remove any existing highlights.
    for (const comment of this.#nearestComments) {
      comment.setHighlighted(false);
    }

    // Scroll to and highlight new nearest comments.
    this.#nearestComments = this.getNearestDiscussionComments(timeMs);
    if (this.#nearestComments.length == 0) {
      return;
    }
    this.#nearestComments[0].scrollToTopOfDiscussion();
    for (const comment of this.#nearestComments) {
      comment.setHighlighted(true);
    }
  }

  postNewComment() {
    this.#manager
        .postRootComment(
            DiscussionArea.#ELEMENT_POST_TEXTAREA.value, this.#currentTimeMs,
            COMMENT_TYPE_QUESTION)
        .then(() => {
          this.loadDiscussion();
        });
  }

  postReply(content, parentId) {
    this.#manager.postReply(content, parentId).then(() => {
      this.loadDiscussion();
    });
  }
}
