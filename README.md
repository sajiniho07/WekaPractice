# Weka J48 Classifier for Possum Gender Prediction

This Java application interacts with an Oracle database to prepare and analyze data related to possum characteristics. It creates ARFF files from database tables, trains a J48 decision tree classifier, and predicts possum genders based on the provided data.

## Prerequisites

- Java Development Kit (JDK) installed on your system
- Oracle database access for the provided database URL, username, and password
- Weka library for machine learning tasks
- Basic understanding of JDBC, Weka, and machine learning concepts

## Setup

1. Ensure you have the necessary dependencies and libraries set up in your project.
2. Update the database connection details in the `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` variables at the top of the `Main.java` file.

## Usage

1. Run the `main` method in the `Main.java` file to execute various operations:
   -  `prepareDatabase()` to set up the database tables and populate them with CSV data.
   -  `createArffFileFromDatabase()` to create ARFF files from database tables.
   -  `predictPossumGender()` to train the J48 classifier, predict possum genders, and save the results.

2. Follow the console output and check for any errors during database setup, ARFF file creation, or gender prediction.

## Important Notes

- Make sure to handle exceptions and review the database access and file paths before running the code.
- Edit the method calls in the `main` method according to your requirements and desired workflow.

## Author

This code was developed by Sajad Kamali for educational purposes.
