package ru.grom.cloudstorage.lesson3.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

public class ClientCloud extends JFrame {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public ClientCloud() throws IOException {
        socket = new Socket("localhost", 5000);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        setSize(800, 600);
        JPanel panel = new JPanel(new GridLayout(3, 1));

        JButton btnSend = new JButton("Send");
        JTextField textField = new JTextField();
        JTextArea textArea = new JTextArea();


        btnSend.addActionListener(a -> {
            String [] cmd = textField.getText()
                    .replace("\u0000", "")
                    .replace("\u0001", "")
                    .replace("\u0002", "")
                    .replace("\u0003", "")
                    .replace("\u0004", "")
                    .replace("\u0005", "")
                    .replace("\u0006", "")
                    .replace("\u0007", "")
                    .replace("\u0008", "")
                    .replace("\u0009", "")
                    .replace("\u0010", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .split(" ");
            sendMessage(cmd[0]);
//            if ("upload".equals(cmd[0])) {
//                sendFile(cmd[1]);
//            }
//            else if ("download".equals(cmd[0])) {
//                getFile(cmd[1]);
//            }
            if ("mkdir".equals(cmd[0])) {
                createDir(cmd[1]);
            }
        });

        panel.add(textArea);
        panel.add(textField);
        panel.add(btnSend);

        add(panel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                sendMessage("exit");
            }
        });
        setVisible(true);
    }


    private void createDir (String dirname) {
        sendMessage("mkdir " + dirname);
//        String status;
//        try {
//            status = in.readUTF();
//            System.out.println(status);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readMessage () {
        String message = " ";
        try {
            message = in.readUTF();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }


    public static void main(String[] args) throws IOException {
        new ru.grom.cloudstorage.lesson3.client.ClientCloud();
    }
}