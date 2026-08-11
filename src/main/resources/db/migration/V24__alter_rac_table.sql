


ALTER TABLE rac
    ADD CONSTRAINT uk_rac_schedule_number
        UNIQUE (schedule_id, rac_number);