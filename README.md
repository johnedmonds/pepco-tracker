# Introduction

Pepco's storm center, which is found at http://www.pepco.com/home/emergency/maps/stormcenter/ , lets Pepco's customers see where there is an outage and how many customers that outage affects.  The goal of this project is to collect that data so we can have detailed statistics on Pepco's reliability.

# Dependencies

1. Java 1.5+
2. Maven 2+

All the library dependencies will be automatically downloaded by Maven.

# Quick Start

Here's how to get up and running really quickly:

    git clone https://github.com/johnedmonds/pepco-tracker.git
    cd pepco-tracker
    mvn install
    cd pepco-scraper
    java -jar target/pepco-scraper-1.0-SNAPSHOT-local.jar #Fill up your database with some initial data.
    cd ../pepco-web
    mvn jetty:run #Start up your personal web server.
    firefox localhost:8080/pepco-web #View the site.

# Getting set up

Simply go to the root directory and execute

    mvn install

# Components

pepco-tracker is made of several components:

* pepco-model: Models the data for storage in a relational database.
* pepco-scraper: Scrapes Pepco's site and stores the data using pepco-model
* pepco-web: A web interface for viewing the data
* pepco-charts: Generates the summary chart image for when you can't use R.
* pepco-uploader: Scrapes the site and uploads the content to S3.  Since we know the site will only be updated on a cron job, it's all essentially static content anyway.

# Pepco-Scraper

The scraper visits Pepco's StormCenter website and scrapes data from there.  If you're running the scraper in development mode, it will create a HSQLDB database in the `./data/pepco` (where `.` is the current working directory).

## Running the Scraper

cd into the pepco-scraper directory and run

    java -jar target/pepco-scraper-1.0-SNAPSHOT-local.jar

# Running the Website

## For Development

When you compile pepco-tracker (by running `mvn install` in the root directory) it will by default compile everything in development mode.

First make sure you have a database set up.  pepco-web in development mode by default looks for a HSQLDB database at ../pepco-scraper/data/pepco.  You can generate this database by running pepco-scraper in development mode from the `pepco-scraper/` directory.

Then, to get a server going, simply `cd` into the `pepco-web` folder and run

    mvn jetty:run

## For Deployment

Maven will generate a war file when you cd into pepco-web and do

    mvn package

You can put that war file into any servlet container and it should work.

You can also build for other environments (e.g. local, dev, test, prod, etc.).  Sources specific to an environment go in src/env.  For things that should go in WEB-INF put them in /src/env/${build.env}/webapp.  For things that should go in WEB-INF/classes, put them in src/env/${build.env}/resources.  By default, we will build for the "local" environment.  You can switch the environment by specifying a value for build.env like so:

    mvn package -Dbuild.env=prod

# Configuration

pepco-scraper takes its database configuration from pepco-model.  You will have to be somewhat familiar with Hibernate if you wish to change the default place in which data is stored (currently pepco-tracker/data/pepco) or database (currently hsqldb).  You can find the database configuration file at (assuming $PEPCO_TRACKER_HOME refers to the path to pepco-tracker) $PEPCO_TRACKER_HOME/pepco-model/src/main/resources/hibernate.cfg.xml.

# Contributing

We welcome anyone who wants to contribute to this project.  Please do not be afraid to contact us for help understanding the code, finding something to work on, or for any other reason you can think of.
