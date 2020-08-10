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

/**
 * Converts a video {@code timestamp} in milliseconds into a string.
 */
function timestampToString(timestamp) {
  const date = new Date();
  date.setTime(timestamp);
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
 * Converts a number of {@code seconds} into a integer number of
 * milliseconds.
 */
function secondsToMilliseconds(seconds) {
  return Math.round(seconds * 1000);
}

/**
 * Converts a number of {@code milliseconds} into seconds.
 */
function millisecondsToSeconds(milliseconds) {
  return milliseconds / 1000;
}
