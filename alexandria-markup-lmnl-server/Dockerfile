FROM java:8
ENV wd=/home/alexandria
RUN useradd -m alexandria
USER alexandria
RUN mkdir -p /home/alexandria/.alexandria
WORKDIR ${wd}
ADD target/alexandria-markup-lmnl-server.jar ${wd}/
ADD server.yml ${wd}/
EXPOSE 8080 8081
ENTRYPOINT java -jar alexandria-markup-lmnl-server.jar server server.yml