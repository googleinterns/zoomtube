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

import MarkAnsweredEndpoint from './mark-answered-endpoint.js';

export const COMMENT_TYPE_REPLY = 'REPLY';
export const COMMENT_TYPE_QUESTION_UNANSWERED = 'QUESTION_UNANSWERED';
export const COMMENT_TYPE_QUESTION_ANSWERED = 'QUESTION_ANSWERED';
export const COMMENT_TYPE_NOTE = 'NOTE';

export const COMMENT_TYPES = {
  [COMMENT_TYPE_REPLY]: {
    name: 'Reply',
    badgeStyles: ['badge-secondary', 'float-right', 'badge', 'badge-pill'],
    hasMarkAs: false,
  },
  [COMMENT_TYPE_NOTE]: {
    name: 'Note',
    badgeStyles: ['badge-secondary', 'float-right', 'badge', 'badge-pill'],
    hasMarkAs: false,
  },
  [COMMENT_TYPE_QUESTION_UNANSWERED]: {
    name: 'Question',
    badgeStyles: ['badge-danger', 'float-right', 'badge', 'badge-pill'],
    hasMarkAs: true,
    markAsText: 'Mark as Unanswered',
    markAsFunction: MarkAnsweredEndpoint.markUnanswered,
    oppositeType: COMMENT_TYPE_QUESTION_ANSWERED,
  },
  [COMMENT_TYPE_QUESTION_ANSWERED]: {
    name: 'Answered',
    badgeStyles: ['badge-success', 'float-right', 'badge', 'badge-pill'],
    hasMarkAs: true,
    markAsText: 'Mark as Answered',
    markAsFunction: MarkAnsweredEndpoint.markAnswered,
    oppositeType: COMMENT_TYPE_QUESTION_UNANSWERED,
  },
  [COMMENT_TYPE_QUESTION_ANSWERED]: {
    name: 'Answered',
    badgeStyles: ['badge-success', 'float-right', 'badge', 'badge-pill'],
  },
};
