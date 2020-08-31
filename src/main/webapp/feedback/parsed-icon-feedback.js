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

import IconFeedbackUtil from './icon-feedback-util.js';

/** Stores parsed icon feedback data. */
export default class ParsedIconFeedback {
  /* Values of each icon feedback type at each interval. */
  #counts = {
    [IconFeedbackUtil.TYPE_GOOD]: [],
    [IconFeedbackUtil.TYPE_BAD]: [],
    [IconFeedbackUtil.TYPE_TOO_FAST]: [],
    [IconFeedbackUtil.TYPE_TOO_SLOW]: [],
    [IconFeedbackUtil.INTERVAL]: [],
  }

  /**
   * Appends each value in `typeCountsAndInterval` to it's respective
   * dictionary value.
   */
  appendTypeCountsAndInterval(typeCountsAndInterval) {
    for (const type in typeCountsAndInterval) {
      if (Object.prototype.hasOwnProperty.call(typeCountsAndInterval, type)) {
        this.#counts[type].push(typeCountsAndInterval[type]);
      }
    }
  }

  getGoodCounts() {
    return this.#counts[IconFeedbackUtil.TYPE_GOOD];
  }

  getBadCounts() {
    return this.#counts[IconFeedbackUtil.TYPE_BAD];
  }

  getTooFastCounts() {
    return this.#counts[IconFeedbackUtil.TYPE_TOO_FAST];
  }

  getTooSlowCounts() {
    return this.#counts[IconFeedbackUtil.TYPE_TOO_SLOW];
  }

  getInterval() {
    return this.#counts[IconFeedbackUtil.INTERVAL];
  }
}
