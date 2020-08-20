
import {secondsToMilliseconds} from '../../timestamps.js';

import DiscussionArea from './discussion-area.js';

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

export let discussion;

/**
 * Loads the lecture disucssion.
 */
export async function intializeDiscussion() {
  discussion = new DiscussionArea(window.LECTURE);
  discussion.initialize();
  // This is used as the `onclick` handler of the new comment area submit
  // button. It must be set after discussion is initialized.
  window.postNewComment = discussion.postNewComment.bind(discussion);
}

/** Seeks discussion to `currentTimeSeconds`. */
export function seekDiscussion(currentTimeSeconds) {
  const currentTimeMilliseconds = secondsToMilliseconds(currentTimeSeconds);
  discussion.seek(currentTimeMilliseconds);
}
