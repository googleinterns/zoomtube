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

// These can't be combined because the line is too long, and the linter
// isn't able to split the line.
import {COMMENT_TYPE_QUESTION_ANSWERED} from './discussion.js';
import {COMMENT_TYPE_QUESTION_UNANSWERED} from './discussion.js';


/*
 * Provides static utility methods for marking comments as answered or
 * unanswered.
 */
export default class MarkAnsweredEndpoint {
  static #ENDPOINT = '/mark-answered';
  static #PARAM_COMMENT = 'comment';
  static #PARAM_NEW_TYPE = 'new-type';

  /**
   * Sends a request to mark `commentId` as answered.
   */
  static async markAnswered(commentId) {
    await MarkAnsweredEndpoint.postNewType(
        commentId, COMMENT_TYPE_QUESTION_ANSWERED);
  }

  /**
   * Sends a request to mark `commentId` as unanswered.
   */
  static async markUnanswered(commentId) {
    await MarkAnsweredEndpoint.postNewType(
        commentId, COMMENT_TYPE_QUESTION_UNANSWERED);
  }

  /**
   * Sends a post request to the `ENDPOINT` that marks `commentId`
   * as `newType`.
   *
   * <p>This function is private and should not be directly called
   * from outside of this class.
   */
  static async postNewType(commentId, newType) {
    const url = new URL(MarkAnsweredEndpoint.#ENDPOINT, window.location.origin);
    url.searchParams.append(MarkAnsweredEndpoint.#PARAM_COMMENT, commentId);
    url.searchParams.append(MarkAnsweredEndpoint.#PARAM_NEW_TYPE, newType);

    await fetch(url, {method: 'POST'});
  }
}
