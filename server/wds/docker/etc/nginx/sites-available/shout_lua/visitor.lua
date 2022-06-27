local ngx = require("ngx")
local lfs = require("lfs")
local pickle = require("simplepickle")
local wds = require("wds_core")

local visit_path_root = ngx.req.get_headers()["OP-ROOT"] == "www" and ngx.var.www_path_root or ngx.var.user_upload_path_root

local upload_dirs = {}
for path_part in string.gmatch(visit_path_root, "[^/]+") do
    table.insert(upload_dirs, path_part)
end

local function todirs(parent_dirs, entry)
    local result = {}
    for i, v in ipairs(parent_dirs) do
        table.insert(result, v)
    end
    table.insert(result, entry)
    return result
end

local function topath(dirs)
    local path = "/"..table.concat(dirs, "/")
    --ngx.log(ngx.INFO, "topath="..path)
    return path
end

local function visitor_function()
    --x ngx.log(ngx.INFO, "]1")
    local dirs_list = { upload_dirs }
    while #dirs_list > 0 do
        local dirs = table.remove(dirs_list)
        if dirs == nil then
        --x ngx.log(ngx.INFO, "]1x")
            return nil
        end
        local dirs_path = topath(dirs)
        for entry in lfs.dir(dirs_path) do
        --x ngx.log(ngx.INFO, "]2")
            local entry_dirs = todirs(dirs, entry)
            local entry_path = topath(entry_dirs)
            local dir_attr = lfs.attributes(entry_path, "mode")
            if dir_attr == "file" then
        --x ngx.log(ngx.INFO, "]3")
                if entry == "__meta.db" then
        --x ngx.log(ngx.INFO, "]4")
                    -- Lock the directory
                    local lockfile_dirs = todirs(dirs, "__meta.db.__lock__")
                    local lockfile_path = topath(lockfile_dirs)
                    local lockfile, success, err
                    while true do
                        lockfile, err = io.open(lockfile_path, "w")
                        if not lockfile then
                            errr.response("UPLOAD failed to open lockfile "..lockfile_path..": "..tostring(err))
                            return nil
                        else
                            success, err = lfs.lock(lockfile, "w")
                            if success then
                                break
                            else
                                ngx.log(ngx.ALERT, "Lock prevented readwrite open for "..lockfile_path..": "..err)
                                lockfile:close()
                                lockfile = nil
                                ngx.sleep(0.1)
                            end
                        end
                    end

        --x ngx.log(ngx.INFO, "]5")
                    -- Lock the directory
                    -- Read the metadata
                    local db, err = io.open(entry_path, "r");
                    if not db then
                        errr.response("UPLOAD failed to open hash db "..entry_path..": "..tostring(err))
                        return nil
                    end
                    local db_string = db:read("*a")
                    db:close()
                    local db_dict = pickle.unpickle(db_string)

        --x ngx.log(ngx.INFO, "]6")
                    -- Iterate over the metadata
                    for filename, metadata_string in pairs(db_dict) do
                        local file_dirs = todirs(dirs, filename)
                        local file_path = topath(file_dirs)
                        local metadata_dict = pickle.unpickle(metadata_string)
                        coroutine.yield(file_path, metadata_dict)
                    end

        --x ngx.log(ngx.INFO, "]7")
                    -- Unlock the directory
                    os.remove(lockfile_path)
                    lockfile:close()
                    lockfile = nil
                end
            elseif dir_attr == "directory" then
                if entry ~= "." and entry ~= ".." then
        --x ngx.log(ngx.INFO, "]8")
                    table.insert(dirs_list, todirs(dirs, entry))
                end
            end
        end
    end
    --x ngx.log(ngx.INFO, "]X")
end

local max_age_delete_current_entry

local function max_age_file_visitor_function(scan_path, max_age_sec)
    local now = os.time()
    local scan_dirs = {}
    for path_part in string.gmatch(scan_path, "[^/]+") do
        table.insert(scan_dirs, path_part)
    end
    local dirs_list = { scan_dirs }
    while #dirs_list > 0 do
        local dirs = table.remove(dirs_list)
        if dirs == nil then
        --x ngx.log(ngx.INFO, "]1x")
            return nil
        end
        local dirs_path = topath(dirs)
        local dir_lockfile, dir_metadata, err
        for entry in lfs.dir(dirs_path) do
        --x ngx.log(ngx.INFO, "]2")
            local entry_dirs = todirs(dirs, entry)
            local entry_path = topath(entry_dirs)
            local entry_attrs = lfs.attributes(entry_path)
            if entry_attrs.mode == "file" then
                if not entry:match("^__meta.db") then
                    local entry_age = now - entry_attrs.modification
                    if entry_age > max_age_sec then
                        if dir_lockfile == nil then
                            dir_lockfile, err = wds.wdsdir_lock(dirs)
                            if not dir_lockfile then
                                ngx.log(ngx.ERR, "While trying to expire-delete "..entry_path.." could not lock directory "..dirs_path..": "..err)
                            end
                        end
                        if dir_lockfile then
                            if dir_metadata == nil then
                                dir_metadata, err = wds.wdsdir_read_metadata(dirs)
                                if not dir_metadata then
                                    ngx.log(ngx.WARN, "While trying to expire-delete "..entry_path.." could not read metadata for directory "..dirs_path..": "..err)
                                end
                            end
                            local entry_metadata
                            if dir_metadata then
                                local entry_metadata_string = dir_metadata[entry]
                                if entry_metadata_string == nil then
                                    ngx.log(ngx.WARN, "While trying to expire-delete "..entry_path.." could not find metadata for entry "..entry)
                                else
                                    entry_metadata = pickle.unpickle(entry_metadata_string)
                                end
                            end
                            coroutine.yield(entry_path, entry_age, entry_metadata)
                            if max_age_delete_current_entry then
                                max_age_delete_current_entry = nil
                                if dir_metadata and dir_metadata[entry] ~= nil then
                                    dir_metadata[entry] = nil
                                    local success, err = wds.wdsdir_write_metadata(dirs, dir_metadata)
                                    if not success then
                                        ngx.log(ngx.ERR, "While trying to expire-delete "..entry_path.." could not write metadata for directory "..dirs_path..": "..err)
                                    end
                                end
                                local success, err = os.remove(entry_path)
                                if not success then
                                    ngx.log(ngx.ERR, "Could not expire-delete "..entry_path..": "..err)
                                end
                            end
                        end
                    end
                end
            elseif entry_attrs.mode == "directory" then
                if entry ~= "." and entry ~= ".." then
        --x ngx.log(ngx.INFO, "]8")
                    table.insert(dirs_list, todirs(dirs, entry))
                end
            end
        end
        if dir_lockfile then
            local success, err = wds.wdsdir_unlock(dirs, dir_lockfile)
            if not success then
                ngx.log(ngx.ERR, "While trying to expire-delete "..entry_path.." could not unlock directory "..dirs_path..": "..err)
            end
        end
    end
end

local _M = {}

function _M.iter_all_objects()
    --x ngx.log(ngx.INFO, ")1")
    local visitor = coroutine.create(visitor_function)
    return function()
        --x ngx.log(ngx.INFO, ")2")
        local success, filename, metadata = coroutine.resume(visitor)
        --x ngx.log(ngx.INFO, ")3:"..tostring(success))
        
        if success == true and filename == nil or success == false then
        --x ngx.log(ngx.INFO, ")4")
            return nil
        end
        --x ngx.log(ngx.INFO, ")5")
        return filename, metadata
    end
end

function _M.max_age_iter_all_files(scan_path, max_age_sec)
    --x ngx.log(ngx.INFO, ")1")
    local visitor = coroutine.create(max_age_file_visitor_function)
    return function()
        --x ngx.log(ngx.INFO, ")2")
        local success, file_path, file_age_sec, file_metadata = coroutine.resume(visitor, scan_path, max_age_sec)
        --x ngx.log(ngx.INFO, ")3:"..tostring(success))
        
        if success == true and file_path == nil or success == false then
        --x ngx.log(ngx.INFO, ")4")
            return nil
        end
        --x ngx.log(ngx.INFO, ")5")
        return file_path, file_age_sec, file_metadata
    end
end

function _M.max_age_delete_current_object()
    -- This is thread-safe because NGINX instantiates local variables per-request
    max_age_delete_current_entry = true
end

return _M
