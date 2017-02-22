package com.anton.fastcloud.bindings;

@SuppressWarnings("OctalInteger")
public class SystemNative {
    /**
     * Directory
     */
    public static int S_IFDIR = 0040000;

    /**
     * Character device
     */
    public static int S_IFCHR = 0020000;

    /**
     * Block device
     */
    public static int S_IFBLK = 0060000;

    /**
     * Regular file
     */
    public static int S_IFREG = 0100000;

    /**
     * FIFO
     */
    public static int S_IFIFO = 0010000;

    /**
     * Symbolic link
     */
    public static int S_IFLNK = 0120000;

    /**
     * Socket
     */
    public static int S_IFSOCK = 0140000;

    /**
     * Set user ID on execution
     */
    public static int S_ISUID = 04000;

    /**
     * Set group ID on execution
     */
    public static int S_ISGID = 02000;

    /**
     * Save swapped text after use (sticky)
     */
    public static int S_ISVTX = 01000;

    /**
     * Read by owner
     */
    public static int S_IREAD = 0400;

    /**
     * Write by owner
     */
    public static int S_IWRITE = 0200;

    /**
     * Execute by owner
     */
    public static int S_IEXEC = 0100;


    /**
     * Operation not permitted
     */
    public static int EPERM = 1;

    /**
     * No such file or directory
     */
    public static int ENOENT = 2;

    /**
     * No such process
     */
    public static int ESRCH = 3;

    /**
     * Interrupted system call
     */
    public static int EINTR = 4;

    /**
     * I/O error
     */
    public static int EIO = 5;

    /**
     * No such device or address
     */
    public static int ENXIO = 6;

    /**
     * Argument list too long
     */
    public static int E2BIG = 7;

    /**
     * Exec format error
     */
    public static int ENOEXEC = 8;

    /**
     * Bad file number
     */
    public static int EBADF = 9;

    /**
     * No child processes
     */
    public static int ECHILD = 10;

    /**
     * Try again
     */
    public static int EAGAIN = 11;

    /**
     * Out of memory
     */
    public static int ENOMEM = 12;

    /**
     * Permission denied
     */
    public static int EACCES = 13;

    /**
     * Bad address
     */
    public static int EFAULT = 14;

    /**
     * Block device required
     */
    public static int ENOTBLK = 15;

    /**
     * Device or resource busy
     */
    public static int EBUSY = 16;

    /**
     * File exists
     */
    public static int EEXIST = 17;

    /**
     * Cross-device link
     */
    public static int EXDEV = 18;

    /**
     * No such device
     */
    public static int ENODEV = 19;

    /**
     * Not a directory
     */
    public static int ENOTDIR = 20;

    /**
     * Is a directory
     */
    public static int EISDIR = 21;

    /**
     * Invalid argument
     */
    public static int EINVAL = 22;

    /**
     * File table overflow
     */
    public static int ENFILE = 23;

    /**
     * Too many open files
     */
    public static int EMFILE = 24;

    /**
     * Not a typewriter
     */
    public static int ENOTTY = 25;

    /**
     * Text file busy
     */
    public static int ETXTBSY = 26;

    /**
     * File too large
     */
    public static int EFBIG = 27;

    /**
     * No space left on device
     */
    public static int ENOSPC = 28;

    /**
     * Illegal seek
     */
    public static int ESPIPE = 29;

    /**
     * Read-only file system
     */
    public static int EROFS = 30;

    /**
     * Too many links
     */
    public static int EMLINK = 31;

    /**
     * Broken pipe
     */
    public static int EPIPE = 32;

    /**
     * Math argument out of domain of func
     */
    public static int EDOM = 33;

    /**
     * Math result not representable
     */
    public static int ERANGE = 34;


    public static int O_ACCMODE = 0003;
    public static int O_RDONLY = 00;
    public static int O_WRONLY = 01;
    public static int O_RDWR = 02;
    public static int O_CREAT = 0100;
    public static int O_EXCL = 0200;
    public static int O_NOCTTY = 0400;
    public static int O_TRUNC = 01000;
    public static int O_APPEND = 02000;
    public static int O_NONBLOCK = 04000;
    public static int O_NDELAY = O_NONBLOCK;
    public static int O_SYNC = 04010000;
    public static int O_FSYNC = O_SYNC;
    public static int O_ASYNC = 020000;


    public static class timespec {
        /**
         * whole seconds (valid values are >= 0)
         */
        public long tv_sec;

        /**
         * nanoseconds (valid values are [0, 999999999])
         */
        public long tv_nsec;

        public timespec(long tv_sec, long tv_nsec) {
            this.tv_sec = tv_sec;
            this.tv_nsec = tv_nsec;
        }
    }

    public static class stat {
        /**
         * ID of device containing file
         */
        public long st_dev;

        /**
         * inode number
         */
        public long st_ino;

        /**
         * file type and mode
         */
        public int st_mode;

        /**
         * number of hard links
         */
        public long st_nlink;

        /**
         * user ID of owner
         */
        public int st_uid;

        /**
         * group ID of owner
         */
        public int st_gid;

        /**
         * device ID (if special file)
         */
        public long st_rdev;

        /**
         * total size, in bytes
         */
        public long st_size;

        /**
         * blocksize for filesystem I/O
         */
        public long st_blksize;

        /**
         * number of 512B blocks allocated
         */
        public long st_blocks;

        /**
         * time of last access
         */
        public timespec st_atim = new timespec(0, 0);

        /**
         * time of last modification
         */
        public timespec st_mtim = new timespec(0, 0);

        /**
         * time of last status change
         */
        public timespec st_ctim = new timespec(0, 0);

        public stat() {
        }

        public stat(
                long st_dev,
                long st_ino,
                int st_mode,
                long st_nlink,
                int st_uid,
                int st_gid,
                long st_rdev,
                long st_size,
                long st_blksize,
                long st_blocks,
                timespec st_atim,
                timespec st_mtim,
                timespec st_ctim
        ) {
            this.st_dev = st_dev;
            this.st_ino = st_ino;
            this.st_mode = st_mode;
            this.st_nlink = st_nlink;
            this.st_uid = st_uid;
            this.st_gid = st_gid;
            this.st_rdev = st_rdev;
            this.st_size = st_size;
            this.st_blksize = st_blksize;
            this.st_blocks = st_blocks;
            this.st_atim = st_atim;
            this.st_mtim = st_mtim;
            this.st_ctim = st_ctim;
        }
    }
}
