ALTER TABLE schedules
    ADD COLUMN departure_time TIME;

UPDATE schedules
SET departure_time = '10:00:00';

ALTER TABLE schedules
    ALTER COLUMN departure_time SET NOT NULL;