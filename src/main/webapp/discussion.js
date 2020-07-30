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

const ELEMENT_DISCUSSION = document.querySelector('#discussion');
const ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');
const ELEMENT_AUTH_STATUS = document.querySelector('#auth-status');

let AUTH_STATUS = null;

/**
 * Loads the user's authentication status and then loads the lecture
 * disucssion.
 */
async function intializeDiscussion() {
  AUTH_STATUS = await getAuthStatus();
  displayAuthStatus();
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
 * Displays authentication status and action links.  If a user is logged in,
 * this adds the user's email and a logout link.  Otherwise, this adds a login
 * link.
 */
async function displayAuthStatus() {
  const link = document.createElement('a');
  if (AUTH_STATUS.loggedIn) {
    link.href = AUTH_STATUS.logoutUrl.value;
    link.innerText = 'logout';
    ELEMENT_AUTH_STATUS.innerText =
      `Logged in as ${AUTH_STATUS.user.value.email}. `;
  } else {
    link.href = AUTH_STATUS.loginUrl.value;
    link.innerText = 'login';
    ELEMENT_AUTH_STATUS.innerText = 'Not logged in. ';
  }
  ELEMENT_AUTH_STATUS.appendChild(link);
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
    ELEMENT_DISCUSSION.appendChild(createComment(comment));
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
 * Creates an element for displaying {@code comment}.
 */
function createComment(comment) {
  const element = document.createElement('li');
  element.setAttribute(ATTR_ID, comment.key.id);

  const content = document.createElement('span');
  content.innerText = `${comment.author.email} says ${comment.content}`;
  element.appendChild(content);

  const repliesDiv = document.createElement('div');

  const repliesList = document.createElement('ul');
  for (const reply of comment.replies) {
    repliesList.appendChild(createComment(reply));
  }
  repliesDiv.appendChild(repliesList);

  if (AUTH_STATUS.loggedIn) {
    const replyButton = document.createElement('button');
    replyButton.innerText = 'Reply';
    element.appendChild(replyButton);
    replyButton.onclick = () => {
      createReplySubmission(repliesDiv);
      replyButton.remove();
    };
  }
  element.appendChild(repliesDiv);

  return element;
}

/**
 * Creates a reply textarea and submit button within {@code repliesDiv}.
 *
 * <p>The parent of {@code repliesDiv} should be a comment element created by
 * {@code createComment}.
 */
function createReplySubmission(repliesDiv) {
  const div = document.createElement('div');
  const textarea = document.createElement('textarea');
  const submit = document.createElement('button');
  const parentId = repliesDiv.parentElement.getAttribute(ATTR_ID);
  submit.innerText = 'Post';
  submit.onclick = () => {
    postAndReload(textarea, parentId);
  };
  div.appendChild(textarea);
  div.appendChild(submit);

  repliesDiv.prepend(div);
}

// TODO: Implement
function seekDiscussion(currentTime) {
  console.log('SEEKING DISCUSSION TO: ' + currentTime);
}
