/** lmd5.c Copyright (c) 2014 SHOUT TV, Inc. **/

#include <openssl/md5.h>
#include <stdbool.h>
#include <stdlib.h>

#include "lua.h"
#include "lauxlib.h"

#define MYNAME     "md5"
#define MYVERSION  MYNAME " library for " LUA_VERSION " / Jun 2014 / \n"
#define MYTYPE     MYNAME " handle"

static char hexArrayLower[] = "0123456789abcdef";
static char hexArrayUpper[] = "0123456789ABCDEF";

static char* bytesToHexString(unsigned char* bytes, int len, bool hexLowerCase) {
    //after testing several algorithms, this is miles ahead of the others for performance
    //http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    int hexLen = len * 2;
    char* hexChars = malloc(hexLen + 1);
    for (int i = 0; i < len; i++) {
        unsigned char c = bytes[i];
        hexChars[i * 2]     = hexLowerCase ? hexArrayLower[c >> 4]   : hexArrayUpper[c >> 4];
        hexChars[i * 2 + 1] = hexLowerCase ? hexArrayLower[c & 0x0F] : hexArrayUpper[c & 0x0F];
    }
    hexChars[hexLen] = 0;
    return hexChars;
}

static int Linit(lua_State *L) {
    MD5_CTX* ctx = lua_newuserdata(L, sizeof(MD5_CTX));
    int success = MD5_Init(ctx);
    if (success == 0) {
        lua_pushnil(L);
        return 1;
    } else {
        luaL_getmetatable(L, MYTYPE);
        lua_setmetatable(L, -2);
        return 1;
    }
}

static int Lupdate(lua_State *L) {
    MD5_CTX* ctx = (MD5_CTX*) luaL_checkudata(L, 1, MYTYPE);
    size_t len;
    const char* val = luaL_checklstring(L, 2, &len);
    int success = MD5_Update(ctx, val, len);
    if (success == 0) {
        lua_pushnil(L);
        return 1;
    } else {
        lua_pushboolean(L, 1);
        return 1;
    }
}

static int Lfinal(lua_State *L) {
    unsigned char buf[16];
    MD5_CTX* ctx = (MD5_CTX*) luaL_checkudata(L, 1, MYTYPE);
    int success = MD5_Final(buf, ctx);
    if (success == 0) {
        lua_pushnil(L);
        return 1;
    } else {
        char* hexString = bytesToHexString(buf, sizeof(buf), 1);
        lua_pushstring(L, hexString);
        free(hexString);
        return 1;
    }
}

static int Lreset(lua_State *L) {
    MD5_CTX* ctx = (MD5_CTX*) luaL_checkudata(L, 1, MYTYPE);
    int success = MD5_Init(ctx);
    if (success == 0) {
        lua_pushnil(L);
        return 1;
    } else {
        lua_pushboolean(L, 1);
        return 1;
    }
}

static const luaL_Reg R[] = {
    { "init",   Linit  },
    { "update", Lupdate},
    { "final",  Lfinal },
    { "reset",  Lreset },
    { NULL,     NULL   }
};

LUALIB_API int luaopen_md5(lua_State *L) {
    luaL_newmetatable(L, MYTYPE);  //push metadata table
    lua_setglobal(L, MYNAME);      //pop  metadata table, assign global ref to it
    luaL_register(L, MYNAME, R);   //push metadata table and use it as module table
    lua_pushliteral(L, "__index"); //push index pointer key
    lua_pushvalue(L, -2);          //push ref to module/metadata table
    lua_settable(L, -3);           //pop  pointer key, and ref to module/metadata
                                   //  set module/metadata table as index pointer for itself
                                   //  any table/userdata with this table set as metadata
                                   //    will allow library method calls on it
    return 1;
}
