FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1

WORKDIR /api
ADD . /api

EXPOSE 8080

CMD sbt "project core" "run"
