CREATE TABLE PROJECT ( PROJECT_ID INT NOT NULL, NAME VARCHAR(50), TEAM_LEAD INT, PRIMARY KEY (PROJECT_ID) )
CREATE TABLE EMPLOYEE ( ID INT NOT NULL, NAME VARCHAR(50), MANAGER_ID INT, PRIMARY KEY (ID), CONSTRAINT EMPLOYEE_MANAGER FOREIGN KEY (MANAGER_ID) REFERENCES EMPLOYEE(ID))
CREATE TABLE WORKS_ON ( PROJECT_ID INT NOT NULL, EMPLOYEE_ID INT NOT NULL, START_DATE DATE, END_DATE DATE, PRIMARY KEY (PROJECT_ID, EMPLOYEE_ID), CONSTRAINT WORKSON_EMPLOYEE FOREIGN KEY (EMPLOYEE_ID) REFERENCES EMPLOYEE(ID), FOREIGN KEY (PROJECT_ID) REFERENCES PROJECT(PROJECT_ID) )
CREATE TABLE PERSON ( PERSON_ID INT NOT NULL, NAME VARCHAR(50), PRIMARY KEY (PERSON_ID) )
CREATE TABLE ADDRESS_PERSON ( ADDRESS_ID INT NOT NULL, NAME VARCHAR(50), PRIMARY KEY (ADDRESS_ID), CONSTRAINT TO_PERSON FOREIGN KEY (ADDRESS_ID) REFERENCES PERSON(PERSON_ID))
CREATE TABLE MULTI_PERSON ( PERSON_ID INT NOT NULL, PERSON_COMPID INT NOT NULL, NAME VARCHAR(50), PRIMARY KEY (PERSON_ID, PERSON_COMPID) )
CREATE TABLE ADDRESS_MULTI_PERSON ( ADDRESS_ID INT NOT NULL, ADDRESS_COMPID INT NOT NULL, NAME VARCHAR(50), PRIMARY KEY (ADDRESS_ID, ADDRESS_COMPID), CONSTRAINT TO_MULTI_PERSON FOREIGN KEY (ADDRESS_ID, ADDRESS_COMPID) REFERENCES MULTI_PERSON(PERSON_ID, PERSON_COMPID))
ALTER TABLE PROJECT ADD CONSTRAINT PROJECT_MANAGER FOREIGN KEY (TEAM_LEAD) REFERENCES EMPLOYEE(ID)
