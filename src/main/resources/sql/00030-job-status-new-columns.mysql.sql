ALTER TABLE stallion_job_status
   ADD COLUMN lockedAt BIGINT NOT NULL DEFAULT 0,
   ADD COLUMN lockGuid varchar(50),
   ADD COLUMN nextExecuteMinuteStamp varchar(12),
   ADD COLUMN nextExecuteAt BIGINT DEFAULT 0,
   ADD KEY nextExecuteKey (nextExecuteMinuteStamp),
   ADD COLUMN `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   ;