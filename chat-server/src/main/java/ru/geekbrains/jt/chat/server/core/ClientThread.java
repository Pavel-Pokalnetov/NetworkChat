package ru.geekbrains.jt.chat.server.core;

import ru.geekbrains.jt.chat.common.Messages;
import ru.geekbrains.jt.network.SocketThread;
import ru.geekbrains.jt.network.SocketThreadListener;

import java.io.IOException;
import java.net.Socket;

public class ClientThread extends SocketThread {
    private String nickname;
    private boolean isAuthorized;

    private long clientConnectionTime;
    private boolean isReconnecting;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
            Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(120000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!isAuthorized) close();
                }
        };
        Thread t = new Thread(runnable);
        t.start();
    }

    public boolean isReconnecting() {
        return isReconnecting;
    }

    public String getNickname() {
        return nickname;
    }

    public synchronized boolean isAuthorized() {
        return isAuthorized;
    }

    void reconnect() {
        isReconnecting = true;
        close();
    }

    synchronized void authAccept(String nickname) {
        isAuthorized = true;
        this.nickname = nickname;
        sendMessage(Messages.getAuthAccept(nickname));
    }

    void authFail() {
        sendMessage(Messages.getAuthDenied());
        //close();
    }

    void msgFormatError(String msg) {
        sendMessage(Messages.getMsgFormatError(msg));
        //close();
    }
}
