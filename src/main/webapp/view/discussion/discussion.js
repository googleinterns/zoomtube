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

import {secondsToMilliseconds, timestampToString} from '../../timestamps.js';

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

const COMMENT_TYPE_REPLY = 'REPLY';
const COMMENT_TYPE_QUESTION = 'QUESTION';
const COMMENT_TYPE_NOTE = 'NOTE';

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
  manager.postRootComment(
      ELEMENT_POST_TEXTAREA.value, newCommentTimestampMs,
      COMMENT_TYPE_QUESTION);
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
    const rootCommentElement = new DiscussionComment(rootComment);
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


class DiscussionManager {
  static #ENDPOINT = '/discussion';
  static #PARAM_LECTURE = 'lecture';
  static #PARAM_PARENT = 'parent';
  static #PARAM_TIMESTAMP = 'timestamp';
  static #PARAM_TYPE = 'type';
  #lecture;

  constructor(lecture) {
    this.#lecture = lecture;
  }

  /**
   * Organizes comments into threads with nested replies.
   *
   * <p>All comments are sent from the servlet without any structure, so the
   * client needs to organize them before displaying.
   */
  #structureComments(allComments) {
    const commentIds = {};
    for (const comment of allComments) {
      comment.replies = [];
      commentIds[comment.commentKey.id] = comment;
    }

    const rootComments = [];
    for (const comment of allComments) {
      if (comment.type === COMMENT_TYPE_REPLY) {
        const parent = commentIds[comment.parentKey.value.id];
        parent.replies.push(comment);
      } else {
        // Top level comments don't have parents.
        rootComments.push(comment);
      }
    }
    // Sort comments such that earliest timestamp is first.
    rootComments.sort((a, b) => (a.timestampMs.value - b.timestampMs.value));
    return rootComments;
  }

  /**
   * Fetches and returns all comments in `this.#lecture` from the `ENDPOINT`.
   * This returns a sorted array of all root comments, with replies added to
   * their parents.
   */
  async fetchRootComments() {
    const url = new URL(DiscussionManager.#ENDPOINT, window.location.origin);
    url.searchParams.append(DiscussionManager.#PARAM_LECTURE, this.#lecture.id);

    const request = await fetch(url);
    const json = await request.json();

    return this.#structureComments(json);
  }

  /**
   * Posts `content` reloads the discussion. Adds query
   * parameters from `params` to the request. Different types of comments
   * require different parameters, such as `PARAM_TIMESTAMP` or `PARAM_PARENT`.
   * The caller should ensure the correct parameters are supplied for the type
   * of comment being posted.
   */
  async #postComment(content, params) {
    const url = new URL(DiscussionManager.#ENDPOINT, window.location.origin);
    url.searchParams.append(DiscussionManager.#PARAM_LECTURE, this.#lecture.id);
    for (const param in params) {
      // This is recommended by the style guide, but disallowed by linter.
      /* eslint-disable no-prototype-builtins */
      if (params.hasOwnProperty(param)) {
        url.searchParams.append(param, params[param]);
      }
      /* eslint-enable no-prototype-builtins */
    }

    await fetch(url, {
      method: 'POST',
      body: content,
    });
  }

  /**
   * Posts `content` as a new root comment at `timestampMs` with the specified
   * `type`.
   */
  async postRootComment(content, timestampMs, type) {
    await this.#postComment(content, {
      [DiscussionManager.#PARAM_TIMESTAMP]: timestampMs,
      [DiscussionManager.#PARAM_TYPE]: type,
    });
  }

  /**
   * Posts `content` as a reply to `parentId`.
   */
  async postReply(content, parentId, callback) {
    await this.#postComment(content, {
      [DiscussionManager.#PARAM_PARENT]: parentId,
      [DiscussionManager.#PARAM_TYPE]: COMMENT_TYPE_REPLY,
    });
  }
}


/**
 * Renders a comment and its replies, with a form to post a new reply.
 */
class DiscussionComment extends HTMLElement {
  /**
   * Creates an custom HTML element representing a comment.  This uses the
   * template and slots defined by `TEMPLATE_COMMENT` to render the
   * comment's content and replies.
   *
   * @param comment The comment from the servlet that this element should
   *     render.
   */
  constructor(comment) {
    super();
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
      const textarea = this.shadowRoot.querySelector(SELECTOR_REPLY_TEXTAREA);
      postReply(textarea, this.comment.commentKey.id);
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
      replyDiv.appendChild(new DiscussionComment(reply));
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

/** Seeks discussion to `currentTimeSeconds`. */
export function seekDiscussion(currentTimeSeconds) {
  const currentTimeMilliseconds = secondsToMilliseconds(currentTimeSeconds);
  updateNewCommentTimestamp(currentTimeMilliseconds);
  const nearbyComments = getNearbyDiscussionComments(currentTimeMilliseconds);
  if (nearbyComments.length == 0) {
    return;
  }
  nearbyComments[0].scrollToTopOfDiscussion();
}
