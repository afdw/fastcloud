package com.anton.fastcloud.bindings;

import java.nio.Buffer;
import java.util.Arrays;

public class FuseLibNative {
    public static native fuse_session fuse_session_new(String[] args, fuse_lowlevel_ops ops, Object userdata);

    public static native int fuse_set_signal_handlers(fuse_session se);

    public static native int fuse_session_mount(fuse_session se, String[] args);

    public static native int fuse_session_loop(fuse_session se);

    public static native void fuse_session_unmount(fuse_session se);

    public static native Object fuse_req_userdata(fuse_req_t req);

    /**
     * Add a directory entry to the buffer
     * <p>
     * Buffer needs to be large enough to hold the entry.  If it's not,
     * then the entry is not filled in but the size of the entry is still
     * returned.  The caller can check this by comparing the bufsize
     * parameter with the returned entry size.  If the entry size is
     * larger than the buffer size, the operation failed.
     * <p>
     * From the 'stbuf' argument the st_ino field and bits 12-15 of the
     * st_mode field are used.  The other fields are ignored.
     * <p>
     * Note: offsets do not necessarily represent physical offsets, and
     * could be any marker, that enables the implementation to find a
     * specific point in the directory stream.
     *
     * @param req     request handle
     * @param buf     the point where the new entry will be added to the buffer
     * @param bufsize remaining size of the buffer
     * @param name    the name of the entry
     * @param stbuf   the file attributes
     * @param off     the offset of the next entry
     * @return the space needed for the entry
     */
    public static native long fuse_add_direntry(fuse_req_t req, byte[] buf, long bufsize, String name, SystemNative.stat stbuf, long off);

    /**
     * Reply with an error code or success.
     * <p>
     * Possible requests:
     * all except forget
     * <p>
     * Whereever possible, error codes should be chosen from the list of
     * documented error conditions in the corresponding system calls
     * manpage.
     * <p>
     * An error code of ENOSYS is sometimes treated specially. This is
     * indicated in the documentation of the affected handler functions.
     * <p>
     * The following requests may be answered with a zero error code:
     * unlink, rmdir, rename, flush, release, fsync, fsyncdir, setxattr,
     * removexattr, setlk.
     *
     * @param req request handle
     * @param err the positive error value, or zero for success
     * @return zero for success, -errno for failure to send reply
     */
    public static native int fuse_reply_err(fuse_req_t req, int err);

    /**
     * Don't send reply
     * <p>
     * Possible requests:
     * forget
     * forget_multi
     * retrieve_reply
     *
     * @param req request handle
     */
    public static native void fuse_reply_none(fuse_req_t req);

    /**
     * Reply with a directory entry
     * <p>
     * Possible requests:
     * lookup, mknod, mkdir, symlink, link
     * <p>
     * Side effects:
     * increments the lookup count on success
     *
     * @param req request handle
     * @param e   the entry parameters
     * @return zero for success, -errno for failure to send reply
     */
    public static native int fuse_reply_entry(fuse_req_t req, fuse_entry_param e);

//    /**
//     * Reply with a directory entry and open parameters
//     *
//     * currently the following members of 'fi' are used:
//     *   fh, direct_io, keep_cache
//     *
//     * Possible requests:
//     *   create
//     *
//     * Side effects:
//     *   increments the lookup count on success
//     *
//     * @param req request handle
//     * @param e the entry parameters
//     * @param fi file information
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_create(fuse_req_t req, const struct fuse_entry_param *e, const struct fuse_file_info *fi);

    /**
     * Reply with attributes
     * <p>
     * Possible requests:
     * getattr, setattr
     *
     * @param req          request handle
     * @param attr         the attributes
     * @param attr_timeout validity timeout (in seconds) for the attributes
     * @return zero for success, -errno for failure to send reply
     */
    public static native int fuse_reply_attr(fuse_req_t req, SystemNative.stat attr, double attr_timeout);

//    /**
//     * Reply with the contents of a symbolic link
//     *
//     * Possible requests:
//     *   readlink
//     *
//     * @param req request handle
//     * @param link symbolic link contents
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_readlink(fuse_req_t req, const char *link);

    /**
     * Reply with open parameters
     * <p>
     * currently the following members of 'fi' are used:
     * fh, direct_io, keep_cache
     * <p>
     * Possible requests:
     * open, opendir
     *
     * @param req request handle
     * @param fi  file information
     * @return zero for success, -errno for failure to send reply
     */
    public static native int fuse_reply_open(fuse_req_t req, fuse_file_info fi);

//    /**
//     * Reply with number of bytes written
//     *
//     * Possible requests:
//     *   write
//     *
//     * @param req request handle
//     * @param count the number of bytes written
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_write(fuse_req_t req, size_t count);

    /**
     * Reply with data
     * <p>
     * Possible requests:
     * read, readdir, getxattr, listxattr
     *
     * @param req  request handle
     * @param buf  buffer containing data
     * @param size the size of data in bytes
     * @return zero for success, -errno for failure to send reply
     */
    public static native int fuse_reply_buf(fuse_req_t req, byte[] buf, long size);

    /**
     * Reply with data
     * <p>
     * Possible requests:
     * read, readdir, getxattr, listxattr
     *
     * @param req  request handle
     * @param buf  buffer containing data
     * @param size the size of data in bytes
     * @return zero for success, -errno for failure to send reply
     */
    public static native int fuse_reply_bufNio(fuse_req_t req, Buffer buf, long size);

//    /**
//     * Reply with data copied/moved from buffer(s)
//     *
//     * Zero copy data transfer ("splicing") will be used under
//     * the following circumstances:
//     *
//     * 1. FUSE_CAP_SPLICE_WRITE is set in fuse_conn_info.want, and
//     * 2. the kernel supports splicing from the fuse device
//     *    (FUSE_CAP_SPLICE_WRITE is set in fuse_conn_info.capable), and
//     * 3. *flags* does not contain FUSE_BUF_NO_SPLICE
//     * 4. The amount of data that is provided in file-descriptor backed
//     *    buffers (i.e., buffers for which bufv[n].flags == FUSE_BUF_FD)
//     *    is at least twice the page size.
//     *
//     * In order for SPLICE_F_MOVE to be used, the following additional
//     * conditions have to be fulfilled:
//     *
//     * 1. FUSE_CAP_SPLICE_MOVE is set in fuse_conn_info.want, and
//     * 2. the kernel supports it (i.e, FUSE_CAP_SPLICE_MOVE is set in
//     fuse_conn_info.capable), and
//     * 3. *flags* contains FUSE_BUF_SPLICE_MOVE
//     *
//     * Note that, if splice is used, the data is actually spliced twice:
//     * once into a temporary pipe (to prepend header data), and then again
//     * into the kernel. If some of the provided buffers are memory-backed,
//     * the data in them is copied in step one and spliced in step two.
//     *
//     * The FUSE_BUF_SPLICE_FORCE_SPLICE and FUSE_BUF_SPLICE_NONBLOCK flags
//     * are silently ignored.
//     *
//     * Possible requests:
//     *   read, readdir, getxattr, listxattr
//     *
//     * Side effects:
//     *   when used to return data from a readdirplus() (but not readdir())
//     *   call, increments the lookup count of each returned entry by one
//     *   on success.
//     *
//     * @param req request handle
//     * @param bufv buffer vector
//     * @param flags flags controlling the copy
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_data(fuse_req_t req, struct fuse_bufvec *bufv, enum fuse_buf_copy_flags flags);
//
//    /**
//     * Reply with data vector
//     *
//     * Possible requests:
//     *   read, readdir, getxattr, listxattr
//     *
//     * @param req request handle
//     * @param iov the vector containing the data
//     * @param count the size of vector
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_iov(fuse_req_t req, const struct iovec *iov, int count);
//
//    /**
//     * Reply with filesystem statistics
//     *
//     * Possible requests:
//     *   statfs
//     *
//     * @param req request handle
//     * @param stbuf filesystem statistics
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_statfs(fuse_req_t req, const struct statvfs *stbuf);
//
//    /**
//     * Reply with needed buffer size
//     *
//     * Possible requests:
//     *   getxattr, listxattr
//     *
//     * @param req request handle
//     * @param count the buffer size needed in bytes
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_xattr(fuse_req_t req, size_t count);
//
//    /**
//     * Reply with file lock information
//     *
//     * Possible requests:
//     *   getlk
//     *
//     * @param req request handle
//     * @param lock the lock information
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_lock(fuse_req_t req, const struct flock *lock);
//
//    /**
//     * Reply with block index
//     *
//     * Possible requests:
//     *   bmap
//     *
//     * @param req request handle
//     * @param idx block index within device
//     * @return zero for success, -errno for failure to send reply
//     */
//    public static native int fuse_reply_bmap(fuse_req_t req, uint64_t idx);

    public static class fuse_session {
        public final long pointer;

        public fuse_session(long pointer) {
            this.pointer = pointer;
        }

        @Override
        public String toString() {
            return "fuse_session{" + pointer + "}";
        }
    }

    public static abstract class fuse_lowlevel_ops {
        public boolean hasMethod(String name) {
            System.out.println(name + ": " + Arrays.stream(getClass().getMethods())
                    .filter(method -> method.getName().equals(name))
                    .anyMatch(method -> method.getDeclaringClass() != fuse_lowlevel_ops.class));
            return Arrays.stream(getClass().getMethods())
                    .filter(method -> method.getName().equals(name))
                    .anyMatch(method -> method.getDeclaringClass() != fuse_lowlevel_ops.class);
        }

        public void init(Object userdata, fuse_conn_info conn) {
        }

        public void destroy(Object userdata) {
        }

        public void lookup(fuse_req_t req, long parent, String name) {
        }

        public void getattr(fuse_req_t req, long ino, fuse_file_info fi) {
        }

        public void open(fuse_req_t req, long ino, fuse_file_info fi) {
        }

        public void read(fuse_req_t req, long ino, long size, long off, fuse_file_info fi) {
        }

        public void readdir(fuse_req_t req, long ino, long size, long off, fuse_file_info fi) {
        }
    }

    public static class fuse_conn_info {
        /**
         * Major version of the protocol (read-only)
         */
        public final int proto_major;

        /**
         * Minor version of the protocol (read-only)
         */
        public final int proto_minor;
        /**
         * Capability flags that the kernel supports (read-only)
         */
        public final int capable;
        /**
         * Maximum size of the write buffer
         */
        public int max_write;
        /**
         * Maximum size of read requests. A value of zero indicates no
         * limit. However, even if the filesystem does not specify a
         * limit, the maximum size of read requests will still be
         * limited by the kernel.
         * <p>
         * NOTE: For the time being, the maximum size of read requests
         * must be set both here *and* passed to fuse_session_new()
         * using the ``-o max_read=<n>`` mount option. At some point
         * in the future, specifying the mount option will no longer
         * be necessary.
         */
        public int max_read;
        /**
         * Maximum readahead
         */
        public int max_readahead;
        /**
         * Capability flags that the filesystem wants to enable.
         * <p>
         * libfuse attempts to initialize this field with
         * reasonable default values before calling the init() handler.
         */
        public int want;

        /**
         * Maximum number of pending "background" requests. A
         * background request is any type of request for which the
         * total number is not limited by other means. As of kernel
         * 4.8, only two types of requests fall into this category:
         * <p>
         * 1. Read-ahead requests
         * 2. Asychronous direct I/O requests
         * <p>
         * Read-ahead requests are generated (if max_readahead is
         * non-zero) by the kernel to preemptively fill its caches
         * when it anticipates that userspace will soon read more
         * data.
         * <p>
         * Asynchronous direct I/O requests are generated if
         * FUSE_CAP_ASYNC_DIO is enabled and userspace submits a large
         * direct I/O request. In this case the kernel will internally
         * split it up into multiple smaller requests and submit them
         * to the filesystem concurrently.
         * <p>
         * Note that the following requests are *not* background
         * requests: writeback requests (limited by the kernel's
         * flusher algorithm), regular (i.e., synchronous and
         * buffered) userspace read/write requests (limited to one per
         * thread), asynchronous read requests (Linux's io_submit(2)
         * call actually blocks, so these are also limited to one per
         * thread).
         */
        public int max_background;

        /**
         * Kernel congestion threshold parameter. If the number of pending
         * background requests exceeds this number, the FUSE kernel module will
         * mark the filesystem as "congested". This instructs the kernel to
         * expect that queued requests will take some time to complete, and to
         * adjust its algorithms accordingly (e.g. by putting a waiting thread
         * to sleep instead of using a busy-loop).
         */
        public int congestion_threshold;

        /**
         * When FUSE_CAP_WRITEBACK_CACHE is enabled, the kernel is responsible
         * for updating mtime and ctime when write requests are received. The
         * updated values are passed to the filesystem with setattr() requests.
         * However, if the filesystem does not support the full resolution of
         * the kernel timestamps (nanoseconds), the mtime and ctime values used
         * by kernel and filesystem will differ (and result in an apparent
         * change of times after a cache flush).
         * <p>
         * To prevent this problem, this variable can be used to inform the
         * kernel about the timestamp granularity supported by the file-system.
         * The value should be power of 10.  A zero (default) value is
         * equivalent to 1000000000 (1sec).
         */
        public int time_gran;

        public fuse_conn_info(
                int proto_major,
                int proto_minor,
                int max_write,
                int max_read,
                int max_readahead,
                int capable,
                int want,
                int max_background,
                int congestion_threshold,
                int time_gran
        ) {
            this.proto_major = proto_major;
            this.proto_minor = proto_minor;
            this.max_write = max_write;
            this.max_read = max_read;
            this.max_readahead = max_readahead;
            this.capable = capable;
            this.want = want;
            this.max_background = max_background;
            this.congestion_threshold = congestion_threshold;
            this.time_gran = time_gran;
        }

        @Override
        public String toString() {
            return "fuse_conn_info{" +
                    "proto_major=" + proto_major + ", " +
                    "proto_minor=" + proto_minor + ", " +
                    "max_write=" + max_write + ", " +
                    "max_read=" + max_read + ", " +
                    "max_readahead=" + max_readahead + ", " +
                    "capable=" + capable + ", " +
                    "want=" + want + ", " +
                    "max_background=" + max_background + ", " +
                    "congestion_threshold=" + congestion_threshold + ", " +
                    "time_gran=" + time_gran +
                    "}";
        }
    }

    public static class fuse_req_t {
        public final long pointer;

        public fuse_req_t(long pointer) {
            this.pointer = pointer;
        }

        @Override
        public String toString() {
            return "fuse_req_t{" + pointer + "}";
        }
    }

    public static class fuse_entry_param {
        /**
         * Unique inode number
         * <p>
         * In lookup, zero means negative entry (from version 2.5)
         * Returning ENOENT also means negative entry, but by setting zero
         * ino the kernel may cache negative entries for entry_timeout
         * seconds.
         */
        public long ino;

        /**
         * Generation number for this entry.
         * <p>
         * If the file system will be exported over NFS, the
         * ino/generation pairs need to be unique over the file
         * system's lifetime (rather than just the mount time). So if
         * the file system reuses an inode after it has been deleted,
         * it must assign a new, previously unused generation number
         * to the inode at the same time.
         * <p>
         * The generation must be non-zero, otherwise FUSE will treat
         * it as an error.
         */
        public long generation;

        /**
         * Inode attributes.
         * <p>
         * Even if attr_timeout == 0, attr must be correct. For example,
         * for open(), FUSE uses attr.st_size from lookup() to determine
         * how many bytes to request. If this value is not correct,
         * incorrect data will be returned.
         */
        public SystemNative.stat attr = new SystemNative.stat();

        /**
         * Validity timeout (in seconds) for the attributes
         */
        public double attr_timeout;

        /**
         * Validity timeout (in seconds) for the name
         */
        public double entry_timeout;

        public fuse_entry_param() {
        }

        public fuse_entry_param(long ino, long generation, SystemNative.stat attr, double attr_timeout, double entry_timeout) {
            this.ino = ino;
            this.generation = generation;
            this.attr = attr;
            this.attr_timeout = attr_timeout;
            this.entry_timeout = entry_timeout;
        }
    }

    /**
     * Information about open files
     */
    public static class fuse_file_info {
        /**
         * Open flags.	 Available in open() and release()
         */
        public int flags;

        /**
         * In case of a write operation indicates if this was caused by a
         * writepage
         */
        public boolean writepage;

        /**
         * Can be filled in by open, to use direct I/O on this file.
         */
        public boolean direct_io;

        /**
         * Can be filled in by open, to indicate that currently
         * cached file data (that the filesystem provided the last
         * time the file was open) need not be invalidated.
         */
        public boolean keep_cache;

        /**
         * Indicates a flush operation.  Set in flush operation, also
         * maybe set in highlevel lock operation and lowlevel release
         * operation.
         */
        public boolean flush;

        /**
         * Can be filled in by open, to indicate that the file is not
         * seekable.
         */
        public boolean nonseekable;

        /**
         * Indicates that flock locks for this file should be
         * released.  If set, lock_owner shall contain a valid value.
         * May only be set in ->release().
         */
        public boolean flock_release;

        /**
         * File handle.  May be filled in by filesystem in open().
         * Available in all other file operations
         */
        public long fh;

        /**
         * Lock owner id.  Available in locking operations and flush
         */
        public long lock_owner;

        /**
         * Requested poll events.  Available in ->poll.  Only set on kernels
         * which support it.  If unsupported, this field is set to zero.
         */
        public int poll_events;

        public fuse_file_info(
                int flags,
                boolean writepage,
                boolean direct_io,
                boolean keep_cache,
                boolean flush,
                boolean nonseekable,
                boolean flock_release,
                long fh,
                long lock_owner,
                int poll_events
        ) {
            this.flags = flags;
            this.writepage = writepage;
            this.direct_io = direct_io;
            this.keep_cache = keep_cache;
            this.flush = flush;
            this.nonseekable = nonseekable;
            this.flock_release = flock_release;
            this.fh = fh;
            this.lock_owner = lock_owner;
            this.poll_events = poll_events;
        }
    }
}
