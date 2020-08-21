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

export const COMMENT_TYPE_REPLY = 'REPLY';
export const COMMENT_TYPE_QUESTION = 'QUESTION';
export const COMMENT_TYPE_NOTE = 'NOTE';

const ELEMENT_DISCUSSION = document.querySelector('#discussion-comments');
const ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');
const ELEMENT_TIMESTAMP_SPAN = document.querySelector('#timestamp-span');

const TEMPLATE_COMMENT = document.querySelector('#comment-template');

const SLOT_HEADER = 'header';
const SLOT_CONTENT = 'content';
const SLOT_REPLIES = 'replies';

const SELECTOR_SHOW_REPLY = '#show-reply';
const SELECTOR_REPLY_FORM = '#reply-form';
const SELECTOR_CANCEL_REPLY = '#cancel-reply';
const SELECTOR_POST_REPLY = '#post-reply';
const SELECTOR_REPLY_TEXTAREA = '#reply-textarea';

// 10 seconds.
const TIME_TOLERANCE_MS = 10000;

// TODO: Refactor these global variables into a namespace, module, or class.
// See: #191.
let newCommentTimestampMs = 0;
let /** !Array<DiscussionComment> */ currentRootDiscussionComments = [];

/**
 * Loads the lecture disucssion.
 */
export async function intializeDiscussion() {
  await loadDiscussion();
}

// This is used as the `onclick` handler of the new comment area submit button.
window.postNewComment = () => {
  const manager = new DiscussionManager(window.LECTURE);
  // TODO: Add support for submitting types other than QUESTION.
  manager
      .postRootComment(
          ELEMENT_POST_TEXTAREA.value, newCommentTimestampMs,
          COMMENT_TYPE_QUESTION)
      .then(() => {
        loadDiscussion();
      });
};

/**
 * Adds comments to the discussion element.
 */
async function loadDiscussion() {
  // Clear any existing comments before loading.
  ELEMENT_DISCUSSION.textContent = '';
  currentRootDiscussionComments = [];

  const manager = new DiscussionManager(window.LECTURE);
  const rootComments = await manager.fetchRootComments();
  for (const rootComment of rootComments) {
    const rootCommentElement = new DiscussionComment(rootComment, manager);
    currentRootDiscussionComments.push(rootCommentElement);
    ELEMENT_DISCUSSION.appendChild(rootCommentElement);
  }
}

/**
 * Updates the timestamp displayed and sent by the new comment form.
 *
 * @param {number} timeMs The new time in milliseconds to use.
 */
function updateNewCommentTimestamp(timeMs) {
  ELEMENT_TIMESTAMP_SPAN.innerText = timestampToString(timeMs);
  newCommentTimestampMs = timeMs;
}

/**
 * Returns an array of the `DiscussionComment`s with timestamps near
 * `timestampMs`. This returns an empty array if no elements are nearby.
 *
 * <p>A comment is nearby if it is within `TIME_TOLERANCE_MS`.
 */
function getNearbyDiscussionComments(timestampMs) {
  const nearby = [];
  // currentRootDiscussionComments is already sorted by timestamp.
  for (const element of currentRootDiscussionComments) {
    const commentTime = element.comment.timestampMs.value;
    if (commentTime < timestampMs - TIME_TOLERANCE_MS) {
      // Before the start of the range, continue to next.
      continue;
    }
    if (commentTime > timestampMs + TIME_TOLERANCE_MS) {
      // Outside of range, there will be no more.
      return nearby;
    }
    nearby.push(element);
  }
  return nearby;
}

/**
 * Renders a comment and its replies, with a form to post a new reply.
 */
// TODO: PR #264 moves this to a new file.
class DiscussionComment extends HTMLElement {
  #manager;

  /**
   * Creates an custom HTML element representing a comment.  This uses the
   * template and slots defined by `TEMPLATE_COMMENT` to render the
   * comment's content and replies.
   *
   * @param comment The comment from the servlet that this element should
   *     render.
   * @param {DiscussionManager} manager The current discussion's manager.
   */
  constructor(comment, manager) {
    super();
    this.#manager = manager;
    this.comment = comment;
    this.attachShadow({mode: 'open'});
    const shadow = TEMPLATE_COMMENT.content.cloneNode(true);
    this.shadowRoot.appendChild(shadow);

    this.setSlotSpan(SLOT_HEADER, this.getHeaderString(comment));
    this.setSlotSpan(SLOT_CONTENT, comment.content);
    this.addReplies(comment.replies);

    this.addReplyEventListeners();
  }

  /**
   * Returns a string containing the timestamp, author, and creation time of
   * the `comment`.  The timestamp is not displayed for replies to
   * other comments.
   */
  getHeaderString(comment) {
    const username = comment.author.email.split('@')[0];
    let timestampPrefix = '';
    if (comment.type !== COMMENT_TYPE_REPLY) {
      // Don't show timestamp on replies.
      timestampPrefix = `${timestampToString(comment.timestampMs.value)} - `;
    }
    return `${timestampPrefix}${username} on ${comment.created}`;
  }

  /**
   * Adds event listeners and handlers for the reply button and form.
   * This makes the form open when "Reply" is pressed, submit when
   * "Post" is pressed, and close when "Cancel" is pressed.
   */
  addReplyEventListeners() {
    const replyForm = this.shadowRoot.querySelector(SELECTOR_REPLY_FORM);
    this.shadowRoot.querySelector(SELECTOR_SHOW_REPLY).onclick = () => {
      $(replyForm).collapse('show');
    };
    this.shadowRoot.querySelector(SELECTOR_CANCEL_REPLY).onclick = () => {
      $(replyForm).collapse('hide');
    };
    this.shadowRoot.querySelector(SELECTOR_POST_REPLY).onclick = () => {
      // TODO: Make this its own function.
      const textarea = this.shadowRoot.querySelector(SELECTOR_REPLY_TEXTAREA);
      this.#manager.postReply(textarea.value, this.comment.commentKey.id)
          .then(() => {
            loadDiscussion();
          });
    };
  }

  /**
   * Creates a `DiscussionComment` for every reply to this comment, and
   * adds them to a `<div>` in the replies slot of the DOM template.
   */
  addReplies(replies) {
    const replyDiv = document.createElement('div');
    replyDiv.slot = SLOT_REPLIES;
    for (const reply of replies) {
      replyDiv.appendChild(new DiscussionComment(reply, this.#manager));
    }
    this.appendChild(replyDiv);
  }

  /**
   * Sets the content of the shadow-dom slot named `name` to a span
   * element containing `value` as text.
   */
  setSlotSpan(name, value) {
    const span = document.createElement('span');
    span.innerText = value;
    span.slot = name;
    this.appendChild(span);
  }

  /**
   * Scroll such that this element is at the top of the discussion area.
   */
  scrollToTopOfDiscussion() {
    const scrollPaneTop = ELEMENT_DISCUSSION.offsetTop;
    const elementTop = this.offsetTop;
    const offset = elementTop - scrollPaneTop;
    ELEMENT_DISCUSSION.scrollTop = offset;
  }
}

// Custom element names must contain a hyphen.
customElements.define('discussion-comment', DiscussionComment);

/** Seeks discussion to `timeMs`. */
export function seekDiscussion(timeMs) {
  updateNewCommentTimestamp(timeMs);
  const nearbyComments = getNearbyDiscussionComments(timeMs);
  if (nearbyComments.length == 0) {
    return;
  }
  nearbyComments[0].scrollToTopOfDiscussion();
}
