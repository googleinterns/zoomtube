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

import TimestampUtil from '../timestamp-util.js';
import IconFeedbackUtil from './icon-feedback-util.js';

/** Stores IconFeeback counts in current interval. */
export default class IntervalIconFeedbackCount {
  /* Values of each icon feedback type at each interval. */
  #iconFeedbackCounts = {
    [IconFeedbackUtil.TYPE_GOOD]: 0,
    [IconFeedbackUtil.TYPE_BAD]: 0,
    [IconFeedbackUtil.TYPE_TOO_FAST]: 0,
    [IconFeedbackUtil.TYPE_TOO_SLOW]: 0,
  }

  /* Current interval IconFeedback counts appear in. */
  #timestampInterval;

  constructor(timestampInterval) {
    this.#timestampInterval =
        TimestampUtil.timestampToString(timestampInterval);
  }

  /** Increments count of `iconFeedbackType` in dictionary. */
  incrementIconFeedbackCount(iconFeedbackType) {
    this.#iconFeedbackCounts[iconFeedbackType]++;
  }

  getIconFeedbackCounts() {
    return this.#iconFeedbackCounts;
  }

  getTimestampInterval() {
    return this.#timestampInterval;
  }
}
