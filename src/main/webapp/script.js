/** Checks if form is filled entirely, sends an alert if not. */
function validateForm() {
  var lectureName = document.forms['lectureForm']['name-input'].value;
  var videoUrl = document.forms['lectureForm']['video-input'].value;
  if (lectureName == null || lectureName == '') {
    alert('Lecture name must be filled out.');
    return false;
  }
  if (videoUrl == null || videoUrl == '') {
    alert('Video url must be filled out.');
    return false;
  }
}