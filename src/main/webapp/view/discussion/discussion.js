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

import DiscussionArea, {ELEMENT_DISCUSSION} from './discussion-area.js';

export const COMMENT_TYPE_REPLY = 'REPLY';
export const COMMENT_TYPE_QUESTION = 'QUESTION';
export const COMMENT_TYPE_NOTE = 'NOTE';

const TEMPLATE_COMMENT = document.querySelector('#comment-template');

const SLOT_HEADER = 'header';
const SLOT_CONTENT = 'content';
const SLOT_REPLIES = 'replies';

const SELECTOR_SHOW_REPLY = '#show-reply';
const SELECTOR_REPLY_FORM = '#reply-form';
const SELECTOR_CANCEL_REPLY = '#cancel-reply';
const SELECTOR_POST_REPLY = '#post-reply';
const SELECTOR_REPLY_TEXTAREA = '#reply-textarea';

export let /** DiscussionArea */ discussion;

/**
 * Loads the lecture discussion.
 */
export async function intializeDiscussion() {
  discussion = new DiscussionArea(window.LECTURE);
  discussion.initialize();
  // This is used as the `onclick` handler of the new comment area submit
  // button. It must be set after discussion is initialized.
  window.postNewComment = discussion.postNewComment.bind(discussion);
}

/**
 * Renders a comment and its replies, with a form to post a new reply.
 */
export class DiscussionComment extends HTMLElement {
  #discussion;

  /**
   * Creates an custom HTML element representing a comment.  This uses the
   * template and slots defined by `TEMPLATE_COMMENT` to render the
   * comment's content and replies.
   *
   * @param comment The comment from the servlet that this element should
   *     render.
   * @param {DiscussionArea} discussion The discussion that this comment is a
   *     part of.
   */
  constructor(comment, discussion) {
    super();
    this.#discussion = discussion;
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
      this.#discussion.postReply(textarea.value, this.comment.commentKey.id);
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
      replyDiv.appendChild(new DiscussionComment(reply, this.#discussion));
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
  discussion.seek(currentTimeMilliseconds);
}
