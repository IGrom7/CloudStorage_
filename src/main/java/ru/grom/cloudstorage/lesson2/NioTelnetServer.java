package ru.grom.cloudstorage.lesson2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.net.InetAddress;

public class NioTelnetServer {
    private static final String LS_COMMAND = "\tls - view all files from current directory\n";
    private static final String MKDIR_COMMAND = "\tmkdir (dirname) - create a directory\n";
    private static final String TOUCH_COMMAND = "\ttouch (filename) - create a file\n";
    private static final String CD_COMMAND = "\tcd (path |~| ..) - change the current position\n";
    private static final String RM_COMMAND = "\trm (filename / dirname) - delete file / directory\n";
    private static final String COPY_COMMAND = "\tcopy (src) (target) - copy files / directories\n";
    private static final String CAT_COMMAND = "\tcat (filename) - output the contents of a text file\n";
    private static final String CHANGENICK_COMMAND = "\tchangenick (nickname) - change username\n";


    private final ByteBuffer buffer = ByteBuffer.allocate(512);

    private Map<SocketAddress, String> clients = new HashMap<>();

    public NioTelnetServer() throws Exception {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(5679));
        server.configureBlocking(false);
        Selector selector = Selector.open();

        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key, selector);
                }
                iterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        String nickName = InetAddress.getLocalHost().getHostName();
        System.out.println("Client connected. IP:" + channel.getRemoteAddress());
        System.out.println(nickName);
        channel.register(selector, SelectionKey.OP_READ, "ghbdtnvbh");
        channel.write(ByteBuffer.wrap("Hello user!\n".getBytes(StandardCharsets.UTF_8)));
        channel.write(ByteBuffer.wrap("Enter --help for support info".getBytes(StandardCharsets.UTF_8)));
    }


    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAddress client = channel.getRemoteAddress();
        int readBytes = channel.read(buffer);

        if (readBytes < 0) {
            channel.close();
            return;
        } else  if (readBytes == 0) {
            return;
        }

        buffer.flip();
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            sb.append((char) buffer.get());
        }
        buffer.clear();

        // TODO: 21.06.2021
        // touch (filename) - создание файла
        // mkdir (dirname) - создание директории
        // cd (path | ~ | ..) - изменение текущего положения
        // rm (filename / dirname) - удаление файла / директории
        // copy (src) (target) - копирование файлов / директории
        // cat (filename) - вывод содержимого текстового файла
        // changenick (nickname) - изменение имени пользователя

        // добавить имя клиента

        if (key.isValid()) {
            String [] command = sb.toString()
                    .replace("\n", "")
                    .replace("\r", "")
                    .split(" ");
            if ("--help".equals(command[0])) {
                sendMessage(LS_COMMAND, selector, client);
                sendMessage(MKDIR_COMMAND, selector, client);
                sendMessage(TOUCH_COMMAND, selector, client);
                sendMessage(CD_COMMAND, selector, client);
                sendMessage(RM_COMMAND, selector, client);
                sendMessage(COPY_COMMAND, selector, client);
                sendMessage(CAT_COMMAND, selector, client);
                sendMessage(CHANGENICK_COMMAND, selector, client);
            } else if ("ls".equals(command[0])) {
                sendMessage(getFilesList().concat("\n"), selector, client);
            }
            if ("mkdir".equals(command[0])) {
                sendMessage(newDir(command[1]).concat("\n"), selector, client);
            }
            if ("rm".equals(command[0])) {
                sendMessage(deleteDirAndFiles(command[1]).concat("\n"), selector, client);
            }
            if ("copy".equals(command[0])) {
                sendMessage(copyDirAndFiles(command[1], command[2]).concat("\n"), selector, client);
            }
            if ("cat".equals(command[0])) {
                sendMessage(outPutTxtFile(command[1]).concat("\n"), selector, client);
            }
            if ("touch".equals(command[0])) {
                sendMessage(creatureNewFile(command[1]).concat("\n"), selector, client);
            }
        }
    }

    private void sendMessage(String message, Selector selector, SocketAddress client) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                if (((SocketChannel) key.channel()).getRemoteAddress().equals(client)) {
                    ((SocketChannel) key.channel()).write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
    }

    private String getFilesList() {
        String[] servers = new File("server").list();
        return String.join(" ", servers);
    }

    // touch (filename) - создание файла
    private String creatureNewFile(String newFile) throws IOException {
        String status = " ";
        // byte [] data = new byte[128];
        // Files.write(file, data, StandardOpenOption.APPEND);
        Path nowNewFile = Paths.get(newFile);
        if (!Files.exists(nowNewFile)) {
            Files.createFile(nowNewFile);
            status = "File " + newFile + " created";
        }

        return status;
    }

    // mkdir (dirname) - создание директории
    private String newDir (String dirname) {
        String status = " ";
        Path path = Paths.get(dirname);
        try {
            Path newDir = Files.createDirectory(path);
            status = "Directory " + dirname +  " was created";
        } catch(FileAlreadyExistsException e){
            status = "Such a directory " + dirname +  " already exists";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }
    // cd (path | ~ | ..) - изменение текущего положения
    private void changeDir () {


    }
    // rm (filename / dirname) - удаление файла / директории
    private String deleteDirAndFiles (String delete) {
        String status = " ";
        Path path = Paths.get(delete);
        try {
            status = delete + " successfully deleted";
            Files.delete(path);
        } catch (NoSuchFileException ex) {
            status = delete + " failed to delete";
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }
    // copy (src) (target) - копирование файлов / директории
    private String copyDirAndFiles (String fromCopy, String toCopy) {
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
        return status;
    }
    // cat (filename) - вывод содержимого текстового файла
    private String outPutTxtFile (String filename) {
        String status = " ";
        Path txtFile = Paths.get(filename);
        try {
         status = new String(Files.readAllBytes(txtFile));
        } catch (FileNotFoundException e) {
            status = "File " + filename + " not found";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }
    // changenick (nickname) - изменение имени пользователя
    private void changeNick () {}


    public static void main(String[] args) throws Exception {
        new NioTelnetServer();
    }
}