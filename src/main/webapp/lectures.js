/* Used to gather URL parameters. */
const URL_PARAM_ID = 'id';
const URL_PARAM_VIDEO_ID = 'video_id';

const REDIRECT_PAGE = '/lecture-view.html';

loadLectureList();

/**
 * Fetches data from servlet and sets it in the lecture selection portion.
 * Called whenever lecture selection page is loaded.
 */
async function loadLectureList() {
  const response = await fetch('/lecture');
  const jsonData = await response.json();

  const lectureList = document.getElementById('lecture-list');
  lectureList.innerHTML = '';
  for (const lecture of jsonData) {
    lectureList.appendChild(createLectureListItem(lecture));
  }
}

/**
 * Creates and returns a <li> containing an <a> linking to {@code
 * lecture.videoUrl} and the {@code lecture.lectureName}.
 */
function createLectureListItem(lecture) {
  const listItem = document.createElement('li');
  const lectureLink = document.createElement('a');

  const url = new URL(REDIRECT_PAGE, window.location.origin);
  url.searchParams.append(URL_PARAM_ID, lecture.key.id);
  url.searchParams.append(URL_PARAM_VIDEO_ID, lecture.videoId);
  lectureLink.href = url;

  lectureLink.innerText = lecture.lectureName;

  listItem.appendChild(lectureLink);
  return listItem;
}
