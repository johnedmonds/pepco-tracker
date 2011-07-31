# Introduction

Pepco's storm center, which is found at http://www.pepco.com/home/emergency/maps/stormcenter/ , lets Pepco's customers see where there is an outage and how many customers that outage affects.  The goal of this project is to collect that data so we can have detailed statistics on Pepco's reliability.

# Dependencies

Java 1.5+

# Getting set up

You will need to build and install pepco-model before using pepco-scraper and pepco-web.

    cd </path/to/pepco-tracker>
    cd pepco-model
    mvn install

# Components

pepco-tracker is made of several components:

* pepco-model: Models the data for storage in a relational database.
* pepco-scraper: Scrapes Pepco's site and stores the data using pepco-model
* pepco-web: A web interface for viewing the data

# Running the Scraper

cd into the pepco-scraper directory and run

    java -jar target/pepco-scraper-1.0-SNAPSHOT-local.jar

# Running the Website

Maven will generate a war file when you cd into pepco-web and do

    mvn package

You can put that war file into any servlet container and it should work.

You can also build for other environments (e.g. local, dev, test, prod, etc.).  Sources specific to an environment go in src/env.  For things that should go in WEB-INF put them in /src/env/${build.env}/webapp.  For things that should go in WEB-INF/classes, put them in src/env/${build.env}/resources.  By default, we will build for the "local" environment.  You can switch the environment by specifying a value for build.env like so:

    mvn package -Dbuild.env=prod

# Configuration

pepco-scraper takes its database configuration from pepco-model.  You will have to be somewhat familiar with Hibernate if you wish to change the default place in which data is stored (currently pepco-tracker/data/pepco) or database (currently hsqldb).  You can find the database configuration file at (assuming $PEPCO_TRACKER_HOME refers to the path to pepco-tracker) $PEPCO_TRACKER_HOME/pepco-model/src/main/resources/hibernate.cfg.xml.

# Contributing

We welcome anyone who wants to contribute to this project.  Please do not be afraid to contact us for help understanding the code, finding something to work on, or for any other reason you can think of.