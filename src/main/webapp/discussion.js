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
const ENDPOINT_AUTH = '/auth';

const PARAM_LECTURE = 'lecture';
const PARAM_PARENT = 'parent';

const ATTR_ID = 'key-id';

const ELEMENT_DISCUSSION = document.querySelector('#discussion-comments');
const ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');
const ELEMENT_AUTH_STATUS = document.querySelector('#auth-status');

const TEMPLATE_ROOT_COMMENT = document.querySelector('#comment-template');
const TEMPLATE_REPLY = document.querySelector('#reply-template');

let AUTH_STATUS = null;

/**
 * Loads the user's authentication status and then loads the lecture
 * disucssion.
 */
async function intializeDiscussion() {
  AUTH_STATUS = await getAuthStatus();
  await loadDiscussion();
}


/**
 * Fetches and returns the user's authentication status from the authentication
 * servlet.
 */
async function getAuthStatus() {
  const url = new URL(ENDPOINT_AUTH, window.location.origin);
  const response = await fetch(url);
  return await response.json();
}


/**
 * Posts a new comment using the main post textarea.
 */
async function postNewComment() {
  postAndReload(ELEMENT_POST_TEXTAREA);
}


/**
 * Posts a reply to a comment.
 */
async function postReply(textarea, parentId) {
  postAndReload(textarea, parentId);
}


/**
 * Posts comment from {@code textarea} and reloads the discussion. If
 * {@code parentId} is provided, this posts a reply to the comment with
 * that id.
 */
async function postAndReload(textarea, parentId = undefined) {
  const url = new URL(ENDPOINT_DISCUSSION, window.location.origin);
  url.searchParams.append(PARAM_LECTURE, window.LECTURE_ID);
  if (parentId) {
    url.searchParams.append(PARAM_PARENT, parentId);
  }

  fetch(url, {
    method: 'POST',
    body: textarea.value,
  }).then(() => {
    textarea.value = '';
    loadDiscussion();
  });
}

/**
 * Adds comments to the discussion element.
 */
async function loadDiscussion() {
  // Clear any existing comments before loading.
  ELEMENT_DISCUSSION.textContent = '';

  const comments = await fetchDiscussion();
  const preparedComments = prepareComments(comments);
  for (const comment of preparedComments) {
    ELEMENT_DISCUSSION.appendChild(new DiscussionComment(comment));
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
    commentKeys[comment.key.id] = comment;
  }

  const rootComments = [];
  for (const comment of comments) {
    if (comment.parent.value) {
      const parent = commentKeys[comment.parent.value.id];
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
 * 
 */
class DiscussionComment extends HTMLElement {
  constructor(comment) {
    super();
    this.attachShadow({mode: 'open'});
    const shadow = TEMPLATE_ROOT_COMMENT.content.cloneNode(true);
    this.shadowRoot.appendChild(shadow);

    const username = comment.author.email.split('@')[0];
    this.setSlotSpan('author', username);
    this.setSlotSpan('timestamp', '00:00');
    this.setSlotSpan('created', comment.created);
    this.setSlotSpan('content', comment.content);

    const replies = document.createElement('div');
    replies.slot = 'replies';
    for (const reply of comment.replies) {
      replies.appendChild(new Comment(reply));
    }
    this.appendChild(replies);

    this.shadowRoot.querySelector('#show-reply').onclick = () => {
      $(this.shadowRoot.querySelector('#reply-form')).collapse('show');
    };
    this.shadowRoot.querySelector('#cancel-reply').onclick = () => {
      $(this.shadowRoot.querySelector('#reply-form')).collapse('hide');
    };
    this.shadowRoot.querySelector('#post-reply').onclick = () => {
      const textarea = this.shadowRoot.querySelector('#reply-textarea');
      postReply(textarea, comment.key.id);
    };
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
}

// Custom element names must contain a hyphen.
customElements.define('discussion-comment', DiscussionComment);
