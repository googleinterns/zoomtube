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
import {ELEMENT_DISCUSSION} from './discussion-area.js';
import {COMMENT_TYPE_REPLY} from './discussion.js';

/**
 * Renders a comment and its replies, with a form to post a new reply.
 */
export default class DiscussionComment extends HTMLElement {
  static #TEMPLATE = document.querySelector('#comment-template');

  static #ATTR_HIGHLIGHTED = 'highlighted';

  static #SLOT_HEADER = 'header';
  static #SLOT_CONTENT = 'content';
  static #SLOT_REPLIES = 'replies';

  static #SELECTOR_SHOW_REPLY = '#show-reply';
  static #SELECTOR_REPLY_FORM = '#reply-form';
  static #SELECTOR_CANCEL_REPLY = '#cancel-reply';
  static #SELECTOR_POST_REPLY = '#post-reply';
  static #SELECTOR_REPLY_TEXTAREA = '#reply-textarea';

  #discussion;
  #replyDiv;

  /**
   * Creates an custom HTML element representing a comment.  This uses the
   * template and slots defined by `TEMPLATE` to render the
   * comment's content and replies, once the comment is set.
   *
   * @param {DiscussionArea} discussion The current discussion.
   */
  constructor(discussion) {
    super();
    this.#discussion = discussion;
    this.attachShadow({mode: 'open'});
    const shadow = DiscussionComment.#TEMPLATE.content.cloneNode(true);
    this.shadowRoot.appendChild(shadow);
    this.#replyDiv = document.createElement('div');
    this.#replyDiv.slot = DiscussionComment.#SLOT_REPLIES;
    this.appendChild(this.#replyDiv);
    this.addReplyEventListeners();
  }

  /**
   * Sets the `comment` from the discussion that this element should
   * render. This does not add any nested reply elements. Those must be
   * added separately by the caller using `insertReply`.
   */
  setComment(comment) {
    this.comment = comment;
    this.setSlotSpan(
        DiscussionComment.#SLOT_HEADER, this.getHeaderString(comment));
    this.setSlotSpan(DiscussionComment.#SLOT_CONTENT, comment.content);
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
    // The linter does not agree with the prettier about how to indent this.
    /* eslint-disable indent */
    const replyForm =
        this.shadowRoot.querySelector(DiscussionComment.#SELECTOR_REPLY_FORM);
    this.shadowRoot.querySelector(DiscussionComment.#SELECTOR_SHOW_REPLY)
        .onclick = () => {
      $(replyForm).collapse('show');
    };
    this.shadowRoot.querySelector(DiscussionComment.#SELECTOR_CANCEL_REPLY)
        .onclick = () => {
      $(replyForm).collapse('hide');
    };
    this.shadowRoot.querySelector(DiscussionComment.#SELECTOR_POST_REPLY)
        .onclick = this.postReplyClicked.bind(this);
    /* eslint-enable indent */
  }

  /**
   * Posts the content of the reply textarea as a reply to this comment,
   * and resets the reply form.
   */
  postReplyClicked() {
    const textarea = this.shadowRoot.querySelector(
        DiscussionComment.#SELECTOR_REPLY_TEXTAREA);
    this.#discussion.postReply(textarea.value, this.comment.commentKey.id);

    textarea.value = '';
    const replyForm =
        this.shadowRoot.querySelector(DiscussionComment.#SELECTOR_REPLY_FORM);
    $(replyForm).collapse('hide');
  }

  /**
   * Inserts a new reply comment, maintaining order by time created.
   */
  insertReply(newComment) {
    const newCommentDate = new Date(newComment.created);
    // For now, we use a linear search. This can be improved if it becomes
    // an issue.
    for (const commentElement of this.#replyDiv.children) {
      const commentDate = new Date(commentElement.comment.created);
      if (newCommentDate >= commentDate) {
        commentElement.before(newComment.element);
        return;
      }
    }
    // If it isn't before any existing replies, it must belong at the end.
    this.#replyDiv.appendChild(newComment.element);
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

  /**
   * Highlights this element on the DOM.
   */
  highlight() {
    // We just want to add the attribute, the value doesn't matter.
    this.setAttribute(DiscussionComment.#ATTR_HIGHLIGHTED, /* value= */ '');
  }

  /**
   * Unhighlights this element on the DOM.
   */
  unhighlight() {
    this.removeAttribute(DiscussionComment.#ATTR_HIGHLIGHTED);
  }
}

// Custom element names must contain a hyphen.
customElements.define('discussion-comment', DiscussionComment);
