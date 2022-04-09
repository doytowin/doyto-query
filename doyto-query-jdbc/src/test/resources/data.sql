SET DATABASE SQL SYNTAX MYS TRUE

INSERT INTO t_user (id, username, mobile, email, nickname, password, userLevel, valid) VALUES (1, 'f0rb', '17778888881', 'f0rb@163.com', '测试1', '123456', '高级', true);
INSERT INTO t_user (id, username, mobile, email, nickname, password, userLevel, valid) VALUES (2, 'user2', '17778888882', 'test2@qq.com', '测试2', '123456', '普通', true);
INSERT INTO t_user (id, username, mobile, email, nickname, password, userLevel, memo, valid) VALUES (3, 'user3', '17778888883', 'test3@qq.com', '测试3', '123456', '普通', 'memo', true);
INSERT INTO t_user (id, username, mobile, email, nickname, password, userLevel, valid) VALUES (4, 'user4', '17778888884', 'test4@qq.com', '测试4', '123456', '普通', true);

INSERT INTO menu_01 (id, platform, parentId, menuName, memo, valid) VALUES (1, '01', 0, 'root', 'root menu', true);
INSERT INTO menu_01 (id, platform, parentId, menuName, memo, valid) VALUES (2, '01', 1, 'first', 'first menu', true);
INSERT INTO menu (id, platform, parentId, menuName, memo, valid) VALUES (1, '02', 0, 'root', 'root menu', true);

INSERT INTO t_role (ROLENAME, ROLECODE) VALUES ('测试', 'TEST');
INSERT INTO t_role (ROLENAME, ROLECODE) VALUES ('高级', 'VIP');
INSERT INTO t_role (ROLENAME, ROLECODE) VALUES ('高级2', 'VIP2');
INSERT INTO t_role (ROLENAME, ROLECODE) VALUES ('高级3', 'VIP3');
INSERT INTO t_role (ROLENAME, ROLECODE) VALUES ('高级4', 'VIP4');

INSERT INTO j_user_and_role (user_id, role_id) VALUES (1, 1);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (1, 2);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (3, 1);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (4, 1);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (4, 2);

INSERT INTO t_perm (permName, valid) VALUES ('user:list', true);
INSERT INTO t_perm (permName, valid) VALUES ('user:get', true);
INSERT INTO t_perm (permName, valid) VALUES ('user:update', true);
INSERT INTO t_perm (permName, valid) VALUES ('user:delete', true);

INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 3);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (2, 3);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (5, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (5, 4);
