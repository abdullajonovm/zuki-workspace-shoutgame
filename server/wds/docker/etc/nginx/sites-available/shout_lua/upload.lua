local errr = require("error")
local lfs = require("lfs")
local upload = require("resty.upload")
local path_hash = require("path_hash")
local md5 = require("md5")
local pickle = require("simplepickle")
local cors = require("cors")

local success
local err

-- 1. Calculate paths;

local upload_path_root = ngx.var.is_user_upload == '0' and ngx.var.www_path_root or ngx.var.user_upload_path_root

local path, relative_path_idx, err = path_hash.uri_to_file(upload_path_root, ngx.var.op_uri_path)
if not path then
    return errr.response(err)
end
local file_path = "/"..table.concat(path, "/")

local filename = path[#path]
if filename:match("__meta.db%") or filename:match("%.__new__$") then
    return errr.response("Illegal filename")
end
local filename_new = filename..".__new__"

local path_string = ""
for i, path_part in ipairs(path) do
    path_string = path_string.."/"..path_part
    local path_part_attr, err = lfs.attributes(path_string, "mode")
    if not path_part_attr then
        success, err = lfs.mkdir(path_string)
    elseif path_part_attr ~= 'directory' then
        err = 'non-directory file entry exists at '..path_string
    end
    if err or i == #path-1 then
        break
    end
end
if err then
    return errr.response(err)
end

-- 2. Create and write lock new lockfile;

path[#path] = "__meta.db.__lock__"
local lockfile_path = "/"..table.concat(path, "/")
local lockfile, err
for i = 1,3 do
    lockfile, err = io.open(lockfile_path, "w")
    if not lockfile then
        return errr.response("UPLOAD failed to open lockfile "..lockfile_path..": "..tostring(err))
    else
        success, err = lfs.lock(lockfile, "w")
        if success then
            break
        else
            ngx.log(ngx.ALERT, "Lock prevented readwrite open for "..lockfile_path..": "..err)
            lockfile:close()
            lockfile = nil
            if i < 3 then
                ngx.sleep(0.1)
            end
        end
    end
end
if lockfile == nil then
    return errr.response("UPLOAD failed to acquire readwrite lock for "..lockfile_path)
end

-- 3. Upload new file;

local chunk_size = 4096
local form, err = upload:new(chunk_size)
if not form then
    return errr.response("UPLOAD failed to read form: "..err)
end

form:set_timeout(10000)

local file
local file_path_new
local md5sum
local file_metadata = {}
local is_metadata_only, is_update_only, is_create_only
local is_patch, patch_pattern, patch_len, patch_save
while true do
    local typ, res, err = form:read()
    if not typ then
        if file then
            file:close()
        end
        return errr.response("UPLOAD failed to read form: "..err)
    end
    -- ngx.log(ngx.INFO, "==>"..typ)
    if typ == "header" then
        local k, v = unpack(res)
        -- ngx.log(ngx.INFO, ">header>"..k.."="..v)
        if k:match("^RHDR%-") or k:match("^META%-") or k:match("^PROP%-") then
            file_metadata[k] = v
        elseif k == "OP-PATCH" then
            patch_len = tonumber(v:sub(1, 5))
            if patch_len == nil then
                return errr.response("UPLOAD received bad pattern parameter: "..v)
            end
            patch_pattern = v:sub(6)
            is_patch = true
        elseif k == "OP-CREATE-ONLY" then
            is_create_only = true
        elseif k == "OP-UPDATE-ONLY" then
            is_update_only = true
        elseif k == "OP-META-ONLY" then
            is_metadata_only = true
        end
    elseif typ == "body" then
        -- ngx.log(ngx.INFO, ">body>"..res)
        if is_metadata_only then
            local file_size = lfs.attributes(file_path, "size")
            if file_size == nil then
                return errr.response("OP-META-ONLY specified without pre-existing file: "..file_path, 412)
            end
        else
            if not file then
                ngx.log(ngx.DEBUG, "Uploading file to "..file_path)
                local file_size = lfs.attributes(file_path, "size")
                if is_create_only and file_size ~= nil then
                    return errr.response("OP-CREATE-ONLY specified for pre-existing file: "..file_path, 412)
                elseif is_update_only and file_size == nil then
                    return errr.response("OP-UPDATE-ONLY specified for non-existing file: "..file_path, 412)
                end
                path[#path] = filename_new
                file_path_new = "/"..table.concat(path, "/")
                file, err = io.open(file_path_new, "w")
                if not file then
                    return errr.response("UPLOAD failed to open file "..file_path_new..": "..err)
                end
                md5sum = md5.init()
                if is_patch then
                    if patch_len == nil or patch_pattern == nil then
                        return errr.response("UPLOAD did not receive one or more patch parameters")
                    end
                    if file_size == nil then
                        return errr.response("UPLOAD cannot find file to patch at "..file_path, ngx.HTTP_NOT_FOUND)
                    end
                    local bytes_remaining = math.max(file_size-patch_len, 0)
                    local old_file, err = io.open(file_path, "r")
                    while bytes_remaining > 0 do
                        local chunk = old_file:read(math.min(bytes_remaining,chunk_size))
                        if not chunk then
                            old_file:close()
                            return errr.response("UPLOAD unexpectedly read to end of file "..file_path)
                        end
                        bytes_remaining = bytes_remaining - #chunk
                        success, err = file:write(chunk)
                        if not success then
                            old_file:close()
                            return errr.response("UPLOAD failed to write to file "..file_path..": "..err)
                        end
                        success = md5sum:update(chunk)
                        if not success then
                            old_file:close()
                            return errr.response("UPLOAD failed to calculate md5sum for file "..file_path)
                        end
                    end
                    local final_chunk = old_file:read(patch_len*2)
                    old_file:close()
                    old_file = nil
                    if final_chunk then
                        local idx = final_chunk:find(patch_pattern)
                        if idx == nil then
                            return errr.response("UPLOAD did not find pattern '"..patch_pattern.."' in final chunk '"..final_chunk.."'")
                        end
                        patch_save = final_chunk:sub(idx)
                        final_chunk = final_chunk:sub(1, idx-1)
                        success, err = file:write(final_chunk)
                        if not success then
                            return errr.response("UPLOAD failed to write to file "..file_path..": "..err)
                        end
                        success = md5sum:update(final_chunk)
                        if not success then
                            return errr.response("UPLOAD failed to calculate md5sum for file "..file_path)
                        end
                    end
                end
            end
            success, err = file:write(res)
            if not success then
                file:close()
                return errr.response("UPLOAD failed to write to file "..file_path_new..": "..err)
            end
            success = md5sum:update(res)
            if not success then
                file:close()
                return errr.response("UPLOAD failed to calculate md5sum for file "..file_path_new)
            end
        end
    elseif typ == "part_end" then
        if file then
            if patch_save then
                success, err = file:write(patch_save)
                if not success then
                    file:close()
                    return errr.response("UPLOAD failed to write to file "..file_path_new..": "..err)
                end
                success = md5sum:update(patch_save)
                if not success then
                    file:close()
                    return errr.response("UPLOAD failed to calculate md5sum for file "..file_path_new)
                end
            end
            success, err = file:close()
            if not success then
                return errr.response("UPLOAD failed to close file "..file_path_new..": "..err)
            end
            file = nil
        end
    elseif typ == "eof" then
        break
    end
end

-- 4. Read old meta db;

path[#path] = "__meta.db"
local db_path = "/"..table.concat(path, "/")
local db, err
db, err = io.open(db_path, "r");
if not db and not err:find("No such file or directory$") then
    return errr.response("UPLOAD failed to open hash db "..db_path..": "..tostring(err))
end

local db_dict
if not db then
    db_dict = {}
else
    local db_string = db:read("*a")
    db:close()
    db_dict = pickle.unpickle(db_string)
end

-- 5. Create new meta db;

path[#path] = "__meta.db.__new__"
local new_db_path = "/"..table.concat(path, "/")
local new_db, err
new_db, err = io.open(new_db_path, "w")
if not new_db then
    return errr.response("UPLOAD failed to open hash db "..new_db_path..": "..tostring(err))
end

-- 6. Copy old db keys to new db;

if not is_metadata_only then
    file_metadata['META-MD5'] = md5sum:final()
end

local file_metadata_string = pickle.pickle(file_metadata)

db_dict[filename] = file_metadata_string
local db_string = pickle.pickle(db_dict)
new_db:write(db_string)
new_db:close()

-- 7. Rename new meta db to old meta db;

success, err = os.rename(new_db_path, db_path)
if not success then
    return errr.response("UPLOAD failed to move hash db from "..new_db_path.." to "..db_path..": "..tostring(err))
end

-- 8. Rename new file to old file;

if not is_metadata_only then
    success, err = os.rename(file_path_new, file_path)
    if not success then
        return errr.response("UPLOAD failed to move filename from "..file_path_new.." to "..file_path..": "..tostring(err))
    end
end

-- 9. Unlock lockfile;

success, err = os.remove(lockfile_path)
-- It's possible for this thread to open the lock file but before locking the open file handle,
-- another thread removes the file and closes its file handle and lock. After locking the file
-- handle (which Linux allows - even though the file was deleted), this thread will eventually
-- attempt to remove the file, and an error will be returned. But this error is benign - the lock
-- file successfully served its purpose.
-- if not success then
--     return errr.response("UPLOAD failed to remove lockfile "..lockfile_path.." : "..tostring(err))
-- end
lockfile:close()

cors.handle_cors_response_headers()
