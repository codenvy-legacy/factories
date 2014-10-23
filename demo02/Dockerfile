FROM codenvy/jdk7_tomcat7
ENV DEBIAN_FRONTEND noninteractive
 
# Install MYSQL
RUN sudo apt-get update && \
    sudo apt-get install pwgen && \
    CODENVY_MYSQL_PASSWORD=password && \
    CODENVY_MYSQL_DB=petclinic && \
    CODENVY_MYSQL_USER=root && \
    echo "MySQL password: $CODENVY_MYSQL_PASSWORD" >> /home/user/.mysqlrc && \
    echo "MySQL user    : $CODENVY_MYSQL_USER" >> /home/user/.mysqlrc && \
    echo "MySQL Database:$CODENVY_MYSQL_DB" >> /home/user/.mysqlrc && \
    echo "mysql-server mysql-server/root_password password $CODENVY_MYSQL_PASSWORD" | sudo debconf-set-selections && \
    echo "mysql-server mysql-server/root_password_again password $CODENVY_MYSQL_PASSWORD" | sudo debconf-set-selections && \
    sudo -E bash -c "apt-get -y --no-install-recommends install mysql-server" && \
    sudo service mysql restart && \
    sudo mysql -u$CODENVY_MYSQL_USER -p$CODENVY_MYSQL_PASSWORD -e "GRANT ALL PRIVILEGES ON *.* TO '$CODENVY_MYSQL_USER'@'%' IDENTIFIED BY '"$CODENVY_MYSQL_PASSWORD"'; FLUSH PRIVILEGES;" && \
    sudo mysql -u$CODENVY_MYSQL_USER -p$CODENVY_MYSQL_PASSWORD -e "CREATE DATABASE $CODENVY_MYSQL_DB;"
 
 
# Listen on all the interfaces
RUN sudo sed -i.bak 's/127.0.0.1/0.0.0.0/g' /etc/mysql/my.cnf
 
# expose MYSQL port
EXPOSE 3306

# Set JVM options (low memory)
ENV JAVA_OPTS -Djava.awt.headless=true -server -Xms48m -Xmx200M -XX:MaxPermSize=200m

#Add webapp
ADD $app$ /home/user/tomcat7/webapps/ROOT.war
 
# Display login, password and hostname:port
CMD cat /home/user/.mysqlrc;echo Database host:port is $CODENVY_HOSTNAME:$CODENVY_PORT_3306; sudo service mysql start > /dev/null; ./catalina.sh run 2>&1
