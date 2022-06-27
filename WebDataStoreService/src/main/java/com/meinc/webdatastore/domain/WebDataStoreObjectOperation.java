package com.meinc.webdatastore.domain;

import static com.meinc.webdatastore.domain.WebDataStoreObjectOperation.OperationType.RESIZE;
import static com.meinc.webdatastore.domain.WebDataStoreObjectOperation.OperationType.SET_ROOT;
import static com.meinc.webdatastore.domain.WebDataStoreObjectOperation.OperationType.STRIP;
import static com.meinc.webdatastore.domain.WebDataStoreObjectOperation.OperationType.THUMB;

import java.io.Serializable;

import com.meinc.webdatastore.domain.WebDataStoreObject.Endpoint.Root;

public class WebDataStoreObjectOperation implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum OperationType {
        RESIZE,
        THUMB,
        SET_ROOT,
        STRIP
    }
    
    private OperationType type;
    private String[] parms;
    
    public WebDataStoreObjectOperation(OperationType type, String...parms) {
        this.type = type;
        this.parms = parms;
    }

    public OperationType getType() {
        return type;
    }

    public String[] getParms() {
        return parms;
    }
    
    public static class ResizeOperation extends WebDataStoreObjectOperation {
        private static final long serialVersionUID = 1L;
        /**
         * Example resize strings:
         * <pre>
         * "500x300"       -- Resize image such that the aspect ratio is kept,
         *                 --  the width does not exceed 500 and the height does
         *                 --  not exceed 300
         * "500x300!"      -- Resize image to 500 by 300, ignoring aspect ratio
         * "500x"          -- Resize width to 500 keep aspect ratio
         * "x300"          -- Resize height to 300 keep aspect ratio
         * "50%x20%"       -- Resize width to 50% and height to 20% of original
         * "500x300#"      -- Resize image to 500 by 300, but crop either top
         *                 --  or bottom to keep aspect ratio
         * "500x300+10+20" -- Crop image to 500 by 300 at position 10,20
         * </pre>
         */
        public ResizeOperation(String resizeString) {
            super(RESIZE, resizeString);
        }

        /**
         * Resize image while preserving aspect ratio
         * @param maxWidth max image width in pixels
         * @param maxHeight max image height in pixels
         */
        public ResizeOperation(int maxWidth, int maxHeight) {
            super(RESIZE, maxWidth+"x"+maxHeight);
        }
    }
    
    /**
     * Creates a new image that is a thumbnail of another image. The resulting
     * thumbnail image is automatically stripped of extraneous metadata.
     */
    public static class CreateThumbnailOperation extends WebDataStoreObjectOperation {
        private static final long serialVersionUID = 1L;
        /**
         * Save a thumbnail of an image to the same root as the original image
         * @param resizeString see {@link ResizeOperation#ResizeOperation(String)}
         * @param saveToUriPath the uri-path where the thumbnail will be saved
         */
        public CreateThumbnailOperation(String resizeString, String saveToUriPath) {
            super(THUMB, resizeString, saveToUriPath);
        }

        /**
         * Save a thumbnail of an image
         * @param resizeString see {@link ResizeOperation#ResizeOperation(String)}
         * @param saveToRoot the root where this image will be saved
         * @param saveToUriPath the uri-path where the thumbnail will be saved
         */
        public CreateThumbnailOperation(String resizeString, Root saveToRoot, String saveToUriPath) {
            super(THUMB, resizeString, saveToUriPath, saveToRoot.getName());
        }

        /**
         * Save a thumbnail of an image while preserving aspect ratio to the
         * same root as the original image
         * @param maxWidth max image width in pixels
         * @param maxHeight max image height in pixels
         * @param saveToUriPath the uri-path where the thumbnail will be saved
         */
        public CreateThumbnailOperation(int maxWidth, int maxHeight, String saveToUriPath) {
            super(THUMB, maxWidth+"x"+maxHeight, saveToUriPath);
        }

        /**
         * Save a thumbnail of an image while preserving aspect ratio
         * @param maxWidth max image width in pixels
         * @param maxHeight max image height in pixels
         * @param saveToUriPath the uri-path where the thumbnail will be saved
         */
        public CreateThumbnailOperation(int maxWidth, int maxHeight, Root saveToRoot, String saveToUriPath) {
            super(THUMB, maxWidth+"x"+maxHeight, saveToUriPath, saveToRoot.getName());
        }
    }

    /**
     * Changes the root of a WDS object. A root is akin to a library or collection of objects.
     * Only {@link Root#WWW} is publicly visible.
     */
    public static class SetRootOperation extends WebDataStoreObjectOperation {
        private static final long serialVersionUID = 1L;
        /**
         * Move object to another root
         * @param toRoot the destination root
         */
        public SetRootOperation(Root toRoot) {
            super(SET_ROOT, toRoot.getName());
        }

        /**
         * Move object from the specified uri-path to another root. This operation is special because it breaks the
         * convention used by virtually all other operations by allowing the target of the operation to be explicitly
         * specified as a parameter of the operation. Normally the target is specified as a parameter to the store.
         * 
         * @param fromUriPath the uri-path of the object to move
         * @param toRoot the destination root
         */
        public SetRootOperation(String fromUriPath, Root toRoot) {
            super(SET_ROOT, fromUriPath, toRoot.getName());
        }
    }
    
    /**
     * Strips image of all non-essential metadata
     */
    public static class StripOperation extends WebDataStoreObjectOperation {
        private static final long serialVersionUID = 1L;
        public StripOperation() {
            super(STRIP);
        }
    }
}
