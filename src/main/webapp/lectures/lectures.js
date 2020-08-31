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

const ENDPOINT_LECTURE_LIST = '/lecture-list';
const ENDPOINT_TRANSCRIPT_LANGUAGES = '/transcript-language';
const LANGUAGE_SELECTOR_CONTAINER = 'language-selector';
const NO_LANGUAGES_AVAILABLE_MESSAGE =
    'Sorry, there is no transcript available for this lecture.';
const SELECT_LANGUAGE_OPTION_MESSAGE =
    'Select the language for the transcript.';
/* Used to gather URL parameters. */
const PARAM_ID = 'id';

const REDIRECT_PAGE = '/view/';

loadLectureList();

/**
 * Fetches avaiable Lectures from `ENDPOINT_LECTURE_LIST`
 * and sets them in the lecture selection page.
 */
async function loadLectureList() {
  const response = await fetch(ENDPOINT_LECTURE_LIST);
  const jsonData = await response.json();

  const lectureList = document.getElementById('lecture-list');
  lectureList.innerHTML = '';
  for (const lecture of jsonData) {
    lectureList.appendChild(createLectureListItem(lecture));
  }
}

/**
 * Creates and returns a `<li>` containing an `<a>` linking to
 * `lecture`'s view page.
 */
function createLectureListItem(lecture) {
  const lectureLink = document.createElement('a');

  const url = new URL(REDIRECT_PAGE, window.location.origin);
  url.searchParams.append(PARAM_ID, lecture.key.id);
  lectureLink.href = url;
  lectureLink.className = 'list-group-item list-group-item-action';

  lectureLink.innerText = lecture.lectureName;

  return lectureLink;
}

async function fetchTranscriptLanguages(inputElement) {
  const languageSelectorDivElement =
      document.getElementById(LANGUAGE_SELECTOR_CONTAINER);
  languageSelectorDivElement.innerHTML = '';
  const url = new URL(ENDPOINT_TRANSCRIPT_LANGUAGES, window.location.origin);
  url.searchParams.append(inputElement.name, inputElement.value);
  let languagesResponse;
  try {
    languagesResponse = await fetch(url);
  } catch (error) {
    return;
  }
  const languagesJson = await languagesResponse.json();
  displayLanguages(languagesJson);
}

function displayLanguages(languages) {
  const languageSelectorDivElement =
      document.getElementById(LANGUAGE_SELECTOR_CONTAINER);
  if (languages.length == 0) {
    languageSelectorDivElement.innerText = NO_LANGUAGES_AVAILABLE_MESSAGE;
    return;
  }

  const languageSelectElement = document.createElement('select');
  languageSelectorDivElement.appendChild(languageSelectElement);
  languageSelectElement.appendChild(createDefaultDisabledLanguageOption());
  for (const language of languages) {
    const languageOptionElement = document.createElement('option');
    languageOptionElement.value = language.languageCode;
    languageOptionElement.innerText = language.languageNameInEnglish;
    languageSelectElement.appendChild(languageOptionElement);
  }
}

function createDefaultDisabledLanguageOption() {
  const defaultLanguageOptionElement = document.createElement('option');
  defaultLanguageOptionElement.disabled = true;
  defaultLanguageOptionElement.selected = true;
  defaultLanguageOptionElement.innerText = SELECT_LANGUAGE_OPTION_MESSAGE;
  return defaultLanguageOptionElement;
}
