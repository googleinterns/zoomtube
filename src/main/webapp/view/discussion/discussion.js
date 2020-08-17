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

const ENDPOINT_DISCUSSION = '/discussion';

const PARAM_LECTURE = 'lecture';
const PARAM_PARENT = 'parent';
const PARAM_TIMESTAMP = 'timestamp';
const PARAM_TYPE = 'type';

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
async function intializeDiscussion() {
  await loadDiscussion();
}

/**
 * Posts a new comment using the main post textarea.
 */
async function postNewComment() {
  // TODO: Add support for submitting types other than QUESTION.
  postAndReload(ELEMENT_POST_TEXTAREA, {
    [PARAM_TIMESTAMP]: newCommentTimestampMs,
    [PARAM_TYPE]: COMMENT_TYPE_QUESTION,
  });
}

/**
 * Posts the content of `inputField` as a reply to `parentId`.
 */
async function postReply(inputField, parentId) {
  postAndReload(inputField, {
    [PARAM_PARENT]: parentId,
    [PARAM_TYPE]: COMMENT_TYPE_REPLY,
  });
}

/**
 * Posts comment from `inputField` and reloads the discussion. Adds query
 * parameters from `params` to the request. Different types of comments
 * require different parameters, such as `PARAM_TIMESTAMP` or `PARAM_PARENT`.
 * The caller should ensure the correct parameters are supplied for the type
 * of comment being posted.
 */
async function postAndReload(inputField, params) {
  const url = new URL(ENDPOINT_DISCUSSION, window.location.origin);
  url.searchParams.append(PARAM_LECTURE, window.LECTURE_ID);
  for (const param in params) {
    // This is recommended by the style guide, but disallowed by linter.
    /* eslint-disable no-prototype-builtins */
    if (params.hasOwnProperty(param)) {
      url.searchParams.append(param, params[param]);
    }
    /* eslint-enable no-prototype-builtins */
  }

  fetch(url, {
    method: 'POST',
    body: inputField.value,
  }).then(() => {
    inputField.value = '';
    loadDiscussion();
  });
}

/**
 * Adds comments to the discussion element.
 */
async function loadDiscussion() {
  // Clear any existing comments before loading.
  ELEMENT_DISCUSSION.textContent = '';
  currentRootDiscussionComments = [];

  const comments = await fetchDiscussion();
  const preparedComments = prepareComments(comments);
  for (const comment of preparedComments) {
    const commentElement = new DiscussionComment(comment);
    currentRootDiscussionComments.push(commentElement);
    ELEMENT_DISCUSSION.appendChild(commentElement);
  }
}

/**
 * Organizes comments into threads with nested replies.
 *
 * <p>All comments are sent from the servlet without any structure, so the
 * client needs to organize them before displaying.
 */
function prepareComments(comments) {
  const commentKeys = {};
  for (const comment of comments) {
    comment.replies = [];
    commentKeys[comment.commentKey.id] = comment;
  }

  const rootComments = [];
  for (const comment of comments) {
    if (comment.type === COMMENT_TYPE_REPLY) {
      const parent = commentKeys[comment.parentKey.value.id];
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
 * Requests all comments in the lecture specified by `LECTURE_ID` from
 * the {@link java.com.googleinterns.zoomtube.servlets.DiscussionServlet}.
 */
async function fetchDiscussion() {
  const url = new URL(ENDPOINT_DISCUSSION, window.location.origin);
  url.searchParams.append(PARAM_LECTURE, window.LECTURE_ID);

  const request = await fetch(url);
  return request.json();
}

/**
 * Updates the timestamp displayed and sent by the new comment form.
 *
 * @param {number} timeMs The new time in milliseconds to use.
 */
function updateNewCommentTimestamp(timeMs) {
  ELEMENT_TIMESTAMP_SPAN.innerText = window.timestampToString(timeMs);
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
      timestampPrefix =
          `${window.timestampToString(comment.timestampMs.value)} - `;
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

/** Seeks discussion to `currentTimeMs`. */
function seekDiscussion(currentTimeMs) {
  updateNewCommentTimestamp(currentTimeMs);
  const nearbyComments = getNearbyDiscussionComments(currentTimeMs);
  if (nearbyComments.length == 0) {
    return;
  }
  nearbyComments[0].scrollToTopOfDiscussion();
}
