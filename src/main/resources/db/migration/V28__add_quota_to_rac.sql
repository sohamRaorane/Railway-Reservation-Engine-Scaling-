ALTER TABLE rac
    ADD COLUMN quota_id BIGINT;

UPDATE rac r
SET quota_id = b.quota_id
FROM passengers p
         JOIN bookings b ON p.booking_id = b.id
WHERE r.passenger_id = p.id;

alter table rac
    alter column quota_id set not null;


ALTER TABLE rac
    ADD CONSTRAINT fk_rac_quota
        FOREIGN KEY (quota_id)
            REFERENCES quotas(id);