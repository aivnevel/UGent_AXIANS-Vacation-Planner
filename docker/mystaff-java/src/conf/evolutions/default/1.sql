# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table absence_counter (
  employee_id                   varchar(255) not null,
  max_days_this_year            double not null,
  last_update                   timestamp,
  last_comment                  varchar(255),
  constraint pk_absence_counter primary key (employee_id)
);

create table exact_date (
  id                            bigint auto_increment not null,
  date                          date,
  daypart                       integer,
  holiday_message_id            bigint not null,
  constraint ck_exact_date_daypart check ( daypart in (0,1)),
  constraint pk_exact_date primary key (id)
);

create table exact_date_hmh (
  id                            bigint auto_increment not null,
  date                          date,
  daypart                       integer,
  holiday_message_history_id    bigint not null,
  constraint ck_exact_date_hmh_daypart check ( daypart in (0,1)),
  constraint pk_exact_date_hmh primary key (id)
);

create table holiday_message (
  id                            bigint auto_increment not null,
  employee_id                   varchar(255),
  type                          integer,
  state                         integer,
  request_date                  timestamp,
  last_update                   timestamp,
  request_by_id                 varchar(255),
  planner_of_last_update        varchar(255),
  comment                       varchar(255),
  constraint ck_holiday_message_type check ( type in (0,1,2,3,4,5)),
  constraint ck_holiday_message_state check ( state in (0,1,2,3)),
  constraint pk_holiday_message primary key (id)
);

create table holiday_message_history (
  id                            bigint auto_increment not null,
  type                          integer,
  state                         integer,
  request_date                  timestamp,
  request_by_id                 varchar(255),
  comment                       varchar(255),
  holiday_message_id            bigint not null,
  constraint ck_holiday_message_history_type check ( type in (0,1,2,3,4,5)),
  constraint ck_holiday_message_history_state check ( state in (0,1,2,3)),
  constraint pk_holiday_message_history primary key (id)
);

create table planning_unit_state (
  id                            bigint auto_increment not null,
  holiday_message_id            bigint not null,
  unit_id                       varchar(255),
  comment                       varchar(255),
  state                         integer,
  planner_id                    varchar(255),
  constraint ck_planning_unit_state_state check ( state in (0,1,2,3)),
  constraint pk_planning_unit_state primary key (id)
);

create table settings (
  employee_id                   varchar(255) not null,
  request_filters               boolean default false not null,
  sat_on                        varchar(255),
  day_part                      varchar(255),
  constraint pk_settings primary key (employee_id)
);

create table settings_period (
  settings_employee_id          varchar(255) not null,
  period                        varchar(255) not null
);

create table settings_function (
  settings_employee_id          varchar(255) not null,
  function                      varchar(255) not null
);

create table settings_location (
  settings_employee_id          varchar(255) not null,
  location                      varchar(255) not null
);

create table settings_favorites (
  settings_employee_id          varchar(255) not null,
  favorites                     varchar(255) not null
);

create table settings_comments (
  settings_employee_id          varchar(255) not null,
  comments                      varchar(255) not null
);

create table settings_absence_type (
  settings_employee_id          varchar(255) not null,
  absencetype                   varchar(255) not null
);

create index ix_exact_date_holiday_message_id on exact_date (holiday_message_id);
alter table exact_date add constraint fk_exact_date_holiday_message_id foreign key (holiday_message_id) references holiday_message (id) on delete restrict on update restrict;

create index ix_exact_date_hmh_holiday_message_history_id on exact_date_hmh (holiday_message_history_id);
alter table exact_date_hmh add constraint fk_exact_date_hmh_holiday_message_history_id foreign key (holiday_message_history_id) references holiday_message_history (id) on delete restrict on update restrict;

create index ix_holiday_message_history_holiday_message_id on holiday_message_history (holiday_message_id);
alter table holiday_message_history add constraint fk_holiday_message_history_holiday_message_id foreign key (holiday_message_id) references holiday_message (id) on delete restrict on update restrict;

create index ix_planning_unit_state_holiday_message_id on planning_unit_state (holiday_message_id);
alter table planning_unit_state add constraint fk_planning_unit_state_holiday_message_id foreign key (holiday_message_id) references holiday_message (id) on delete restrict on update restrict;

create index ix_settings_period_settings_employee_id on settings_period (settings_employee_id);
alter table settings_period add constraint fk_settings_period_settings_employee_id foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_period_settings on settings_period (settings_employee_id);
alter table settings_period add constraint fk_settings_period_settings foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_function_settings_employee_id on settings_function (settings_employee_id);
alter table settings_function add constraint fk_settings_function_settings_employee_id foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_function_settings on settings_function (settings_employee_id);
alter table settings_function add constraint fk_settings_function_settings foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_location_settings_employee_id on settings_location (settings_employee_id);
alter table settings_location add constraint fk_settings_location_settings_employee_id foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_location_settings on settings_location (settings_employee_id);
alter table settings_location add constraint fk_settings_location_settings foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_favorites_settings_employee_id on settings_favorites (settings_employee_id);
alter table settings_favorites add constraint fk_settings_favorites_settings_employee_id foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_favorites_settings on settings_favorites (settings_employee_id);
alter table settings_favorites add constraint fk_settings_favorites_settings foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_comments_settings_employee_id on settings_comments (settings_employee_id);
alter table settings_comments add constraint fk_settings_comments_settings_employee_id foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_comments_settings on settings_comments (settings_employee_id);
alter table settings_comments add constraint fk_settings_comments_settings foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_absence_type_settings_employee_id on settings_absence_type (settings_employee_id);
alter table settings_absence_type add constraint fk_settings_absence_type_settings_employee_id foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;

create index ix_settings_absence_type_settings on settings_absence_type (settings_employee_id);
alter table settings_absence_type add constraint fk_settings_absence_type_settings foreign key (settings_employee_id) references settings (employee_id) on delete restrict on update restrict;


# --- !Downs

alter table exact_date drop constraint if exists fk_exact_date_holiday_message_id;
drop index if exists ix_exact_date_holiday_message_id;

alter table exact_date_hmh drop constraint if exists fk_exact_date_hmh_holiday_message_history_id;
drop index if exists ix_exact_date_hmh_holiday_message_history_id;

alter table holiday_message_history drop constraint if exists fk_holiday_message_history_holiday_message_id;
drop index if exists ix_holiday_message_history_holiday_message_id;

alter table planning_unit_state drop constraint if exists fk_planning_unit_state_holiday_message_id;
drop index if exists ix_planning_unit_state_holiday_message_id;

alter table settings_period drop constraint if exists fk_settings_period_settings_employee_id;
drop index if exists ix_settings_period_settings_employee_id;

alter table settings_period drop constraint if exists fk_settings_period_settings;
drop index if exists ix_settings_period_settings;

alter table settings_function drop constraint if exists fk_settings_function_settings_employee_id;
drop index if exists ix_settings_function_settings_employee_id;

alter table settings_function drop constraint if exists fk_settings_function_settings;
drop index if exists ix_settings_function_settings;

alter table settings_location drop constraint if exists fk_settings_location_settings_employee_id;
drop index if exists ix_settings_location_settings_employee_id;

alter table settings_location drop constraint if exists fk_settings_location_settings;
drop index if exists ix_settings_location_settings;

alter table settings_favorites drop constraint if exists fk_settings_favorites_settings_employee_id;
drop index if exists ix_settings_favorites_settings_employee_id;

alter table settings_favorites drop constraint if exists fk_settings_favorites_settings;
drop index if exists ix_settings_favorites_settings;

alter table settings_comments drop constraint if exists fk_settings_comments_settings_employee_id;
drop index if exists ix_settings_comments_settings_employee_id;

alter table settings_comments drop constraint if exists fk_settings_comments_settings;
drop index if exists ix_settings_comments_settings;

alter table settings_absence_type drop constraint if exists fk_settings_absence_type_settings_employee_id;
drop index if exists ix_settings_absence_type_settings_employee_id;

alter table settings_absence_type drop constraint if exists fk_settings_absence_type_settings;
drop index if exists ix_settings_absence_type_settings;

drop table if exists absence_counter;

drop table if exists exact_date;

drop table if exists exact_date_hmh;

drop table if exists holiday_message;

drop table if exists holiday_message_history;

drop table if exists planning_unit_state;

drop table if exists settings;

drop table if exists settings_period;

drop table if exists settings_function;

drop table if exists settings_location;

drop table if exists settings_favorites;

drop table if exists settings_comments;

drop table if exists settings_absence_type;

