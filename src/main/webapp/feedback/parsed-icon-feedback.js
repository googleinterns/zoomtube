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
  /* Each index of these represents the number of IconFeedbacks for that
   * icon type during that 10 seconds interval.
   */
  #counts = {
    [IconFeedbackUtil.TYPE_GOOD]: [],
    [IconFeedbackUtil.TYPE_BAD]: [],
    [IconFeedbackUtil.TYPE_TOO_FAST]: [],
    [IconFeedbackUtil.TYPE_TOO_SLOW]: [],
    [IconFeedbackUtil.INTERVAL]: [],
  }

  appendTypeCountsAndInterval(counts) {
    console.log(counts);
    this.#counts[IconFeedbackUtil.TYPE_GOOD].push(
        counts[IconFeedbackUtil.TYPE_GOOD]);
    this.#counts[IconFeedbackUtil.TYPE_BAD].push(
        counts[IconFeedbackUtil.TYPE_BAD]);
    this.#counts[IconFeedbackUtil.TYPE_TOO_FAST].push(
        counts[IconFeedbackUtil.TYPE_TOO_FAST]);
    this.#counts[IconFeedbackUtil.TYPE_TOO_SLOW].push(
        counts[IconFeedbackUtil.TYPE_TOO_SLOW]);
    this.#counts[IconFeedbackUtil.INTERVAL].push(
        counts[IconFeedbackUtil.INTERVAL]);
  }
}
