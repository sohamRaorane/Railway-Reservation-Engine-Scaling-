CREATE TABLE quota_reservation_pool (

                                      id BIGSERIAL PRIMARY KEY,

                             schedule_id BIGINT NOT NULL,
                                        quota_id BIGINT NOT NULL,

                                        rac_limit INTEGER NOT NULL,

                                        rac_available INTEGER NOT NULL,

                                        waitlist_limit INTEGER NOT NULL,

                                        waitlist_available INTEGER NOT NULL,

                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT fk_qrp_schedule
                                            FOREIGN KEY (schedule_id)
                                                REFERENCES schedules(id),

                                        CONSTRAINT fk_qrp_quota
                                            FOREIGN KEY (quota_id)
                                                REFERENCES quotas(id),

                                        CONSTRAINT uk_qrp_schedule_quota
                                            UNIQUE(schedule_id, quota_id)
);