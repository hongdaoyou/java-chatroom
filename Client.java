
// package javaChat;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;

// class ShareDataItem {
//     Socket sock; // 存储,套接字
//     Thread th; // 线程id
//     int successFlag = 0; // 是否登录成功
//     String name; // 用户的名字
// }

// 共享数据, 相当于 struct
class ClientShareData {
    public Socket ss;
    public Thread th1;
    public Thread th2;

    public int connectFlag = 0;
    public int successFlag = 0;

    // 存储,客户端的信息
    // Map<Socket, ShareDataItem> userArr = new HashMap<>();
}

public class Client {
    public ClientShareData shareData;

    public static void main(String[] args) {
        System.out.println("main");

        Client client = new Client();

        client.start_service();

        // System.out.print(2);
    }

    public void start_service() {
        Common.debug("start_service");

        this.shareData = new ClientShareData();

        // 建立,连接
        create_connect();

        Common.debug("建立连接了吗", shareData.connectFlag);

        // 连接成功
        if (shareData.connectFlag == 1) {
            // 启动,ui线程
            shareData.th2 = new Thread(new ClientUI(shareData));
            shareData.th2.start();

            try {

                shareData.th1.join();
                shareData.th2.join();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    // 创建,socket 连接
    void create_connect() {
        Common.debug("create_connect");

        try {
            // 直接建立连接
            shareData.ss = new Socket(Common.ADDR, Common.PORT);

            // OutputStream output = shareData.ss.getOutputStream();
            // output.write(("3333\n").getBytes());
            // output.flush();

            // 连接成功
            shareData.connectFlag = 1;

            // 建立成功后, 读取数据
            shareData.th1 = new Thread(new ClientSocket(shareData));

            // 启动线程
            shareData.th1.start();

        } catch (Exception e) {
            Common.debug("建立连接失败");
            System.exit(1);

            e.printStackTrace();
        }

    } // end create_connect

}

// 客户端,连接
class ClientSocket implements Runnable {
    private ClientShareData shareData;
    private Socket ss;

    // 建立,连接
    public ClientSocket(ClientShareData shareData) {
        Common.debug("ClientSocket 初始化");

        this.shareData = shareData;
        ss = shareData.ss;
    }

    @Override
    public void run() {
        Common.debug("ClientSocket run");

        int serverExitFlag = 0;
        while (true) {
            Common.debug("我正在等待,读取,服务器的信息");
            try {

                // 读取,套接字中的数据
                InputStream input = ss.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String str = reader.readLine();

                Common.debug("收到服务器的消息: ", str);
                if (str == null) {
                    serverExitFlag = 1;
                    break;
                }

                handle_receive_info(str);

            } catch (Exception e) {
                Common.debug("读取异常");
                e.printStackTrace();
                serverExitFlag = 1;
                break;
            }
            Common.debug("我正在结束了,一次读取 ", serverExitFlag);
        } // end of while

        if (serverExitFlag == 1) {
            Common.debug("服务器,退出了. 我也退出了");
            System.exit(1);
        }

    } // end of

    // 处理,接收到的信息
    void handle_receive_info(String str) {
        try {
            JSONObject obj = new JSONObject(str);
            int type = obj.getInt("type");
            String msg = obj.getString("msg");

            switch (type) {
                case Common.FUN_USER_LOGIN:
                    // 是否,登录成功
                    shareData.successFlag = obj.getInt("successFlag");
                    System.out.println(msg);

                    break;

                case Common.FUN_USER_LIST: // 用户列表
                    System.out.println(msg);

                    break;

                case Common.FUN_USER_CHAT: // 聊天信息

                    System.out.println(msg);

                    break;
            }

        } catch (Exception e) {

        }

    }

}

// 用户的ui界面
class ClientUI implements Runnable {
    private ClientShareData shareData;
    private Socket ss;
    private Scanner scanner;

    // 从套接字中,读取
    private BufferedReader reader;

    public ClientUI(ClientShareData shareData) {
        Common.debug("ClientUI 初始化");

        // 初始化,数据
        this.shareData = shareData;
        ss = shareData.ss;

        // 初始化,终端
        scanner = new Scanner(System.in);

        try {
            // 读流
            InputStream inputStream = ss.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));

        } catch (Exception e) {

        }

    }

    @Override
    public void run() {
        Common.debug("ClientUI run");

        while (true) {
            System.out.println(" chat-room\n");
            System.out.println(" 1) 登录");
            System.out.println(" 2) 退出\n");

            System.out.println("请输入,编号");

            String str = scanner.nextLine().trim();

            if (!Common.checkNumber(str)) {
                System.out.println("请输入数字");
                continue;
            }
            int ch = Integer.parseInt(str);

            switch (ch) {
                case 1: // 登录
                    login_ui();

                    break;
                case 2: // 退出
                    login_exit();
                    System.exit(1);

                    break;
                default:
                    System.out.println("输入错误");
                    break;
            }
            Common.debug("读取终端:", str);

            // 暂停
            scanner.nextLine();
            // 登录页面, 并且,登录成功. 进入功能页面
            if (ch == 1 && shareData.successFlag == 1) {
                function_ui();
            }
        }
    } // end of run

    void function_ui() {
        while (true) {
            System.out.println("        chat-room\n");
            System.out.println(" 1) 获取,用户列表");
            System.out.println(" 2) 聊天");
            System.out.println(" 3) 退出\n");

            System.out.println("请输入,编号");

            String str = scanner.nextLine().trim();

            if (!Common.checkNumber(str)) {
                System.out.println("请输入数字");
                continue;
            }
            int ch = Integer.parseInt(str);

            switch (ch) {
                case 1: // 获取用户列表
                    get_user_list();

                    break;
                case 2: // 聊天
                    chat_ui();

                    break;

                case 3: // 退出
                    login_exit();

                    System.exit(1);
                    break;
                default:
                    System.out.println("输入错误");

                    break;
            } // end of switch

            // 获取一行
            scanner.nextLine();
        } // end of while
    }

    // 获取用户列表
    void get_user_list() {
        Common.debug("get_user_list");

        JSONObject obj = new JSONObject();

        try {
            obj.put("type", Common.FUN_USER_LIST);

        } catch (Exception e) {
            e.printStackTrace();
        }

        sendMsg(obj);
    }

    // 聊天页面
    void chat_ui() {
        Common.debug("get_user_list");

        System.out.println("请输入,聊天对象");
        String receiver = scanner.nextLine().trim();

        System.out.println("请输入,信息");
        String msg = scanner.nextLine().trim();

        try {
            JSONObject obj = new JSONObject();

            obj.put("type", Common.FUN_USER_CHAT);
            obj.put("receiver", receiver);
            obj.put("msg", msg);

            sendMsg(obj);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 退出
    void login_exit() {
        Common.debug("login_exit");

        JSONObject obj = new JSONObject();

        try {
            obj.put("type", Common.FUN_USER_EXIT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        sendMsg(obj);
    }

    // 登录的界面
    void login_ui() {
        Common.debug("login_ui");

        System.out.println("        chat room\n");
        System.out.println("请输入,你的用户名");

        String name = scanner.nextLine().trim();

        System.out.println("请输入,你的密码");

        String passwd = scanner.nextLine().trim();

        JSONObject obj = new JSONObject();

        try {
            obj.put("type", Common.FUN_USER_LOGIN);

            obj.put("name", name);

            obj.put("passwd", passwd);

        } catch (Exception e) {
            e.printStackTrace();
        }

        sendMsg(obj);

    } // end of login_ui

    // 发送
    void sendMsg(JSONObject obj) {
        Common.debug("sendMsg");

        try {
            // 写流
            OutputStream output = ss.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
            // OutputStreamWriter writer = new OutputStreamWriter(output);

            Common.debug(obj.toString());
            // writer.write("11111\n");

            // writer.write(obj.toString());
            writer.write(obj.toString() + "\n");
            writer.flush();

        } catch (Exception e) {
            Common.debug("发送异常");
            e.printStackTrace();
        }
    }

}
