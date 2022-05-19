package ru.geekbrains.jt.chat.client;

import ru.geekbrains.jt.chat.common.Messages;
import ru.geekbrains.jt.network.SocketThread;
import ru.geekbrains.jt.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Client extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private static final String TITLE = "Chat Client";
    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 4));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfNickname = new JTextField("RedX");
    private final JTextField tfLogin = new JTextField("RedX");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnRegister = new JButton("Register");
    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("<html><b>Send</b></html>");
    private final JCheckBox cbMsgForAllUsers = new JCheckBox("For all");
    private final JList<String> userList = new JList<>();
    private boolean shownIoErrors = false;
    private SocketThread socketThread;
    private String nickName;
    private boolean isRegistration = false;
    private boolean isLogin = false;

    private Client() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //посреди экрана
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane spLog = new JScrollPane(log);
        JScrollPane spUsers = new JScrollPane(userList);
        spUsers.setPreferredSize(new Dimension(100, 0));

        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        btnLogin.addActionListener(this);
        btnRegister.addActionListener(this);
        btnDisconnect.addActionListener(this);
        panelBottom.setVisible(false);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfNickname);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelTop.add(btnRegister);

        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);
        panelBottom.add(cbMsgForAllUsers, BorderLayout.NORTH);

        add(panelBottom, BorderLayout.SOUTH);
        add(panelTop, BorderLayout.NORTH);
        add(spLog, BorderLayout.CENTER);
        add(spUsers, BorderLayout.EAST);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }

    void handleMessage(String value) {
        String[] arr = value.split(Messages.DELIMITER);
        String msgType = arr[0];
//        putLog(msgType);
        switch (msgType) {
            case Messages.SETNICK:
                setTitle(TITLE + " logged in as: " + arr[2]);
                break;
            case Messages.REGISTRATION_OK:
                panelBottom.setVisible(true);
                panelTop.setVisible(false);
                isRegistration = false;
                break;
            case Messages.AUTH_ACCEPT:
                setTitle(TITLE + " logged in as: " + arr[1]);
                isLogin = true;
                putLog("Для смены ника/пароля отправьте сообщение\nSETNICK:новое_имя\nSETPSWD:новый_пароль");
                nickName = arr[1];
                break;
            case Messages.AUTH_DENY:
                putLog("Не верный логин или пароль.");
                isLogin = false;
                break;
            case Messages.MSG_FORMAT_ERROR:
                putLog(value.split("/msg_error§")[1]);
                //socketThread.close();
                break;
            case Messages.USER_LIST:
                String users = value.substring(Messages.DELIMITER.length() +
                        Messages.USER_LIST.length());
                String[] usersArr = users.split(Messages.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            case Messages.MSG_BROADCAST:
            case Messages.UNICAST:
                log.append(DATE_FORMAT.format(Long.parseLong(arr[1])) + ": " + arr[2] + ": " + arr[3] + "\n");
                log.setCaretPosition(log.getDocument().getLength());
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msgType);
        }
    }

    private void sendMessage() {
        if (!isLogin) return;
        String _message;
        String msg = tfMessage.getText();
        if ("".equals(msg)) return;
        if ("SETNICK".equals(msg.split(":")[0])) {
            //изменение ника
            if (msg.split(":")[1].isBlank()) {
                putLog("Ник не может быть пустым");
                return;
            }
            setUserNick(msg.split(":")[1]);
            return;
        }
        if ("SETPSWD".equals(msg.split(":")[0])) {
            //изменение пароля
            if (msg.split(":")[1].isBlank()) {
                putLog("Пароль не может быть пустым");
                return;
            }
            setUserPassword(msg.split(":")[1]);
            return;
        }
        if (cbMsgForAllUsers.isSelected()) {
            //шлём BROADCAST
            _message = Messages.getTypeBcastFromClient(msg);
            putLog(nickName + " > Всем: " + msg);
        } else {
            //шлём UNICAST
            String username = userList.getSelectedValue();
            if (nickName.equals(username)) return;
            _message = Messages.getMsgUnicast(username, msg);
            putLog(nickName + " > " + username + ": " + msg);
        }
       // System.out.println(_message);
        socketThread.sendMessage(_message);
        tfMessage.setText(null);
        tfMessage.grabFocus();
    }

    private void setUserNick(String s) {
        socketThread.sendMessage(Messages.getMsgSetUserNickname(tfLogin.getText(), s));
    }

    private void setUserPassword(String s) {
        socketThread.sendMessage(Messages.getMsgSetUserPassword(tfLogin.getText(), s));
    }

    private void wrtMsgToLogFile(String msg, String username) {
        try (FileWriter out = new FileWriter("log.txt", true)) {
            out.write(username + ": " + msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }

    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void showException(Thread t, Throwable e) {
        if (!shownIoErrors) return;
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = String.format("Exception in \"%s\" %s: %s\n\tat %s",
                    t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
            JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    private void connect() {
        try {
            Socket socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else if (src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
        } else if (src == btnRegister) {
            isRegistration = true;
            connect();
        } else {
            throw new RuntimeException("Action for component unimplemented");
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        //showException(t, e);
    }

    @Override
    public void onSocketStart(SocketThread t, Socket s) {
        //  putLog("Start");
    }

    @Override
    public void onSocketStop(SocketThread t) {
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
        isLogin = false;
        setTitle(TITLE);
        userList.setListData(new String[0]);
    }

    @Override
    public void onSocketReady(SocketThread t, Socket socket) {
        if (isRegistration) {
            isRegistration = false;
            //регистрируемся новым логин паролем
            String login = tfLogin.getText();
            String nickname = tfNickname.getText();
            if (nickname.isEmpty()) {
                putLog("Укажите никнейм");
                return;
            }
            String pass = new String(tfPassword.getPassword());
            if (pass.isEmpty() || pass.isBlank()) {
                putLog("пароль не может быть пустым");
                return;
            }
            t.sendMessage((Messages.getRegistrationMessage(nickname, login, pass)));
            return;
        }
        //здесь просто логин
        panelBottom.setVisible(true);
        panelTop.setVisible(false);
        String login = tfLogin.getText();
        String pass = new String(tfPassword.getPassword());
        t.sendMessage(Messages.getAuthRequest(login, pass));
    }

    @Override
    public void onReceiveString(SocketThread t, Socket s, String msg) {
        handleMessage(msg);
    }

    @Override
    public void onSocketException(SocketThread t, Throwable e) {
        showException(t, e);
    }
}