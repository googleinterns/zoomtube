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

import {ScrollContainer} from '../../scroll-container.js';
import {timestampToString} from '../../timestamps.js';
import DiscussionComment from './discussion-comment.js';
import DiscussionManager from './discussion-manager.js';
import {COMMENT_TYPE_QUESTION} from './discussion.js';


/*
 * Displays the entire Discussion Area UI, and implements posting
 * new comments and loading existing ones to the current lecture.
 */
export default class DiscussionArea {
  static #ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');
  static #ELEMENT_TIMESTAMP_SPAN = document.querySelector('#timestamp-span');
  static #ELEMENT_DISCUSSION = document.querySelector('#discussion');
  static #ID_DISCUSSION_CONTAINER = 'discussion-comments';
  #lecture;
  #manager;
  #currentTimeMs;
  #currentRootCommentElements;
  #nearestComments;
  #scrollContainer;
  #discussionCommentsDiv;

  /**
   * Creates a `DiscussionArea` for a `lecture`.
   */
  constructor(lecture) {
    this.#lecture = lecture;
    this.#manager = new DiscussionManager(this.#lecture);
    this.#currentTimeMs = 0;
    this.#currentRootCommentElements = [];
    this.#nearestComments = [];
  }

  /**
   * Initialize the discussion area by loading the current comments.
   */
  async initialize() {
    this.#scrollContainer = new ScrollContainer();
    this.#scrollContainer.id = DiscussionArea.#ID_DISCUSSION_CONTAINER;
    this.#discussionCommentsDiv = document.createElement('div');
    this.#scrollContainer.appendChild(this.#discussionCommentsDiv);
    DiscussionArea.#ELEMENT_DISCUSSION.appendChild(this.#scrollContainer);
    await this.loadDiscussion();
  }

  /**
   * Fetches and displays the current comments.
   */
  async loadDiscussion() {
    // Clear any existing comments before loading.
    this.#discussionCommentsDiv.textContent = '';
    this.#currentRootCommentElements = [];

    const rootComments = await this.#manager.fetchRootComments();
    for (const rootComment of rootComments) {
      const rootCommentElement = new DiscussionComment(this);
      rootCommentElement.setComment(rootComment);
      this.#currentRootCommentElements.push(rootCommentElement);
      this.#discussionCommentsDiv.appendChild(rootCommentElement);
    }
  }

  /**
   * Returns an array of the `DiscussionComment`s with the nearest time to
   * `timeMs`.
   *
   * <p>This typically returns an array with a single element, but
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

  /**
   * Removes highlights on nearest comments.
   */
  unhightlightNearestComments() {
    this.#nearestComments.forEach((comment) => comment.unhighlight());
  }

  /**
   * Highlights nearest comments.
   */
  highlightNearestComments() {
    this.#nearestComments.forEach((comment) => comment.highlight());
  }

  /**
   * Seeks discussion to `timeMs`.
   */
  seek(timeMs) {
    this.#currentTimeMs = timeMs;
    DiscussionArea.#ELEMENT_TIMESTAMP_SPAN.innerText =
        timestampToString(timeMs);

    this.unhightlightNearestComments();
    this.#nearestComments = this.getNearestDiscussionComments(timeMs);
    if (this.#nearestComments.length > 0) {
      this.#scrollContainer.scrollToTopOfContainer(this.#nearestComments[0]);
    }
    this.highlightNearestComments();
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
