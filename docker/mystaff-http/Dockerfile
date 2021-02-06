# Httpd image
FROM httpd:2.4

#copy angular dist folder to container 
COPY src/ /usr/local/apache2/htdocs/

#change permissions
RUN chmod -R 755 /usr/local/apache2/htdocs/

#expose port
EXPOSE 4200