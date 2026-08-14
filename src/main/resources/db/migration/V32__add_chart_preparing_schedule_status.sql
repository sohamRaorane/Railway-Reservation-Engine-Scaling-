ALTER TABLE schedules
    DROP CONSTRAINT IF EXISTS chk_schedules_status;

ALTER TABLE schedules
    ADD CONSTRAINT chk_schedules_status
        CHECK (
            status IN (
                       'OPEN',
                       'CLOSED',
                       'CHART_PREPARING',
                       'CHART_PREPARED'
                )
            );