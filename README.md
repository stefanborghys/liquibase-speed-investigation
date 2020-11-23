# liquibase-speed-investigation

### Problem
On a project i'm working, we upgraded Liquibase from v3.4.1 to v3.8.9.
We noticed a drastic slowdown in speed. Almost 6x times slower than before the upgrade.
This resulted in test runs of more than 20 mins which had times of 6 mins before.

### Test setup
To pinpoint the problem we made a simple version comparison test bench.
Where each time one changelog containing two changesets is executed: 
- creating table 'user'
- filling table 'user' based upon a CSV file containing 499 rows

The same H2 database is for both tests. Only the Liquibase version differs.

### Results

#### Liquibase v3.4.1
See: Liquibase341UpdaterTest

`INFO 23/11/20 16:24: liquibase: Successfully acquired change log lock`  
`INFO 23/11/20 16:24: liquibase: Creating database history table with name: PUBLIC.DATABASECHANGELOG`  
`INFO 23/11/20 16:24: liquibase: Reading from PUBLIC.DATABASECHANGELOG`  
`INFO 23/11/20 16:24: liquibase: liquibase/changelog/databaseChangelog.xml: liquibase/changelog/databaseChangelog.xml::1::tester: Table user created`  
`INFO 23/11/20 16:24: liquibase: liquibase/changelog/databaseChangelog.xml: liquibase/changelog/databaseChangelog.xml::1::tester: ChangeSet liquibase/changelog/databaseChangelog.xml::1::tester ran successfully in 1ms`  
`INFO 23/11/20 16:24: liquibase: liquibase/changelog/databaseChangelog.xml: liquibase/changelog/databaseChangelog.xml::2::tester: Data loaded from liquibase/data/user.csv into user`  
`INFO 23/11/20 16:24: liquibase: liquibase/changelog/databaseChangelog.xml: liquibase/changelog/databaseChangelog.xml::2::tester: ChangeSet liquibase/changelog/databaseChangelog.xml::2::tester ran successfully in 162ms`  
`INFO 23/11/20 16:24: liquibase: Successfully released change log lock`  
`Migration took: 6939 millis or rounded 6 sec (rounded)`  

#### Liquibase v3.8.9
See: Liquibase389UpdaterTest

Here the test logs: 
- a huge amount of classes being loadeded, package scans, service locators, ...
- when it reaches the insert statement, every CSV line gets following log:  
`16:27:01.843 [main] DEBUG l.s.ExecutablePreparedStatementBase - Applying column parameter = 1 for column id`  
`16:27:01.843 [main] DEBUG l.s.ExecutablePreparedStatementBase - value is numeric = 1000`  
`16:27:01.843 [main] DEBUG l.s.ExecutablePreparedStatementBase - Applying column parameter = 2 for column username`  
`16:27:01.843 [main] DEBUG l.s.ExecutablePreparedStatementBase - value is string = test-user-1`  
`16:27:01.844 [main] DEBUG l.s.ExecutablePreparedStatementBase - Applying column parameter = 3 for column active`  
`16:27:01.844 [main] DEBUG l.s.ExecutablePreparedStatementBase - value is boolean = false`  
`16:27:01.844 [main] DEBUG l.s.ExecutablePreparedStatementBase - Applying column parameter = 4 for column creationDateTime`  
`16:27:01.844 [main] DEBUG l.s.ExecutablePreparedStatementBase - value is date = 2020-01-01 10:00:00.0`  
 
 - ending in:
`16:40:17.557 [main] INFO  liquibase.changelog.ChangeSet - Data loaded from liquibase/data/user.csv into user`  
`16:40:17.559 [main] INFO  liquibase.changelog.ChangeSet - ChangeSet liquibase/changelog/databaseChangelog.xml::2::tester ran successfully in 198ms`  
`16:40:17.559 [main] INFO  liquibase.executor.jvm.JdbcExecutor - INSERT INTO PUBLIC.DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2', 'tester', 'liquibase/changelog/databaseChangelog.xml', NOW(), 2, '8:5cc7871c100cf4d4060bc6a60459f178', 'loadData tableName=user', '', 'EXECUTED', NULL, NULL, '3.8.9', '6146017334')`  
`16:40:17.560 [main] DEBUG liquibase.executor.jvm.JdbcExecutor - 1 row(s) affected`  
`16:40:17.560 [main] DEBUG liquibase.executor.jvm.JdbcExecutor - Release Database Lock`  
`16:40:17.561 [main] DEBUG liquibase.executor.jvm.JdbcExecutor - UPDATE PUBLIC.DATABASECHANGELOGLOCK SET LOCKED = FALSE, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1`  
`16:40:17.561 [main] INFO  l.lockservice.StandardLockService - Successfully released change log lock`  
`16:40:17.562 [main] INFO  b.s.liquibase.TestLiquibase - Changelog: liquibase/changelog/databaseChangelog.xml - Changeset: 1 - 2020-11-23 16:40:17.357222 - EXECUTED - 3.8.9`  
`16:40:17.563 [main] INFO  b.s.liquibase.TestLiquibase - Changelog: liquibase/changelog/databaseChangelog.xml - Changeset: 2 - 2020-11-23 16:40:17.560201 - EXECUTED - 3.8.9`  
`16:40:17.563 [main] INFO  b.s.liquibase.TestLiquibase - Found 499 user(s)`  
`Migration took: 2388 millis or rounded 2 sec (rounded)`  



