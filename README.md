# ZoomTube

View our [Design Doc](https://docs.google.com/document/d/12t7FDPDpTpHzrQBJ3T8qdmGKGrPi3lr2-UPmoc2ISHI).

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

- [YouTube Data API]() for generating transcripts
- [YouTube iFrame API]() for displaying video
- [AppEngine]() for deploying

*TODO: Add more.*

## Usage and Deployment

To run the project, you need to install Maven and the Google Cloud Platform SDK.

Create an AppEngine project within the Google Cloud Platform Console, and update
the project id in `pom.xml`.

You can run a local test server using

```
mvn package appengine:run
```

or deploy using

```
mvn package appengine:deploy
```

## Context

ZoomTube was developed by Amy (@amuamushu), Jay (@jaysaleh), and Nathan (@n-wach)
as a capstone project for Google's 2020 STEP internship program.
