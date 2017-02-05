#!/usr/bin/python

import datetime
from sqlalchemy import Column, Integer, String, Table
from sqlalchemy import ForeignKey, DateTime, Boolean, Text, Enum
from sqlalchemy import text
from sqlalchemy.orm import relationship, backref, joinedload, sessionmaker
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.schema import ForeignKeyConstraint, ColumnDefault, UniqueConstraint
BASE = declarative_base()

def rSerialize(item):
    if isinstance(item, (str, unicode, int, float, long)):
        return item
    if isinstance(item, datetime.datetime):
        return item.strptime("%Y-%m-%dT%H:%M:%S")
    if isinstance(item, (list, tuple)):
        return [ rSerialize(_) for _ in item ]
    if isinstance(item, dict):
        _ret = {}
        for _k, _v in item.items():
            _ret[_k] = rSerialize(_v)
        return _ret
    if issubclass(item.__class__, BaseClass):
        return item.serialize()

class BaseClass(object):
    __table_args__ = {"mysql_engine": "InnoDB"}

    def serialize(self):
        _ret = {}
        for _k, _v in self.__dict__.items():
            print _k, _v
            if not _k.startswith('_'):
                _ret[_k] = rSerialize(_v)
        return _ret

class Task(BASE, BaseClass):
    __tablename__ = "task"
    id = Column(Integer, primary_key=True)
    taskType = Column(String(20))
    desc = Column(Text)
    answer = Column(String(200))

class User(BASE, BaseClass):
    __tablename__ = "user"
    email = Column(String(100), primary_key=True)
    username = Column(String(100), unique=True)
    currentTask = Column(Integer, ForeignKey("task.id"))
    progress = Column(Boolean, default=False)
    task = relationship(
        "Task",
        uselist=False,
        enable_typechecks=False)

class UserTask(BASE, BaseClass):
    __tablename__ = "usertask"
    id = Column(Integer, primary_key=True)
    email = Column(String(100), ForeignKey("user.email"))
    taskid = Column(Integer, ForeignKey("task.id"))
    punish = Column(Integer)
    startat = Column(DateTime, default=datetime.datetime.fromtimestamp(0))
    finishat = Column(DateTime, default=datetime.datetime.fromtimestamp(0))

    user = relationship(
        "User",
        uselist=False,
        enable_typechecks=False,
        backref=backref(
            "tasks",
            enable_typechecks=False,
            cascade="all,delete-orphan"),
        )
    task = relationship(
        "Task",
        uselist=False,
        enable_typechecks=False,
        backref=backref(
            "users",
            enable_typechecks=False,
            cascade="all,delete-orphan"),
        )
    __table_args__ = (
        UniqueConstraint('email', 'taskid'),
        {'mysql_engine': "InnoDB", "extend_existing": True},
        )

from sqlalchemy import create_engine

def register_models():
    engine = create_engine("mysql://enghack:enghack@awsvps/enghack")
    BASE.metadata.create_all(engine)

def mock_data():
    engine = create_engine("mysql://enghack:enghack@awsvps/enghack")
    Session = sessionmaker(bind=engine)
    session = Session()
    session.add(Task(desc="Find a restaurant!", taskType="type", answer="restaurant"))
    session.add(Task(desc="Find DC library!", taskType="place", answer="43.4729,-80.5418"))
    session.add(Task(desc="Find Clock Tower", taskType="place", answer="43.4643,-80.5259"))
    session.add(Task(desc="Find Crivket Oval", taskType="place", answer="43.4687,-80.5286"))
    session.add(Task(desc="Hug a local", taskType="any", answer="43.4643,-80.5259"))
    session.add(User(email="aa@test.com", username="aa", currentTask=1))
    session.add(User(email="bb@test.com", username="bb", currentTask=2))
    session.commit()

if __name__ == "__main__":
    register_models()
    mock_data()
