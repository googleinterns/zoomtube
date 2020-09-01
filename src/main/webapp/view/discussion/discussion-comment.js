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

import TimestampUtil from '../../timestamp-util.js';
import {COMMENT_TYPE_REPLY, COMMENT_TYPES} from './discussion.js';

/**
 * Renders a comment and its replies, with a form to post a new reply.
 */
export default class DiscussionComment extends HTMLElement {
  static #TEMPLATE = document.querySelector('#comment-template');

  static #ATTR_HIGHLIGHTED = 'highlighted';

  static #SLOT_HEADER = 'header';
  static #SLOT_CONTENT = 'content';
  static #SLOT_REPLIES = 'replies';
  static #SLOT_TYPE_TAG = 'type-tag';

  static #SELECTOR_SHOW_REPLY = '#show-reply';
  static #SELECTOR_REPLY_FORM = '#reply-form';
  static #SELECTOR_CANCEL_REPLY = '#cancel-reply';
  static #SELECTOR_POST_REPLY = '#post-reply';
  static #SELECTOR_REPLY_TEXTAREA = '#reply-textarea';
  static #SELECTOR_MARK_AS_BUTTON = '#mark-as-button';

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
    this.setHeader();
    this.setContent();
    this.setTypeTag(comment.type);
    this.setMarkAsButton(comment.type);
  }

  /**
   * Adds the comment's header to the header slot. For root comments, adds
   * a click event handler to seek everything to the comment's timestamp.
   */
  setHeader() {
    const headerSpan = document.createElement('span');
    headerSpan.innerText = this.getHeaderString();
    headerSpan.slot = DiscussionComment.#SLOT_HEADER;
    this.appendChild(headerSpan);

    if (this.comment.type === COMMENT_TYPE_REPLY) {
      return;
    }

    headerSpan.onclick = () => {
      this.#discussion.onCommentHeaderClicked(this.comment.timestampMs.value);
    };
  }

  /**
   * Adds the comment's content to the content slot.
   */
  setContent() {
    const contentSpan = document.createElement('span');
    contentSpan.innerText = this.comment.content;
    contentSpan.slot = DiscussionComment.#SLOT_CONTENT;
    this.appendChild(contentSpan);
  }

  /**
   * Returns a string containing the timestamp, author, and creation time of
   * the `comment`.  The timestamp is not displayed for replies to
   * other comments.
   */
  getHeaderString() {
    const username = this.comment.author.email.split('@')[0];
    let timestampPrefix = '';
    if (this.comment.type !== COMMENT_TYPE_REPLY) {
      // Don't show timestamp on replies.
      const timestampString =
          TimestampUtil.timestampToString(this.comment.timestampMs.value);
      timestampPrefix = `${timestampString} - `;
    }

    // An undefined locale means to use the browser's default.
    const createdDateString = this.comment.created.toLocaleDateString(
        /* locale= */ undefined, {
          timeStyle: 'short',
          dateStyle: 'short',
        });

    return `${timestampPrefix}${username} on ${createdDateString}`;
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
    const replyTextarea = this.shadowRoot.querySelector(
        DiscussionComment.#SELECTOR_REPLY_TEXTAREA);

    this.shadowRoot.querySelector(DiscussionComment.#SELECTOR_SHOW_REPLY)
        .onclick = () => {
      $(replyForm).collapse('show');
      $(replyTextarea).focus();
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
    this.#discussion.postReply(
        textarea.value, this.comment.commentKey.id,
        this.comment.transcriptLineKey.id);

    textarea.value = '';
    const replyForm =
        this.shadowRoot.querySelector(DiscussionComment.#SELECTOR_REPLY_FORM);
    $(replyForm).collapse('hide');
  }

  /**
   * Inserts a new reply such that replies are sorted with the earliest created
   * appearing first.
   */
  insertReply(newComment) {
    const newCommentDate = new Date(newComment.created);
    // For now, we use a linear search. This can be improved if it becomes
    // an issue.
    for (const commentElement of this.#replyDiv.children) {
      const commentDate = new Date(commentElement.comment.created);
      if (newCommentDate <= commentDate) {
        commentElement.before(newComment.element);
        return;
      }
    }
    // If it isn't before any existing replies, it must belong at the end.
    this.#replyDiv.appendChild(newComment.element);
  }

  /**
   * Sets the comment's type tag to a Bootstrap pill badge based on `type`.
   */
  setTypeTag(type) {
    if (type === COMMENT_TYPE_REPLY) {
      return;
    }

    const typePill = document.createElement('span');
    typePill.innerText = COMMENT_TYPES[type].name;
    typePill.classList.add(...COMMENT_TYPES[type].badgeStyles);
    typePill.slot = DiscussionComment.#SLOT_TYPE_TAG;
    this.appendChild(typePill);
  }

  /**
   * Sets the text and onclick event for the mark as button according to
   * the comment's `type`.
   */
  setMarkAsButton(type) {
    const markAsButton = this.shadowRoot.querySelector(
        DiscussionComment.#SELECTOR_MARK_AS_BUTTON);
    if (!COMMENT_TYPES[type].isQuestion) {
      markAsButton.style.visibility = 'hidden';
      return;
    }

    const oppositeType = COMMENT_TYPES[type].oppositeType;

    const updateTypeText = COMMENT_TYPES[oppositeType].updateTypeText;
    markAsButton.innerText = updateTypeText;

    const updateType = COMMENT_TYPES[oppositeType].updateTypeFunction;
    const listener = async () => {
      markAsButton.removeEventListener('click', listener);
      await updateType(this.comment.commentKey.id);
      await this.#discussion.updateDiscussion();
    };
    markAsButton.onclick = listener;
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
