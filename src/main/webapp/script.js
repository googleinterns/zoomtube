loadList();

async function loadList() {
  const response = await fetch('/lecture');
  const jsonData = await response.json();

  const lectureList = document.getElementById('lecture-list');
  lectureList.innerHTML = '';
  for (lecture of jsonData) {
    lectureList.appendChild(createLectureListItem(lecture));
  }
}

function createLectureListItem(lecture) {
  const listItem = document.createElement('li');
  const lectureLink = document.createElement('a');

  lectureLink.href = lecture.videoUrl;
  lectureLink.target = "_blank";
  lectureLink.innerText = lecture.lectureName;

  listItem.appendChild(lectureLink);
  return listItem;
}