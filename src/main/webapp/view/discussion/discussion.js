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

import LectureView from '../lecture-view.js';

export const COMMENT_TYPE_REPLY = 'REPLY';
export const COMMENT_TYPE_QUESTION = 'QUESTION';
export const COMMENT_TYPE_NOTE = 'NOTE';

export const COMMENT_TYPES = {
  [COMMENT_TYPE_REPLY]: {
    name: 'Reply',
    badgeStyles: ['badge-secondary', 'float-right', 'badge', 'badge-pill'],
  },
  [COMMENT_TYPE_NOTE]: {
    name: 'Note',
    badgeStyles: ['badge-secondary', 'float-right', 'badge', 'badge-pill'],
  },
  [COMMENT_TYPE_QUESTION]: {
    name: 'Question',
    badgeStyles: ['badge-danger', 'float-right', 'badge', 'badge-pill'],
  },
};

/** Seeks discussion to `timeMs`. */
export function seekDiscussion(timeMs) {
  LectureView.discussion.seek(timeMs);
}
