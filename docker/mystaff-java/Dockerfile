# Java image
FROM openjdk:8-jre
# copy wait-for-it script
COPY wait-for-it.sh /usr/local
# copy project's artifact
COPY src /src
# HTTP(S) port
EXPOSE 9000 9443
# start java play app
CMD ["/usr/local/wait-for-it.sh", "mystaff-mariadb:3306", "--", "/src/bin/start", "-Dhttps.port=9443","-Dplay.crypto.secret=secret"]
