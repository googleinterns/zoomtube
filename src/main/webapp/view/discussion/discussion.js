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

import MarkAnsweredUtil from './mark-answered-util.js';

export const COMMENT_TYPE_REPLY = 'REPLY';
export const COMMENT_TYPE_QUESTION_UNANSWERED = 'QUESTION_UNANSWERED';
export const COMMENT_TYPE_QUESTION_ANSWERED = 'QUESTION_ANSWERED';
export const COMMENT_TYPE_NOTE = 'NOTE';

/**
 * Provides information on a comment type. For instance: its name,
 * display styles, and if it can be changed.
 * @typedef {Object} CommentTypeInfo
 * @property {string} name The display name of the type.
 * @property {!Array<string>} badgeStyles List of classes to add to the type
 *     tag.
 * @property {boolean} isQuestion If the type is a question, meaning the
 *     answered status can be changed.
 * @property {string=} oppositeType The opposite type of comment, for
 *     question types.
 * @property {string=} updateTypeText Text to display on a button to change a
 *     comment's type to this type, for question types.
 * @property {function(Object):void=} updateTypeFunction A function to set a
 *     comment to this type, for question types.
 */
// TODO: Rename badgeStyles to tagStyles.

/** @type {Object.<string, CommentTypeInfo>} */
export const COMMENT_TYPES = {
  [COMMENT_TYPE_REPLY]: {
    name: 'Reply',
    badgeStyles: ['badge-secondary', 'float-right', 'badge', 'badge-pill'],
    isQuestion: false,
  },
  [COMMENT_TYPE_NOTE]: {
    name: 'Note',
    badgeStyles: ['badge-secondary', 'float-right', 'badge', 'badge-pill'],
    isQuestion: false,
  },
  [COMMENT_TYPE_QUESTION_UNANSWERED]: {
    name: 'Question',
    badgeStyles: ['badge-danger', 'float-right', 'badge', 'badge-pill'],
    isQuestion: true,
    oppositeType: COMMENT_TYPE_QUESTION_ANSWERED,
    updateTypeText: 'Mark as Unanswered',
    updateTypeFunction: MarkAnsweredUtil.markUnanswered,
  },
  [COMMENT_TYPE_QUESTION_ANSWERED]: {
    name: 'Answered',
    badgeStyles: ['badge-success', 'float-right', 'badge', 'badge-pill'],
    isQuestion: true,
    oppositeType: COMMENT_TYPE_QUESTION_UNANSWERED,
    updateTypeText: 'Mark as Answered',
    updateTypeFunction: MarkAnsweredUtil.markAnswered,
  },
};
