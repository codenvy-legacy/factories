FROM florentbenoit/tomcatandmysql

#Add webapp
ADD $app$ /home/user/tomcat7/webapps/ROOT.war

#Initialize database structure
RUN mkdir /home/user/app_src
ADD $app_src$ /home/user/app_src/
RUN sudo service mysql restart && \
    sudo mysql -uroot < /home/user/app_src/src/main/resources/db/mysql/initDB.sql

# Display login, password and hostname:port
CMD cat /home/user/.mysqlrc;echo Database host:port is $CODENVY_HOSTNAME:$CODENVY_PORT_3306; sudo service mysql start > /dev/null; ./catalina.sh run
