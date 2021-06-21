package ru.grom.cloudstorage.lesson1;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            System.out.printf("Client %s connected\n", socket.getInetAddress());
            while (true) {
                String command = in.readUTF();
                if ("upload".equals(command)) {
                    String fileName = in.readUTF();
                    try {
                        File file = new File("server"  + File.separator + fileName);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(file);

                        long size = in.readLong();

                        byte[] buffer = new byte[8 * 1024];

                        for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
                            int read = in.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                        fos.close();
                        out.writeUTF("file "  + fileName + " uploaded");
                    } catch (Exception e) {
                        out.writeUTF("!file " + fileName +" not uploaded!");
                    }
                }
                if ("download".equals(command)) {
                    String fileName = in.readUTF();
                    try {
                        File file = new File("server" + File.separator + fileName);
                        if (!file.exists()) {
                            throw new FileNotFoundException();
                        }
                        long fileLength = file.length();
                        out.writeLong(fileLength);

                        FileInputStream fis = new FileInputStream(file);


                        int read = 0;
                        byte[] buffer = new byte[8 * 1024];
                        while ((read = fis.read(buffer)) > 0) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                        out.writeUTF("file " + fileName + " downloaded");

                    } catch (FileNotFoundException e) {
                        out.writeUTF("!file " + fileName + " not found!");
                    } catch (Exception e) {
                        out.writeUTF("!file " + fileName + " not downloaded!");
                    }
                }
                if ("exit".equals(command)) {
                    System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
                    break;
                }

                System.out.println(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
