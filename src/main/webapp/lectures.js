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

  const url = new URL('/lecture-view.html', window.location.origin);
  url.searchParams.append('id', lecture.key.id);
  lectureLink.href = url;

  lectureLink.innerText = lecture.lectureName;

  listItem.appendChild(lectureLink);
  return listItem;
}
