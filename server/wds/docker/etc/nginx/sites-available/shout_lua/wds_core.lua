local lfs = require("lfs")
local pickle = require("simplepickle")

local success
local err

local _M = {}

function _M.wdsdir_lock(dir_path_table, readonly)
    dir_path_table[#dir_path_table+1] = "__meta.db.__lock__"
    local lockfile_path = "/"..table.concat(dir_path_table, "/")
    dir_path_table[#dir_path_table] = nil
    local lockfile, err
    for i = 1,3 do
        lockfile, err = io.open(lockfile_path, "w")
        if not lockfile then
            return nil, "failed to open lockfile "..lockfile_path..": "..tostring(err), nil
        else
            success, err = lfs.lock(lockfile, readonly and "r" or "w")
            if success then
                break
            else
                local lock_desc = readonly and "readonly" or "readwrite"
                ngx.log(ngx.ALERT, "Lock prevented "..lock_desc.." open for "..lockfile_path..": "..err)
                lockfile:close()
                lockfile = nil
                if i < 3 then
                    ngx.sleep(0.1)
                end
            end
        end
    end
    if lockfile == nil then
        return nil, "failed to acquire readwrite lock for "..lockfile_path, nil
    end
    return lockfile, nil, nil
end

function _M.wdsdir_unlock(dir_path_table, lockfile)
    dir_path_table[#dir_path_table+1] = "__meta.db.__lock__"
    local lockfile_path = "/"..table.concat(dir_path_table, "/")
    dir_path_table[#dir_path_table] = nil
    local success, err = os.remove(lockfile_path)
    -- It's possible for this thread to open the lock file but before locking the open file handle,
    -- another thread removes the file and closes its file handle and lock. After locking the file
    -- handle (which Linux allows - even though the file was deleted), this thread will eventually
    -- attempt to remove the file, and an error will be returned. But this error is benign - the lock
    -- file successfully served its purpose.
    -- if not success then
    --     return nil, "failed to remove lockfile "..lockfile_path.." : "..tostring(err)
    -- end
    lockfile:close()
    return true, nil, nil
end

function _M.wdsdir_read_metadata(dir_path_table)
    dir_path_table[#dir_path_table+1] = "__meta.db"
    local db_path = "/"..table.concat(dir_path_table, "/")
    dir_path_table[#dir_path_table] = nil
    local db, err
    db, err = io.open(db_path, "r");
    if not db and not err:find("No such file or directory$") then
        return nil, "failed to open hash db "..db_path..": "..tostring(err), nil
    end

    local db_dict
    if not db then
        db_dict = {}
    else
        local db_string = db:read("*a")
        db:close()
        db_dict = pickle.unpickle(db_string)
    end
    return db_dict, nil, nil
end

function _M.wdsdir_write_metadata(dir_path_table, metadata)
    dir_path_table[#dir_path_table+1] = "__meta.db.__new__"
    local new_db_path = "/"..table.concat(dir_path_table, "/")
    dir_path_table[#dir_path_table] = nil
    local new_db, err
    new_db, err = io.open(new_db_path, "w")
    if not new_db then
        return nil, "failed to open hash db "..new_db_path..": "..tostring(err), nil
    end

    if type(metadata) == "table" then
        metadata = pickle.pickle(metadata)
    end
    new_db:write(metadata)
    new_db:close()

    dir_path_table[#dir_path_table+1] = "__meta.db"
    local db_path = "/"..table.concat(dir_path_table, "/")
    dir_path_table[#dir_path_table] = nil
    local success, err = os.rename(new_db_path, db_path)
    if not success then
        return nil, "failed to move hash db from "..new_db_path.." to "..db_path..": "..tostring(err), nil
    end
    return true, nil, nil
end

function _M.wdsdir_mkdirs(dir_path_table)
    local success, err
    local path_string = ""
    for i, path_part in ipairs(dir_path_table) do
        path_string = path_string.."/"..path_part
        local path_part_attr, err = lfs.attributes(path_string, "mode")
        if not path_part_attr then
            success, err = lfs.mkdir(path_string)
        elseif path_part_attr ~= 'directory' then
            err = 'non-directory file entry exists at '..path_string
        end
        if err then
            return false, err
        end
    end
    return true, nil
end

return _M
