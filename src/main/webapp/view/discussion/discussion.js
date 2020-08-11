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

const ATTR_ID = 'key-id';

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
const TIME_TOLERANCE_MILLISECONDS = 10000;

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
  postAndReload(
      ELEMENT_POST_TEXTAREA, /* parent= */ undefined, newCommentTimestampMs);
}

/**
 * Posts the content of {@code inputField} as a reply to {@code parentId}.
 */
async function postReply(inputField, parentId) {
  postAndReload(inputField, parentId);
}

/**
 * Posts comment from {@code inputField} and reloads the discussion. If
 * {@code parentId} is provided, this posts a reply to the comment with
 * that id.
 */
async function postAndReload(
    inputField, parentId = undefined, timestamp = undefined) {
  const url = new URL(ENDPOINT_DISCUSSION, window.location.origin);
  url.searchParams.append(PARAM_LECTURE, window.LECTURE_ID);
  if (parentId !== undefined) {
    url.searchParams.append(PARAM_PARENT, parentId);
  }
  if (timestamp !== undefined) {
    url.searchParams.append(PARAM_TIMESTAMP, timestamp);
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
    if (comment.parentKey.value) {
      const parent = commentKeys[comment.parentKey.value.id];
      parent.replies.push(comment);
    } else {
      // Top level comments don't have parents.
      rootComments.push(comment);
    }
  }
  return rootComments;
}

/**
 * Requests all comments in the lecture specified by {@code LECTURE_ID} from
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
 * Returns an array of the `DiscussionComment`s within
 * `TIME_TOLERANCE_MILLISECONDS` milliseconds to the `timestampMilliseconds`.
 *
 * <p>This returns an empty array if no elements are nearby.
 */
function getNearbyDiscussionComments(timestampMilliseconds) {
  const nearby = [];
  // currentRootDiscussionComments is already sorted by timestamp.
  for (const element of currentRootDiscussionComments) {
    const commentTime = element.comment.timestampMilliseconds;
    if (commentTime < timestampMilliseconds - TIME_TOLERANCE_MILLISECONDS) {
      // Before the start of the range, continue to next.
      continue;
    }
    if (commentTime > timestampMilliseconds + TIME_TOLERANCE_MILLISECONDS) {
      // Outside of range, there will be no more.
      break;
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
   * template and slots defined by {@code TEMPLATE_COMMENT} to render the
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
   * the {@code comment}.  The timestamp is not displayed for replies to
   * other comments.
   */
  getHeaderString(comment) {
    const username = comment.author.email.split('@')[0];
    let timestampPrefix = '';
    if (!comment.parentKey.value) {
      // Only display timestamp on root comments.
      timestampPrefix = `${window.timestampToString(comment.timestampMs)} - `;
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
   * Creates a {@code DiscussionComment} for every reply to this comment, and
   * adds them to a {@code div} in the replies slot of the DOM template.
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
   * Sets the content of the shadow-dom slot named {@code name} to a span
   * element containing {@code value} as text.
   */
  setSlotSpan(name, value) {
    const span = document.createElement('span');
    span.innerText = value;
    span.slot = name;
    this.appendChild(span);
  }

  /**
   * Scroll `ELEMENT_DISCUSSION` such that this element is at the top.
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

/** Seeks discussion to `@code currentTime`. */
function seekDiscussion(currentTimeSeconds) {
  const currentTimeMilliseconds =
      window.secondsToMilliseconds(currentTimeSeconds);
  updateNewCommentTimestamp(currentTimeMilliseconds);
  const nearbyComments = getNearbyDiscussionComments(currentTimeMilliseconds);
  if (nearbyComments.length == 0) {
    return;
  }
  nearbyComments[0].scrollToTopOfDiscussion();
}
