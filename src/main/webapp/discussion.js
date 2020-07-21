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

const ENDPOINT = '/discussion';
const PARAM_LECTURE = 'lecture';
const ATTR_KEY = 'key';

const ELEMENT_DISCUSSION = document.querySelector('#discussion');
const ELEMENT_POST_TEXTAREA = document.querySelector('#post-textarea');


/**
 * Posts comment to {@code ENDPOINT} and reloads the discussion.
 */
async function postAndReload() {
  const url = new URL(ENDPOINT, window.location.origin);
  url.searchParams.append(PARAM_LECTURE, window.LECTURE_ID);

  fetch(url, {
    method: 'POST',
    body: ELEMENT_POST_TEXTAREA.value,
  }).then(() => {
    ELEMENT_POST_TEXTAREA.value = '';
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
  for (const comment of comments) {
    ELEMENT_DISCUSSION.appendChild(createComment(comment));
  }
}

/**
 * Requests all comments in the lecture specified by {@code LECTURE_ID} from
 * the {@link java.com.googleinterns.zoomtube.servlets.DiscussionServlet}.
 */
async function fetchDiscussion() {
  const url = new URL(ENDPOINT, window.location.origin);
  url.searchParams.append(PARAM_LECTURE, window.LECTURE_ID);

  const request = await fetch(url);
  return request.json();
}

/**
 * Creates an element for displaying {@code comment}.
 */
function createComment(comment) {
  const element = document.createElement('li');
  element.setAttribute(ATTR_KEY, comment.key);

  const content = document.createElement('span');
  content.innerText = comment.content;
  element.appendChild(content);

  const repliesDiv = document.createElement('div');
  element.appendChild(repliesDiv);

  const replyButton = document.createElement('button');
  replyButton.onclick = () => {
    createReplySubmission(repliesDiv);
    replyButton.remove();
  };
  replyButton.innerText = 'Reply';
  element.appendChild(replyButton);

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
  div.appendChild(textarea);
  div.appendChild(submit);

  repliesDiv.appendChild(div);
}
