# makefile for md5 library for Lua

LUA= /home/meinc/lua-5.1
LUAINC= $(LUA)/src
LUALIB= $(LUA)/src
LUABIN= /usr/bin

MD5LIB= -lcrypto

CC= gcc
CFLAGS= $(INCS) $(WARN) -O2 $G -fPIC
WARN= -std=gnu99 -pedantic -Wall -Wextra
INCS= -I$(LUAINC)
MAKESO= $(CC) -shared

MYNAME= md5
MYLIB= l$(MYNAME)
T= $(MYNAME).so
OBJS= $(MYLIB).o
TEST= test.lua


all:	test

test:	$T
	$(LUABIN)/lua $(TEST)

o:	$(MYLIB).o

so:	$T

$T:	$(OBJS)
	$(MAKESO) -o $@ $(OBJS) $(MD5LIB)

clean:
	rm -f $(OBJS) $T core core.*
