# Introduction

Pepco's storm center, which is found at http://www.pepco.com/home/emergency/maps/stormcenter/ , lets Pepco's customers see where there is an outage and how many customers that outage affects.  The goal of this project is to collect that data so we can have detailed statistics on Pepco's reliability.

# Dependencies

Java 1.5+

# Getting set up

You will need to build and install pepco-model before using pepco-scraper.

    cd </path/to/pepco-tracker>
    cd pepco-model
    mvn install

# Running

We don't currently have a very user-friendly way of running the scraper.  The current way to do it is to use Maven:

    mvn exec:java -Dexec.mainClass=com.pocketcookies.pepco.scraper.PepcoScraper

# Configuration

pepco-scraper takes its database configuration from pepco-model.  You will have to be somewhat familiar with Hibernate if you wish to change the default place in which data is stored (currently pepco-tracker/data/pepco) or database (currently hsqldb).  You can find the database configuration file at (assuming $PEPCO_TRACKER_HOME refers to the path to pepco-tracker) $PEPCO_TRACKER_HOME/pepco-model/src/main/resources/hibernate.cfg.xml.
