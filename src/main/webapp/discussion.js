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


/**
 * Posts comment to {@code ENDPOINT} and reloads the discussion.
 */
async function postAndReload(postTextarea, discussionElement, lectureKey) {
  fetch(ENDPOINT, {
    method: 'POST',
    body: postTextarea.value,
  }).then(res => {
    postTextarea.value = '';
    loadDiscussion(discussionElement, lectureKey);
  });
}


/**
 * Adds comments to the {@code discussionElement}.
 */
async function loadDiscussion(discussionElement, lectureKey) {
  // Clear any existing comments before loading.
  discussionElement.textContent = '';

  const comments = await fetchDiscussion(lectureKey);
  for (const comment of comments) {
    discussionElement.appendChild(createComment(comment));
  }
}

/**
 * Requests all comments in the lecture specified by {@code lectureKey} from
 * the {@link java.com.googleinterns.zoomtube.servlets.DiscussionServlet}.
 */
async function fetchDiscussion(lectureKey) {
  const url = new URL(ENDPOINT, window.location.origin);
  url.searchParams.append(PARAM_LECTURE, lectureKey);

  const request = await fetch(url);
  return request.json();
}

/**
 * Creates an element for displaying a {@code comment}.
 */
function createComment(comment) {
  const element = document.createElement('li');

  // TODO: Display more than just the content.
  element.innerText = comment.content;

  return element;
}
