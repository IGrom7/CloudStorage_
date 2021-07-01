package ru.grom.cloudstorage.lesson3.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

public class StringInputHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);
        System.out.println("message: " + message.replace("\n", ""));
        mainActions(ctx, message);
//        ctx.write(message);
    }

    private void mainActions(ChannelHandlerContext ctx, String msg) throws Exception {
        String[] command = msg
                .replace("\u0000", "")
                .replace("\u0010", "")
                .replace("\n", "")
                .replace("\r", "")
                .split(" ");
        if ("--help".equals(command[0])) {
//                ctx(LS_COMMAND, selector, client);
//                sendMessage(MKDIR_COMMAND, selector, client);
//                sendMessage(TOUCH_COMMAND, selector, client);
//                sendMessage(CD_COMMAND, selector, client);
//                sendMessage(RM_COMMAND, selector, client);
//                sendMessage(COPY_COMMAND, selector, client);
//                sendMessage(CAT_COMMAND, selector, client);
//                sendMessage(CHANGENICK_COMMAND, selector, client);
//        } else if ("ls".equals(command[0])) {
//            channelRead(getFilesList().concat("\n"), selector, client);
        }
        if ("mkdir".equals(command[0])) {
            channelRead(ctx, newDir(command[1], ctx));
        }
        if ("rm".equals(command[0])) {
            channelRead(ctx, deleteDirAndFiles(command[1], ctx));
        }
        if ("copy".equals(command[0])) {
            channelRead(ctx, copyDirAndFiles(command[1], command[2], ctx));
        }
        if ("cat".equals(command[0])) {
            channelRead(ctx, outPutTxtFile(command[1], ctx));
        }
        if ("touch".equals(command[0])) {
            channelRead(ctx, creatureNewFile(command[1], ctx));
        }

    }

    // touch (filename) - создание файла
    private String creatureNewFile(String newFile, ChannelHandlerContext ctx) throws IOException {
        String status = " ";
        // byte [] data = new byte[128];
        // Files.write(file, data, StandardOpenOption.APPEND);
        Path nowNewFile = Paths.get(newFile);
        if (!Files.exists(nowNewFile)) {
            Files.createFile(nowNewFile);
            status = "File " + newFile + " created";
        }
        ctx.write(status);
        return status;
    }

    // mkdir (dirname) - создание директории
    private String newDir (String dirname, ChannelHandlerContext ctx) {
        String status = " ";
        Path path = Paths.get(String.valueOf(dirname));
        try {
            Path newDir = Files.createDirectory(path);
            status = "Directory " + dirname +  " was created";
        } catch(FileAlreadyExistsException e){
            status = "Such a directory " + dirname +  " already exists";
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.write(status);
    return status;
    }

    // cd (path | ~ | ..) - изменение текущего положения
    private void changeDir () {


    }
    // rm (filename / dirname) - удаление файла / директории
    private String deleteDirAndFiles (String delete, ChannelHandlerContext ctx) {
        String status = " ";
        Path path = Paths.get(String.valueOf(delete));
        try {
            status = delete + " successfully deleted";
            Files.delete(path);
        } catch (NoSuchFileException ex) {
            status = delete + " failed to delete";
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        ctx.write(status);
        return status;
    }
    // copy (src) (target) - копирование файлов / директории
    private String copyDirAndFiles (String fromCopy, String toCopy, ChannelHandlerContext ctx) {
        String status = " ";
        Path sourcePath = Paths.get(fromCopy);
        Path destinationPath = Paths.get(toCopy);
        try {
            Files.copy(sourcePath, destinationPath,
                    StandardCopyOption.REPLACE_EXISTING);
            status = "File " + fromCopy + " copied successfully to " + toCopy;
        } catch(FileAlreadyExistsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.write(status);
        return status;
    }
    // cat (filename) - вывод содержимого текстового файла
    private String outPutTxtFile (String filename, ChannelHandlerContext ctx) {
        String status = " ";
        Path txtFile = Paths.get(filename);
        try {
            status = new String(Files.readAllBytes(txtFile));
        } catch (FileNotFoundException e) {
            status = "File " + filename + " not found";
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.write(status);
        return status;
    }
}
