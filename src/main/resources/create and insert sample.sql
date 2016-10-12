use cs4961stu05;
drop table if exists users;

create table if not exists users (id int auto_increment primary key not null,
firstname varchar(20) not null, 
lastname varchar(20) not null,
username varchar(20) not null,
pass varchar(255) not null,
CIN int,
position int not null default 2,
is_supervisor bool default 0,
supervisor_id int,
unit_id int);

insert into users (id, firstname, lastname, username, pass, CIN, position) values(1, "John", "Smith", "jsmith", "88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589", 123456789, 1);

insert into users (firstname, lastname, username, pass, CIN, position) values("Bob", "Lee", "blee", "7140cce4f5d226132b03b2942f5d0478e1823dc4f69d1eb30b03f1e195b5bb9c", 987654321, 2);
insert into users (firstname, lastname, username, pass, CIN, position) values("Joseph", "Joestar", "jojo", "5ad0000a66aac644685638066d6c1beabd796dc33a5a827ce7121a94aa78552d", 666, 0); 