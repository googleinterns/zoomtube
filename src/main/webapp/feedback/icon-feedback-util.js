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

/** Contains constants related to IconFeedback. */
export default class IconFeedbackUtil {
  static TYPE_GOOD = 'GOOD';
  static TYPE_BAD = 'BAD';
  static TYPE_TOO_FAST = 'TOO_FAST';
  static TYPE_TOO_SLOW = 'TOO_SLOW';
  static INTERVAL = 'INTERVAL';

  static CHART_COLORS = {
    red: 'rgb(255, 99, 132)',
    orange: 'rgb(255, 159, 64)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(54, 162, 235)',
  };
}
