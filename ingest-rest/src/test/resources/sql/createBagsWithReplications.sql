INSERT INTO bag VALUES(1, 'bag-0', 'test-depositor', 'bags/test-bag-0', 'tokens/test-bag-0', '', '', 'STAGED', 'SHA-256', 1500, 5, 3);
INSERT INTO bag VALUES(2, 'bag-1', 'test-depositor', 'bags/test-bag-1', 'tokens/test-bag-1', '', '', 'STAGED', 'SHA-256', 1500, 5, 3);

INSERT INTO bag_replications(bag_id, node_id) VALUES (1, 1);
INSERT INTO bag_replications(bag_id, node_id) VALUES (1, 2);
