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

/** Contains functions used to convert timestamps. */
export default class TimestampUtil {
  static #MILLISECONDS_PER_SECOND = 1000;

  /**
   * Converts a `timestampMs` in milliseconds into a string.
   */
  static timestampToString(timestampMs) {
    const date = new Date();
    date.setTime(timestampMs);
    const seconds = date.getUTCSeconds().toString().padStart(
        /* targetLength= */ 2, /* padString= */ '0');
    const minutes = date.getUTCMinutes().toString().padStart(
        /* targetLength= */ 2, /* padString= */ '0');
    if (date.getUTCHours() == 0) {
      return `${minutes}:${seconds}`;
    }
    // We don't pad hours because lectures won't need two digits for hours.
    const hours = date.getUTCHours().toString();
    return `${hours}:${minutes}:${seconds}`;
  }

  /**
   * Converts `seconds` to milliseconds.
   */
  static secondsToMilliseconds(seconds) {
    return Math.round(seconds * TimestampUtil.#MILLISECONDS_PER_SECOND);
  }

  /**
   * Converts `milliseconds` to seconds.
   */
  static millisecondsToSeconds(milliseconds) {
    return Math.round(milliseconds / TimestampUtil.#MILLISECONDS_PER_SECOND);
  }

  /**
   * Returns a time range string in the form of
   * `startTimestampMs` - `endTimestampMs`.
   */
  static timestampRangeToString(startTimestampMs, endTimestampMs) {
    const startTimestamp = TimestampUtil.timestampToString(startTimestampMs);
    const endTimestamp = TimestampUtil.timestampToString(endTimestampMs);
    return `${startTimestamp} - ${endTimestamp}`;
  }
}
