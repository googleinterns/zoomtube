/** Used for accessing form input values. */
const LECTURE_FORM = 'lectureForm';
const NAME_INPUT = 'name-input';
const VIDEO_INPUT = 'video-input';

/** Used to send an error message to client. */
const LECTURE_NAME_ERROR = 'Lecture name must be filled out.';
const VIDEO_URL_ERROR = 'Video url must be filled out.';

/** Checks if form is filled entirely, sends an alert if not. */
function validateForm() {
  var lectureName = document.forms[LECTURE_FORM][NAME_INPUT].value;
  var videoUrl = document.forms[LECTURE_FORM][VIDEO_INPUT].value;
  if (lectureName == null || lectureName == '') {
    alert(LECTURE_NAME_ERROR);
    return false;
  }
  if (videoUrl == null || videoUrl == '') {
    alert(VIDEO_URL_ERROR);
    return false;
  }
}