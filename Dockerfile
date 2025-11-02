FROM ubuntu:latest
LABEL authors="Eduardo"

ENTRYPOINT ["top", "-b"]