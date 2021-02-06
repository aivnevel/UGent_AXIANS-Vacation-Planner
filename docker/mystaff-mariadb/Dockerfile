FROM mariadb:latest 

# Copy script
COPY init.sql /docker-entrypoint-initdb.d

EXPOSE 3306

CMD ["mysqld"]