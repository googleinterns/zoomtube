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

const ENDPOINT_AUTH = '/auth';
let AUTH_STATUS;

/**
 * Loads and saves the user's authentication status.
 */
async function intializeAuth() {
  AUTH_STATUS = await getAuthStatus();
}

/**
 * Fetches and returns the user's authentication status from the authentication
 * servlet.
 */
async function getAuthStatus() {
  const url = new URL(ENDPOINT_AUTH, window.location.origin);
  const response = await fetch(url);
  return await response.json();
}
