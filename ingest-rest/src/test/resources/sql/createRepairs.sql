-- create a bag
INSERT INTO bag VALUES(1, CURRENT_DATE, CURRENT_DATE, 'bag-0', 'admin', 'test-depositor', 'bags/test-bag-0', 'tokens/test-bag-0', 'token-fixity', 'tag-fixity', 'STAGED', 'SHA-256', 1500, 5, 3);

-- create repair requests
--                        id created_at    updated_at    status requester  to_node bag_id fulfillment_id
INSERT INTO repair VALUES(1, CURRENT_DATE, CURRENT_DATE, 'FULFILLING', 'ucsd', 4, 1, NULL);
INSERT INTO repair VALUES(2, CURRENT_DATE, CURRENT_DATE, 'REQUESTED', 'ncar', 2, 1, NULL);
INSERT INTO repair VALUES(3, CURRENT_DATE, CURRENT_DATE, 'REQUESTED', 'umiacs', 1, 1, NULL);

-- create a fulfillment strategy for one of our fulfillments
--                          id, api-key,        url,                        link, type
INSERT INTO strategy VALUES(1, 'mock-api-key', 'http://some-ace-url/ace-am', NULL, 'ACE');

-- create a fulfillment request
--                             id, current,     updated,      from, status, type, strat_id, repair_id
INSERT INTO fulfillment VALUES(1, CURRENT_DATE, CURRENT_DATE, 1, 'STAGING', NULL, NULL, 1);
INSERT INTO fulfillment VALUES(2, CURRENT_DATE, CURRENT_DATE, 2, 'READY', 'ACE', 1, 3);
