local errr = require("error")
local path_hash = require("path_hash")
local lfs = require("lfs")
local magick = require("magick")
local wds = require("wds_core")
local md5 = require("md5")
local pickle = require("simplepickle")

-- Verify file specified by uri-path exists

local operate_path_root = ngx.req.get_headers()["OP-ROOT"] == "www" and ngx.var.www_path_root or ngx.var.user_upload_path_root

local function verify_uri_path(uri_path)
    local path, relative_path_idx, err = path_hash.uri_to_file(operate_path_root, uri_path)
    if not path then
        errr.response("OPERATE invalid path: "..err)
        return nil
    end
    local path_string = "/"..table.concat(path, "/")
    local path_attr, err = lfs.attributes(path_string, "mode")
    if not path_attr or path_attr ~= 'file' then
        errr.response("OPERATE file not found: "..err, 404)
        return nil
    end
    return path, relative_path_idx, path_string
end

-- allow intentionally missing uri-path
local operate_uri_path = ngx.var.op_uri_path
local operate_path, operate_relative_path_idx, operate_path_string
local operate_filename
if operate_uri_path ~= "/" and operate_uri_path ~= "" then
    operate_path, operate_relative_path_idx, operate_path_string = verify_uri_path(operate_uri_path)
    if operate_path == nil then
        return
    end
    operate_filename = operate_path[#operate_path]
    operate_path[#operate_path] = nil
end

-- Parse operation parameters

local op_types = {}
local op_parms = {}
local parm_idx = 1
local req_headers = ngx.req.get_headers()
while true do
    local op_val = req_headers['OP-'..tostring(parm_idx)]
    if op_val == nil then
        break
    elseif type(op_val) ~= "string" then
        return errr.response("OPERATE received invalid parameter "..parm_idx)
    end
    parm_idx = parm_idx + 1

    -- parse op type
    local op_val_idx = 1
    local op_type_lens = op_val:sub(op_val_idx, op_val_idx+2-1)
    op_val_idx = op_val_idx + 2
    local op_type_len = tonumber(op_type_lens)
    local op_type = op_val:sub(op_val_idx, op_val_idx+op_type_len-1)
    op_val_idx = op_val_idx + op_type_len
    table.insert(op_types, op_type)

    -- parse parameter list
    local plist_lens = op_val:sub(op_val_idx, op_val_idx+2-1)
    local plist_len = tonumber(plist_lens)
    op_val_idx = op_val_idx + 2
    local plist = {}
    for plist_idx = 1, plist_len do
        local parm_lens = op_val:sub(op_val_idx, op_val_idx+3-1)
        local parm_len = tonumber(parm_lens)
        op_val_idx = op_val_idx + 3
        local parm = op_val:sub(op_val_idx, op_val_idx+parm_len-1)
        op_val_idx = op_val_idx + parm_len
        table.insert(plist, parm)
    end
    table.insert(op_parms, plist)
end

-- Execute operations

local function sum_uri_path(uri_paths_to_sum, img_path_root, img_uri_path)
    table.insert(uri_paths_to_sum, {img_path_root, img_uri_path})
end

local function move_uri_path(uri_paths_to_move, from_path_root, from_uri_path, to_path_root, to_uri_path)
    table.insert(uri_paths_to_move, {from_path_root, from_uri_path, to_path_root or from_path_root, to_uri_path or from_uri_path})
end

local function get_current_image_object(current_image_object, current_image_uri_path)
    if current_image_object == nil or current_image_object.img == nil then
        if current_image_uri_path == nil then
            return nil
        end
        local img_path, img_relative_path_idx, err = path_hash.uri_to_file(operate_path_root, current_image_uri_path)
        if not img_path then
            errr.response("OPERATE bad image: "..err)
            return nil
        end
        local img_path_string = "/"..table.concat(img_path, "/")

        local img, err = magick.load_image(img_path_string)
        if img == nil then
            errr.response("OPERATE bad image: "..err)
            return nil
        end
        current_image_object = current_image_object and current_image_object or {}
        current_image_object.img = img
        current_image_object.uri_path = current_image_uri_path
    end
    return current_image_object
end

local function get_current_image(current_image_object, current_image_uri_path)
    local current_image_obj = get_current_image_object(current_image_object, current_image_uri_path)
    if current_image_obj == nil then
        return nil
    end
    return current_image_obj.img, current_image_obj.uri_path
end

local function cleanup(current_image_object, img_uri_paths, lockdir1, lockfile1, lockdir2, lockfile2)
    if current_image_object ~= nil and current_image_object.img ~= nil then
        current_image_object.img:destroy()
        current_image_object.img = nil
        current_image_object.uri_path = nil
    end
    if img_uri_paths ~= nil then
        for _,img_uri_path in ipairs(img_uri_paths) do
            local img_path, img_relative_path_idx, err = path_hash.uri_to_file(operate_path_root, img_uri_path)
            if img_path then
                local img_path_string = "/"..table.concat(img_path, "/")
                local success, err = os.remove(img_path_string)
            end
        end
    end
    if lockdir1 ~= nil and lockfile1 ~= nil then
        wds.wdsdir_unlock(lockdir1, lockfile1)
    end
    if lockdir2 ~= nil and lockfile2 ~= nil then
        wds.wdsdir_unlock(lockdir2, lockfile2)
    end
end

local current_image_object = {}
local uri_paths_to_sum = {}
local uri_paths_to_move = {}
local do_final_strip = false

for i, op_type in ipairs(op_types) do
    local plist = op_parms[i]

    if op_type == "RESIZE" then
        ngx.log(ngx.DEBUG, "EXECUTING RESIZE OPERATION")
        if plist[1] == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad parameters")
        end
        local img = get_current_image(current_image_object, operate_uri_path)
        if img == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad image "..operate_path_string)
        end

        magick.smart_resize(img, plist[1])
        img:set_quality(70)

    elseif op_type == "THUMB" then
        ngx.log(ngx.DEBUG, "EXECUTING THUMB OPERATION")
        if plist[1] == nil or plist[2] == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad parameters")
        end
        local thumb_size = plist[1]
        local thumb_uri_path = plist[2]
        local thumb_dest_root = plist[3] or operate_path_root

        if thumb_dest_root ~= "www" and thumb_dest_root ~= "upload" then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad parameters")
        end

        local thumb_dest_path_root = thumb_dest_root == "www" and ngx.var.www_path_root or ngx.var.user_upload_path_root
        if thumb_dest_path_root == operate_path_root then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE cannot change root from "..operate_path_root.." to "..thumb_dest_path_root)
        end

        -- Read the image afresh
        local img = get_current_image(nil, operate_uri_path)
        if img == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad image "..operate_path_string)
        end

        local img, opts = magick.smart_resize(img, thumb_size)
        img:strip()
        img:set_quality(70)

        local thumb_path, thumb_relative_path_idx, err = path_hash.uri_to_file(thumb_dest_path_root, thumb_uri_path)
        local thumb_path_string = "/"..table.concat(thumb_path, "/")
        local thumb_filename = thumb_path[#thumb_path]
        thumb_path[#thumb_path] = nil

        local success, err = wds.wdsdir_mkdirs(thumb_path)
        if not success then
            return errr.response("OPERATE failed to prepare thumbnail directories for "..thumb_path_string..": "..err)
        end

        local success, err = img:write(thumb_path_string)
        if not success then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE failed to write thumbnail to "..thumb_path_string..": "..err)
        end

        img:write(thumb_path_string)
        img:destroy()

        sum_uri_path(uri_paths_to_sum, thumb_dest_path_root, thumb_uri_path)

    elseif op_type == "SET_ROOT" then
        ngx.log(ngx.DEBUG, "EXECUTING SET_ROOT OPERATION")
        if plist[1] == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad parameters")
        end

        local src_uri_path = plist[1]
        local dest_root = plist[2]
        if dest_root == nil then
            dest_root = src_uri_path
            src_uri_path = operate_uri_path
        else
            local src_path, src_relative_path_idx, src_path_string = verify_uri_path(src_uri_path)
            if src_path == nil then
                cleanup(current_image_object, uri_paths_to_sum)
                return
            end
        end

        if dest_root ~= "www" and dest_root ~= "upload" then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad parameters")
        end

        local dest_path_root = dest_root == "www" and ngx.var.www_path_root or ngx.var.user_upload_path_root
        if dest_path_root == operate_path_root then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE cannot change root from "..operate_path_root.." to "..dest_path_root)
        end

        local dest_path, dest_relative_path_idx, err = path_hash.uri_to_file(dest_path_root, operate_uri_path)
        local dest_path_string = "/"..table.concat(dest_path, "/")

        move_uri_path(uri_paths_to_move, operate_path_root, src_uri_path, dest_path_root)

    elseif op_type == "CROP" then
        if plist[1] == nil or plist[2] == nil or plist[3] == nil or plist[4] == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return errr.response("OPERATE bad parameters")
        end
        local img = get_current_image(current_image_object, operate_uri_path)
        if img == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return
        end
        local width  = tonumber(plist[1])
        local height = tonumber(plist[2])
        local x      = tonumber(plist[3])
        local y      = tonumber(plist[4])

        img:crop(width, height, x, y)

    elseif op_type == "STRIP" then
        local img = get_current_image(current_image_object)
        do_final_strip = (img == nil)
        img = img and img or get_current_image(current_image_object, operate_uri_path)
        if img == nil then
            cleanup(current_image_object, uri_paths_to_sum)
            return
        end

        if not do_final_strip then
            ngx.log(ngx.DEBUG, "EXECUTING LOCAL STRIP OPERATION")
            img:strip()
        end
    end
end

-- Write image pointed to by URI-path if appropriate
local current_img = get_current_image(current_image_object)
if current_img ~= nil then
    local img_lockfile, err = wds.wdsdir_lock(operate_path)
    if img_lockfile == nil then
        cleanup(current_image_object, uri_paths_to_sum)
        return errr.response("OPERATE "..err)
    end

    ngx.log(ngx.DEBUG, "WRITING IMAGE "..operate_uri_path.." TO "..operate_path_string)
    current_img:write(operate_path_string)
    current_img:destroy()
    current_image_object = {}

    wds.wdsdir_unlock(operate_path, img_lockfile)
    sum_uri_path(uri_paths_to_sum, operate_path_root, operate_uri_path)
end

if do_final_strip then
    --Xlocal img_path, img_relative_path_idx, err = path_hash.uri_to_file(operate_path_root, operate_uri_path)
    --Xlocal img_path_string = "/"..table.concat(img_path, "/")
    ngx.log(ngx.DEBUG, "STRIPPING IMAGE "..operate_path_string)
    local proc = io.popen("exiv2 rm "..operate_path_string)
    local output = proc:read("*a")
    if output ~= nil and #output > 0 then
        ngx.log(ngx.ALERT, "FINAL STRIP OF "..operate_path_string.." RETURNED: "..output)
    end
    proc:close()
    sum_uri_path(uri_paths_to_sum, operate_path_root, operate_uri_path)
end

-- Update metadata for images marked to be summed
for _,img_sum_obj in ipairs(uri_paths_to_sum) do
    local img_path_root = img_sum_obj[1]
    local img_uri_path = img_sum_obj[2]
    local img_path, img_relative_path_idx, err = path_hash.uri_to_file(img_path_root, img_uri_path)
    if not img_path then
        cleanup(nil, uri_paths_to_sum)
        return errr.response("OPERATE bad path")
    end
    local img_path_string = "/"..table.concat(img_path, "/")

    local img_filename = img_path[#img_path]
    img_path[#img_path] = nil

    local img_lockfile, err = wds.wdsdir_lock(img_path)
    if img_lockfile == nil then
        return errr.response("OPERATE "..err)
    end

    local imgfile, err = io.open(img_path_string, "r")
    if imgfile == nil then
        cleanup(nil, nil, img_path, img_lockfile)
        return errr.response("OPERATE failed to reopen image at "..img_uri_path..": "..err)
    end
    local md5sum = md5.init()
    local img_bytes = imgfile:read(8192)
    while img_bytes ~= nil do
        local success = md5sum:update(img_bytes)
        if not success then
            imgfile:close()
            cleanup(nil, nil, img_path, img_lockfile)
            return errr.response("OPERATE failed to reopen image at "..img_path_string..": "..err)
        end
        img_bytes = imgfile:read(8192)
    end
    imgfile:close()
    local md5sum_string = md5sum:final()
    --ngx.log(ngx.DEBUG, "CALCULATED MD5SUM OF "..md5sum_string.." FOR "..img_path_string)

    -- save the new MD5 sum to metadata
    local metadata, err = wds.wdsdir_read_metadata(img_path)
    if metadata == nil then
        cleanup(nil, nil, img_path, img_lockfile)
        return errr.response("OPERATE "..err)
    end

    local img_metadata_string = metadata[img_filename]
    local img_metadata = img_metadata_string and pickle.unpickle(img_metadata_string) or {}
    img_metadata['META-MD5'] = md5sum_string
    img_metadata['RHDR-Last-Modified'] = ngx.http_time(ngx.time())
    metadata[img_filename] = pickle.pickle(img_metadata)

    local success, err = wds.wdsdir_write_metadata(img_path, metadata)
    if not success then
        cleanup(nil, nil, img_path, img_lockfile)
        return errr.response("OPERATE "..err)
    end

    wds.wdsdir_unlock(img_path, img_lockfile)
end
uri_paths_to_sum = {}

-- Safely publish (move) files

for _,move_uri_path in ipairs(uri_paths_to_move) do
    local from_path_root = move_uri_path[1]
    local from_uri_path = move_uri_path[2]
    local to_path_root = move_uri_path[3]
    local to_uri_path = move_uri_path[4]

    -- calculate destinate paths

    local src_path, relative_path_idx, err = path_hash.uri_to_file(from_path_root, from_uri_path)
    local src_path_string = "/"..table.concat(src_path, "/")
    local src_filename = src_path[#src_path]
    src_path[#src_path] = nil

    local dest_path, relative_path_idx, err = path_hash.uri_to_file(to_path_root, to_uri_path)
    local dest_path_string = "/"..table.concat(dest_path, "/")
    local dest_path_string_new = dest_path_string..".__new__"
    local dest_filename = dest_path[#dest_path]
    dest_path[#dest_path] = nil

    ngx.log(ngx.DEBUG, "MOVING "..src_path_string.." TO "..dest_path_string)

    local success, err = wds.wdsdir_mkdirs(dest_path)
    if not success then
        return errr.response("OPERATE "..err)
    end

    -- lock the source directory

    local src_lockfile, err = wds.wdsdir_lock(src_path)
    if src_lockfile == nil then
        return errr.response("OPERATE "..err)
    end

    -- lock the destination directory

    local dest_lockfile, err = wds.wdsdir_lock(dest_path)
    if dest_lockfile == nil then
        cleanup(nil, nil, src_path, src_lockfile)
        return errr.response("OPERATE "..err)
    end

    -- move the file

    -- this technique works only when the file stays on the same disk
    local success, err = os.rename(src_path_string, dest_path_string_new)
    if not success then
        cleanup(nil, nil, src_path, src_lockfile, dest_path, dest_lockfile)
        return errr.response("OPERATE failed to move file from "..src_path_string.." to "..dest_path_string_new..": "..tostring(err))
    end

    -- this technique works even when the file moves between disks, however it is a lot slower
    -- local success = os.execute('mv "'..src_path_string..'" "'..dest_path_string_new..'"')
    -- if success ~= 0 then
    --     cleanup(nil, nil, src_path, src_lockfile, dest_path, dest_lockfile)
    --     return errr.response("OPERATE failed to move file from "..src_path_string.." to "..dest_path_string_new..": mv returned status "..tostring(success))
    -- end

    -- read the source metadata

    local src_metadata, err = wds.wdsdir_read_metadata(src_path)
    if src_metadata == nil then
        cleanup(nil, nil, src_path, src_lockfile, dest_path, dest_lockfile)
        return errr.response("OPERATE "..err)
    end

    -- read the destination metadata

    local dest_metadata, err = wds.wdsdir_read_metadata(dest_path)
    if dest_metadata == nil then
        cleanup(nil, nil, src_path, src_lockfile, dest_path, dest_lockfile)
        return errr.response("OPERATE "..err)
    end

    -- move the file metadata

    dest_metadata[dest_filename] = src_metadata[src_filename]
    src_metadata[src_filename] = nil

    -- write the updated source metadata

    local success, err = wds.wdsdir_write_metadata(src_path, src_metadata)
    if not success then
        cleanup(nil, nil, src_path, src_lockfile, dest_path, dest_lockfile)
        return errr.response("OPERATE "..err)
    end

    -- write the updated destination metadata

    local success, err = wds.wdsdir_write_metadata(dest_path, dest_metadata)
    if not success then
        cleanup(nil, nil, src_path, src_lockfile, dest_path, dest_lockfile)
        return errr.response("OPERATE "..err)
    end

    -- unlock the directories

    wds.wdsdir_unlock(src_path, src_lockfile)
    wds.wdsdir_unlock(dest_path, dest_lockfile)

    -- rename file for download

    local success, err = os.rename(dest_path_string_new, dest_path_string)
    if not success then
        return errr.response("OPERATE failed to move file from "..dest_path_string_new.." to "..dest_path_string..": "..tostring(err))
    end
end
