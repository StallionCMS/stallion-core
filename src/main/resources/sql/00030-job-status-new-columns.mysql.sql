ALTER TABLE stallion_job_status
   ADD COLUMN lockedAt BIGINT NOT NULL DEFAULT 0,
   ADD COLUMN lockGuid varchar(50) NOT NULL DEFAULT '',
   ADD COLUMN nextExecuteMinuteStamp varchar(12),
   ADD COLUMN nextExecuteAt DATETIME,
   ADD KEY nextExecuteKey (nextExecuteMinuteStamp)
   ;