package com.anton.fastcloud;

import com.anton.fastcloud.bindings.Bindings;
import com.anton.fastcloud.bindings.FuseLibNative;
import com.anton.fastcloud.bindings.SystemNative;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("OctalInteger")
public class FileSystemMain {
    static {
        Bindings.init();
    }

    public static void main(String[] args) {
        String[] fuseArgs = new String[args.length + 1];
        fuseArgs[0] = "fastcloud";
        System.arraycopy(args, 0, fuseArgs, 1, args.length);
        String fileName = "hello";
        byte[] fileContent = new byte[1 * 1024 * 1024 * 1024];
        new Random().nextBytes(fileContent);
//        byte[] fileContent = "Hello, World!\n".getBytes(StandardCharsets.UTF_8);
        ByteBuffer fileContentBuffer = ByteBuffer.allocateDirect(fileContent.length);
        fileContentBuffer.put(fileContent);
        fileContentBuffer.position(0);
        FuseLibNative.fuse_session fuse_session = FuseLibNative.fuse_session_new(
                fuseArgs,
                new FuseLibNative.fuse_lowlevel_ops() {
                    @Override
                    public void lookup(FuseLibNative.fuse_req_t req, long parent, String name) {
                        FuseLibNative.fuse_entry_param fuse_entry_param = new FuseLibNative.fuse_entry_param();
                        if (parent != 1 || !name.equals(fileName)) {
                            FuseLibNative.fuse_reply_err(req, SystemNative.ENOENT);
                        } else {
                            fuse_entry_param.ino = 2;
                            fuse_entry_param.attr_timeout = 1;
                            fuse_entry_param.entry_timeout = 1;
                            fuse_entry_param.attr.st_ino = fuse_entry_param.ino;
                            fuse_entry_param.attr.st_mode = SystemNative.S_IFREG | 0444;
                            fuse_entry_param.attr.st_nlink = 1;
                            fuse_entry_param.attr.st_size = fileContent.length;
                            FuseLibNative.fuse_reply_entry(req, fuse_entry_param);
                        }
                    }

                    @Override
                    public void getattr(FuseLibNative.fuse_req_t req, long ino, FuseLibNative.fuse_file_info fi) {
                        SystemNative.stat attr = new SystemNative.stat();
                        attr.st_ino = ino;
                        boolean found = false;
                        if (ino == 1) {
                            attr.st_mode = SystemNative.S_IFDIR | 0755;
                            attr.st_nlink = 2;
                            found = true;
                        }
                        if (ino == 2) {
                            attr.st_mode = SystemNative.S_IFREG | 0444;
                            attr.st_nlink = 1;
                            attr.st_size = fileContent.length;
                            found = true;
                        }
                        if (found) {
                            FuseLibNative.fuse_reply_attr(req, attr, 1);
                        } else {
                            FuseLibNative.fuse_reply_err(req, SystemNative.ENOENT);
                        }
                    }

                    @Override
                    public void open(FuseLibNative.fuse_req_t req, long ino, FuseLibNative.fuse_file_info fi) {
                        if (ino != 2) {
                            FuseLibNative.fuse_reply_err(req, SystemNative.EISDIR);
                        } else if ((fi.flags & 3) != SystemNative.O_RDONLY) {
                            FuseLibNative.fuse_reply_err(req, SystemNative.EACCES);
                        } else {
                            FuseLibNative.fuse_reply_open(req, fi);
                        }
                    }

                    @Override
                    public void read(FuseLibNative.fuse_req_t req, long ino, long size, long off, FuseLibNative.fuse_file_info fi) {
                        if (ino == 2) {
                            FuseLibNative.fuse_reply_bufNio(req, fileContentBuffer.slice().position((int) off), (int) size);
                        } else {
                            FuseLibNative.fuse_reply_err(req, SystemNative.ENOENT);
                        }
                    }

                    @Override
                    public void readdir(FuseLibNative.fuse_req_t req, long ino, long size, long off, FuseLibNative.fuse_file_info fi) {
                        if (ino != 1) {
                            FuseLibNative.fuse_reply_err(req, SystemNative.ENOTDIR);
                        } else {
                            String[] names = {".", "..", fileName};
                            int[] inos = {1, 1, 2};
                            int count = 3;
                            byte[] buffer = new byte[0];
                            for (long i = off; i < count; i++) {
                                byte[] entryBuf = new byte[(int) FuseLibNative.fuse_add_direntry(req, null, 0, names[(int) i], null, 0)];
                                if (buffer.length + entryBuf.length <= size) {
                                    SystemNative.stat stat = new SystemNative.stat();
                                    stat.st_ino = inos[(int) i];
                                    FuseLibNative.fuse_add_direntry(req, entryBuf, entryBuf.length, names[(int) i], stat, i + 1);
                                    byte[] newBuffer = Arrays.copyOf(buffer, buffer.length + entryBuf.length);
                                    System.arraycopy(entryBuf, 0, newBuffer, buffer.length, entryBuf.length);
                                    buffer = newBuffer;
                                } else {
                                    break;
                                }
                            }
                            FuseLibNative.fuse_reply_buf(req, buffer.length == 0 ? null : buffer, buffer.length);
                        }
                    }
                },
                "123"
        );
        FuseLibNative.fuse_set_signal_handlers(fuse_session);
        FuseLibNative.fuse_session_mount(fuse_session, fuseArgs);
        FuseLibNative.fuse_session_loop(fuse_session);
        FuseLibNative.fuse_session_unmount(fuse_session);
    }
}
