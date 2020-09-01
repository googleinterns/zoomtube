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
import TimestampUtil from '../../timestamp-util.js';
import DiscussionComment from './discussion-comment.js';
import DiscussionManager from './discussion-manager.js';
import {COMMENT_TYPE_REPLY} from './discussion.js';


/*
 * Displays the entire Discussion Area UI, and implements posting
 * new comments and loading existing ones to the current lecture.
 */
export default class DiscussionArea {
  static #ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');
  static #ELEMENT_TIMESTAMP_SPAN = document.querySelector('#timestamp-span');
  static #ELEMENT_DISCUSSION = document.querySelector('#discussion');
  static #ID_DISCUSSION_CONTAINER = 'discussion-comments';
  static #ELEMENT_NEW_COMMENT_TYPES =
      document.querySelector('#new-comment-types');
  static #ELEMENT_LOADING_SPINNER =
      DiscussionArea.#ELEMENT_DISCUSSION.querySelector('.spinner-border');

  /**
   * A selector to query on `ELEMENT_NEW_COMMENT_TYPES`. It returns the
   * selected type button in the new comment area.
   */
  static #SELECTOR_SELECTED_TYPE = 'label.active > input';

  #lecture;
  #eventController;
  #manager;
  #currentTimeMs;
  #nearestComments;
  #transcriptSeeker;
  #scrollContainer;
  #discussionCommentsDiv;

  /**
   * Creates a `DiscussionArea` for a `lecture`
   * with a `transcriptSeeker`.
   */
  constructor(lecture, eventController, transcriptSeeker) {
    this.#lecture = lecture;
    this.#eventController = eventController;
    this.#manager = new DiscussionManager(this.#lecture);
    this.#currentTimeMs = 0;
    this.#nearestComments = [];
    this.#transcriptSeeker = transcriptSeeker;
  }

  /**
   * Adds event listener for seeking and initializes the discussion area by
   * loading the current comments.
   */
  async initialize() {
    this.#scrollContainer = new ScrollContainer();
    this.#scrollContainer.id = DiscussionArea.#ID_DISCUSSION_CONTAINER;
    this.#discussionCommentsDiv = document.createElement('div');
    this.#scrollContainer.appendChild(this.#discussionCommentsDiv);
    DiscussionArea.#ELEMENT_DISCUSSION.appendChild(this.#scrollContainer);
    this.addSeekingListener();
    // This is used as the `onclick` handler of the new comment area submit
    // button. It must be set after discussion is initialized.
    window.postNewComment = this.postNewComment.bind(this);
    await this.updateDiscussion();
    DiscussionArea.#ELEMENT_DISCUSSION.removeChild(
        DiscussionArea.#ELEMENT_LOADING_SPINNER);
  }

  /**
   * Adds event listener allowing seeking discussion area
   * on event broadcast.
   */
  addSeekingListener() {
    this.#eventController.addEventListener((timestampMs) => {
      this.seek(timestampMs);
    }, 'seek', 'seekAll');
  }

  /**
   * Seeks the transcript, discussion, and video to `timestampMs`.
   *
   * <p>This should be added as an event listener to every root
   * discussion header's onclick event.
   */
  onCommentHeaderClicked(timestampMs) {
    // TODO: Enable scroll container autoscroll.
    this.#eventController.broadcastEvent('seekAll', timestampMs);
  }

  /**
   * Fetches and updates the currently displayed comments.
   */
  async updateDiscussion() {
    const newComments = await this.#manager.fetchNewComments();
    if (newComments.length == 0) {
      return;
    }

    // Create a new element for every new comment.
    // This must be done first for all comments because comment order is
    // not guarenteed.
    for (const comment of newComments) {
      const commentElement = new DiscussionComment(this);
      commentElement.setComment(comment);
      comment.element = commentElement;
    }

    // Insert comments.
    for (const comment of newComments) {
      if (comment.type === COMMENT_TYPE_REPLY) {
        comment.parent.element.insertReply(comment);
        continue;
      }
      this.insertRootComment(comment);
    }

    // It's possible that the comment we should be seeked to has changed,
    // so we run seek again.
    this.seek(this.#currentTimeMs);
  }

  /**
   * Inserts a new root comment into the DOM, maintaining order by timestamp.
   */
  insertRootComment(newComment) {
    const newCommentTimeMs = newComment.timestampMs.value;
    // For now, we use a linear search. This can be improved if it becomes
    // an issue.
    for (const commentElement of this.#discussionCommentsDiv.children) {
      const commentTimeMs = commentElement.comment.timestampMs.value;
      if (commentTimeMs >= newCommentTimeMs) {
        commentElement.before(newComment.element);
        return;
      }
    }
    // If it isn't before any existing comments, it must belong at the end.
    this.#discussionCommentsDiv.appendChild(newComment.element);
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
    // this.#discussionCommentsDiv is sorted by timestamp.
    for (const element of this.#discussionCommentsDiv.children) {
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
        TimestampUtil.timestampToString(timeMs);

    this.unhightlightNearestComments();
    this.#nearestComments = this.getNearestDiscussionComments(timeMs);
    if (this.#nearestComments.length > 0) {
      this.#scrollContainer.scrollToTopOfContainer(this.#nearestComments[0]);
    }
    this.highlightNearestComments();
  }

  /**
   * Posts the comment in the new comment area, and updates the discussion.
   */
  postNewComment() {
    const commentContent = DiscussionArea.#ELEMENT_POST_TEXTAREA.value;
    const commentTimestampMs = this.#currentTimeMs;
    /* eslint-disable indent */
    const commentType =
        DiscussionArea.#ELEMENT_NEW_COMMENT_TYPES
            .querySelector(DiscussionArea.#SELECTOR_SELECTED_TYPE)
            .value;
    /* eslint-enable indent */

    const currentTranscripLineId =
        this.#transcriptSeeker.currentTranscriptLine();

    this.#manager
        .postRootComment(
            commentContent, commentTimestampMs, commentType,
            currentTranscripLineId)
        .then(() => {
          this.updateDiscussion();
        });

    DiscussionArea.#ELEMENT_POST_TEXTAREA.value = '';
  }

  /**
   * Posts `content` as a reply to `parentId`, and updates the discussion.
   */
  postReply(content, parentId) {
    this.#manager.postReply(content, parentId).then(() => {
      this.updateDiscussion();
    });
  }
}
