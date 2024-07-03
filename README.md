# Web technology project
A playlist manager developed in two versions.

# Backend
Written using Java servlet to manage backend.<br>
MySQL Workbench was used as DBMS.

# Frontend
Written in two versions (with a common backend):
- A pure HTML version with the usage of Thymeleaf as template engine,
- A pure Javascript version.
  
Both rely on basic HTML pages.

# Deployment
To deploy the application:
- Setup MySQL Workbench on localhost:3306 (other DBMS may be used but they were not tested), dump of used DB is located under DB folder on the repo,
- Fill web.xml with required information,
- Boot your chosen server (the webapp was tested on Tomcat),
- Add images/video tracks via the webapp.
