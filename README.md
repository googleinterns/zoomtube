# ZoomTube

[Check it out!](https://zoomtube-step-2020.appspot.com/)

View our [Design Doc](https://docs.google.com/document/d/12t7FDPDpTpHzrQBJ3T8qdmGKGrPi3lr2-UPmoc2ISHI/edit?usp=sharing).

## Overview

We are building a web application that facilitates the viewing of recorded
university lectures. With classes shifting towards a virtual environment,
lectures have become disengaging, especially for those watching asynchronously.
Our web application aims to mitigate this issue by providing a way for students
to interact with each other on a single platform.

Recorded university lectures are synchronized with their transcript and a
student discussion forum. Within this web application, students are able to
create and view timestamp annotations as a lecture is playing. These
annotations can include comments, questions, and replies. Students watching can
easily refer to relevant comments made by other students, add their own, and
reply to other students’ questions.

## Technology and APIs

This project uses the following tools:

- [AppEngine on Google Cloud](https://cloud.google.com/appengine) for deploying
- [Java Servlets](https://docs.oracle.com/javaee/5/tutorial/doc/bnafe.html) for back-end
- [YouTube Data API](https://developers.google.com/youtube/v3) for generating transcripts
- [YouTube iFrame API](https://developers.google.com/youtube/iframe_api_reference) for displaying video
- [Perspective API](https://www.perspectiveapi.com/) for automatic discussion moderation

## Usage and Deployment

To run the project, you need to install `npm`, Maven, and the Google Cloud
Platform SDK.

Run `make setup_hooks` if you want to always run linter tests locally before committing your code.

You can run a local test server using

```
make run
```

To deploy, create an AppEngine project within the Google Cloud Platform
Console, and update the project id in `pom.xml`. Then, deploy using

```
gcloud config set project [YOUR_PROJECT_ID]
gcloud datastore indexes create index.yaml
make deploy
```

## Context

ZoomTube was developed by
Amy ([@amuamushu](https://github.com/amuamushu)),
Jay ([@jaysaleh](https://github.com/jaysaleh)), and
Nathan ([@n-wach](https://github.com/n-wach)) as a capstone project for Google's
2020 STEP internship program.
