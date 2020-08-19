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

/** Manages storing eventListeners and broadcasting events. */
export default class EventController {
  constructor() {
    this.eventListeners = new Map();
    this.addEventListener = this.addEventListener.bind(this);
    this.broadcastEvent = this.broadcastEvent.bind(this);
  }

  /** Adds `callBack` to `eventNames` stored in `eventListeners`. */
  addEventListener(callBack, eventNames) {
    for (const eventName of eventNames) {
      let callBacks = [];
      if (this.eventListeners.has(eventName)) {
        callBacks = this.eventListeners.get(eventName);
      }
      callBacks.push(callBack);
      this.eventListeners.set(eventName, callBacks);
    }
  }

  /**
   *  Calls all callbacks associated with `eventName` and
   *  passes `params` to callback. Returns false if there
   *  were no callbacks, true otherwise.
   */
  broadcastEvent(eventName, ...params) {
    if (!this.eventListeners.has(eventName)) {
      console.error('No event listeners found.');
      return false;
    }
    const callBacks = this.eventListeners.get(eventName);
    callBacks.forEach((callback) => {
      callback(...params);
    });
    return true;
  }
}
