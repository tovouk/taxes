# Overview
## What this app does?

The purpose of this app is to browse a record of people that owe money using the Algolia Search API. 

In this current version, all fields are searchable. Touching a person's record will allow you to send an email to them to discuss a payment plan. In the future there will be more customizability for the users at hand. Pagination, rather than infinite scrolling is being considered. 

Known bugs:

* Losing connection while loading more records can cause a crash.

Note: This is merely a test version to display some of the core functionalities.

Another note: A live version should have authentication as contact information like this should not be readily available except to any debt collectors. No calling feature was included as calling random numbers is not in our best interest.


## Setup

Please populate your Algolia index with the MOCK_DATA.json. If you don't want to, at least create json in the following format:

```json
[
  {
    "id":1,
    "first_name":"Cosetta",
    "last_name":"Haliday",
    "email":"chaliday0@ameblo.jp",
    "phone":"760-598-3146",
    "streetAddress":"61292 Farwell Court",
    "socialSecurity":622443542,
    "amountOwed":16106.96,
    "City":"Escondido",
    "State":"CA"
  },
  {
  "//":"another one of the same model as above",
  },
  {
  "//":"another one of the same model as above",
  }
  ]
```

The algolia index name is inserted on Line 64 of MainActivity, in my case it is "dev_tax".

As this is a test app, in order to use this app, you will have to define the following values in your user's .gradle directory in the gradle.properties file.

* ApplicationID
* AdminAPIKey
* SearchAPIKey



