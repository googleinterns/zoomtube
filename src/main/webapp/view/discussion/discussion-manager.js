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

import {COMMENT_TYPE_REPLY} from './discussion.js';

/**
 * Manages fetching and posting comments in a lecture's discussion.
 */
export default class DiscussionManager {
  static #ENDPOINT = '/discussion';
  static #PARAM_LECTURE = 'lecture';
  static #PARAM_PARENT = 'parent';
  static #PARAM_TIMESTAMP = 'timestamp';
  static #PARAM_TYPE = 'type';
  static #PARAM_TRANSCRIPT_LINE = 'transcript-line';
  #lecture;

  /**
   * Creates a `DiscussionManager` to manage posting and fetching comments for
   * the `lecture`.
   */
  constructor(lecture) {
    this.#lecture = lecture;
  }

  /**
   * Organizes comments into threads with nested replies.
   *
   * <p>All comments are sent from the servlet without any structure, so the
   * client needs to organize them before displaying.
   */
  structureComments(allComments) {
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
   * Fetches all of the lecture comments from the `ENDPOINT`. This
   * returns a sorted array of all root comments, with replies added to
   * their parents.
   */
  async fetchRootComments() {
    const url = new URL(DiscussionManager.#ENDPOINT, window.location.origin);
    url.searchParams.append(
        DiscussionManager.#PARAM_LECTURE, this.#lecture.key.id);

    const request = await fetch(url);
    const json = await request.json();

    return this.structureComments(json);
  }

  /**
   * Posts `content` as a new root comment at `timestampMs` with the specified
   * `type` and `transcriptLineId`.
   */
  async postRootComment(content, timestampMs, type, transcriptLineId) {
    await this.postComment(content, {
      [DiscussionManager.#PARAM_TIMESTAMP]: timestampMs,
      [DiscussionManager.#PARAM_TYPE]: type,
      [DiscussionManager.#PARAM_TRANSCRIPT_LINE]: transcriptLineId,
    });
  }

  /**
   * Posts `content` as a reply to `parentId`.
   */
  async postReply(content, parentId) {
    await this.postComment(content, {
      [DiscussionManager.#PARAM_PARENT]: parentId,
      [DiscussionManager.#PARAM_TYPE]: COMMENT_TYPE_REPLY,
    });
  }

  /**
   * Posts `content` to the discussion with the given `params`.  This method is
   * private and should only be called within `DiscussionManager`.
   *
   * <p>Different types of comments require different parameters, such as
   * `PARAM_TIMESTAMP` or `PARAM_PARENT`. The caller should ensure the correct
   * parameters are supplied for the type of comment being posted.
   */
  async postComment(content, params) {
    const url = new URL(DiscussionManager.#ENDPOINT, window.location.origin);
    url.searchParams.append(
        DiscussionManager.#PARAM_LECTURE, this.#lecture.key.id);
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
}
