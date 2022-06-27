
INSERT INTO store.item (item_id, uuid, title, description, price, active, duration_type)
     VALUES (1, 'd5ccb8c9-62e9-485c-9464-cd864c0ecf70', 'Standard Service for One Year', 'Standard Service for One Year', 19.95, 1, 'YEARLY');

INSERT INTO store.item (item_id, uuid, title, description, price, active, duration_type)
     VALUES (2, '33b32b6e-dad8-4126-b608-ca2c8bbc3c39', 'Standard Service for One Month', 'Standard Service for One Month', 1.95, 1, 'MONTHLY');

INSERT INTO store.item (item_id, uuid, title, description, price, active, duration_type)
     VALUES (3, 'c5ec7bfe-b148-4e1c-892c-c1ff78c83f95', 'Premium Service for One Year', 'Premium Service for One Year', 39.95, 1, 'YEARLY');

INSERT INTO store.item (item_id, uuid, title, description, price, active, duration_type)
     VALUES (4, 'a1f42cbc-e6c5-4770-afc7-e6866df1d8c8', 'Premium Service for One Month', 'Premium Service for One Month', 3.95, 1, 'MONTHLY');


INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (1, '4df1fe5b-5344-492b-84c0-7c793538b529', 'SYNC_ONE_ACCOUNT', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (2, 'fdd30e2a-e16a-48d5-a8aa-57f8c27c357c', 'SYNC_MULTIPLE_ACCOUNTS', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (3, 'f2847eaa-9252-4a86-b4b3-7ab697500460', 'CLOUD_ARCHIVE_30_DAYS', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (4, 'fcbf9a07-2218-43ff-9af2-9d60e2924985', 'CLOUD_ARCHIVE_180_DAYS', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (5, 'bb3db7c8-6788-4a2e-8e0f-d07f053c72d6', 'EMAIL_SINGLE_TASK', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (6, '78966667-29e7-4cf7-8def-50b079c49a05', 'DELEGATE_TASK', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (7, '67e6944c-fec6-465e-af21-0edda74a850d', 'FRANKLIN_COVEY_TRAINING', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (8, '08b89004-6d66-4212-94b3-d2832c94045c', 'EMAIL_TASK_LIST', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (9, 'ff64bb6a-5af2-49f5-9a13-9081c502ea0d', 'PRIVATE_TASKS', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (10, '761096bd-1e38-4c80-a524-d1d1e7a0b181', 'CUSTOM_BACKGROUNDS', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (11, '73a85636-1cd4-42d4-96ed-ecee54008872', 'CUSTOM_TASK_FILTERS', 'NONE');

INSERT INTO store.entitlement (entitlement_id, uuid, name, duration_type)
     VALUES (12, '78e90859-f56b-4d3f-993b-ec557473858c', 'QUOTE_OF_THE_DAY', 'NONE');

-- Insert relationships for yearly standard service item
INSERT INTO store.item_entitlement (item_id, entitlement_id)
     VALUES (1, 1), -- Sync one account
            (1, 3), -- Cloud archive 30 days
            (1, 5), -- Email a single task
            (1, 6); -- Delegate a task

-- Insert relationships for monthly standard service item
INSERT INTO store.item_entitlement (item_id, entitlement_id)
     VALUES (2, 1), -- Sync one account
            (2, 3), -- Cloud archive 30 days
            (2, 5), -- Email a single task
            (2, 6); -- Delegate a task

-- Insert relationships for yearly premium service item
INSERT INTO store.item_entitlement (item_id, entitlement_id)
     VALUES (3, 2), -- Sync multiple accounts
            (3, 4), -- Cloud archive 180 days
            (3, 5), -- Email a single task
            (3, 6), -- Delegate a task
            (3, 7), -- FC training
            (3, 8), -- Email a task list
            (3, 9), -- Private tasks
            (3, 10), -- Custom backgrounds
            (3, 11), -- Custom task filters
            (3, 12); -- Quote of the day

-- Insert relationships for monthly premium service item
INSERT INTO store.item_entitlement (item_id, entitlement_id)
     VALUES (4, 2), -- Sync multiple accounts
            (4, 4), -- Cloud archive 180 days
            (4, 5), -- Email a single task
            (4, 6), -- Delegate a task
            (4, 7), -- FC training
            (4, 8), -- Email a task list
            (4, 9), -- Private tasks
            (4, 10), -- Custom backgrounds
            (4, 11), -- Custom task filters
            (4, 12); -- Quote of the day




-- abb42f5e-cdce-416e-9786-6cc6b019b9aa
-- f15a4395-d084-4ae5-8176-641c6e2a8441
-- 65e000c1-46f4-459c-90cb-8d98e29afde8
