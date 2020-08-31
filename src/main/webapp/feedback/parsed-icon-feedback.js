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

/** Stores parsed icon feedback data. */
export default class ParsedIconFeedback {
  /* Each index of these represents the number of IconFeedbacks for that
   * icon type during that 10 seconds interval.
   */
  #goodCounts = [];
  #badCounts = [];
  #tooFastCounts = [];
  #tooSlowCounts = [];

  /* Each index of this represents a point on the horizontal axis of graph. */
  #intervals = [];

  appendGoodCount(goodCount) {
    this.#goodCounts.push(goodCount);
  }

  appendBadCount(badCount) {
    this.#badCounts.push(badCount);
  }

  appendTooFastCount(tooFastCount) {
    this.#tooFastCounts.push(tooFastCount);
  }

  appendTooSlowCount(tooSlowCount) {
    this.#tooSlowCounts.push(tooSlowCount);
  }

  appendInterval(interval) {
    this.#intervals.push(interval);
  }
}
