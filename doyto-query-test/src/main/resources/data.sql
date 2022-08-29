SET DATABASE SQL SYNTAX MYS TRUE

INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid, create_user_id) VALUES ('f0rb', '17778888881', 'f0rb@163.com', '测试1', '123456', '高级', true, 1);
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid, create_user_id) VALUES ('user2', '17778888882', 'test2@qq.com', '测试2', '123456', '普通', false, 1);
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, memo, valid, create_user_id) VALUES ('user3', '17778888883', 'test3@qq.com', '测试3', '123456', '普通', 'memo', true, 2);
INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid, create_user_id) VALUES ('user4', '17778888884', 'test4@qq.com', '测试4', '123456', '普通', true, 2);

INSERT INTO t_menu_01 (id, platform, parent_id, menu_name, memo, valid) VALUES (1, '01', 0, 'root', 'root menu', true);
INSERT INTO t_menu_01 (id, platform, parent_id, menu_name, memo, valid) VALUES (2, '01', 1, 'first', 'first menu', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 0, 'root', 'root menu', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 1, 'User Management', 'Menu for User Management', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 1, 'Role Management', 'Menu for Role Management', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 1, 'Permission Management', 'Menu for Permission Management', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 1, 'Menu Management', 'Menu for Menu Management', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 2, 'User List', 'Menu for User List', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 3, 'Role List', 'Menu for Role List', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 4, 'Permission List', 'Menu for Permission List', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('02', 0, 'root', 'root menu', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 4, 'Permission Op1', 'Menu for Permission Op1', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 4, 'Permission Op2', 'Menu for Permission Op2', true);
INSERT INTO t_menu (platform, parent_id, menu_name, memo, valid) VALUES ('00', 4, 'Permission Op3', 'Menu for Permission Op3', true);

INSERT INTO t_role (role_name, role_code, create_user_id) VALUES ('admin', 'ADMIN', 1);
INSERT INTO t_role (role_name, role_code, create_user_id) VALUES ('vip', 'VIP', 2);
INSERT INTO t_role (role_name, role_code, create_user_id) VALUES ('vip2', 'VIP2', 2);
INSERT INTO t_role (role_name, role_code, create_user_id) VALUES ('vip3', 'VIP3', 0);
INSERT INTO t_role (role_name, role_code) VALUES ('vip4', 'VIP4');

INSERT INTO j_user_and_role (user_id, role_id) VALUES (1, 1);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (1, 2);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (3, 1);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (4, 1);
INSERT INTO j_user_and_role (user_id, role_id) VALUES (4, 2);

INSERT INTO t_perm (perm_name, valid) VALUES ('user:list', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('user:get', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('user:update', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('user:delete', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('role:list', true);
INSERT INTO t_perm (perm_name, valid) VALUES ('perm:list', true);

INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (1, 3);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (2, 3);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (2, 5);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (2, 6);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (5, 1);
INSERT INTO j_role_and_perm (role_id, perm_id) VALUES (5, 4);

INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (1, 1);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (1, 2);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (1, 6);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (5, 1);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (5, 3);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (5, 7);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (6, 1);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (6, 4);
INSERT INTO j_perm_and_menu (perm_id, menu_id) VALUES (6, 8);
