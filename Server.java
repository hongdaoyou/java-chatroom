// package main;
// package javaChat;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;

class ShareDataItem {
    public Socket sock; // 存储,套接字
    public Thread th; // 线程id

    public int successFlag = 0; // 是否登录成功
    public boolean exitFlag = false; // 是否退出

    public String name; // 用户的名字
}

// 共享数据, 相当于 struct
class ShareData {
    public ServerSocket ss;

    // 存储,客户端的信息
    public Map<Socket, ShareDataItem> userArr = new HashMap<>();
}

public class Server {
    // private int PORT= 8888;
    ShareData shareData = new ShareData();

    public static void main(String[] args) {

        Server server = new Server();
        server.start_service();
        // System.out.println(1);
    }

    // 创建服务器, 监听,用户的连接
    void start_service() {
        Common.debug("create_server");

        Thread th1;
        Socket cs;
        ShareDataItem dataItem;
        try {
            // 创建服务器
            shareData.ss = new ServerSocket(Common.PORT);

            while (true) {
                Common.debug("accept before");
                // 等待客户端的到来
                cs = shareData.ss.accept();
                Common.debug("one client come");

                th1 = new Thread(new HandleClient(cs, shareData));

                // 创建,该线程的存储信息
                dataItem = new ShareDataItem();
                dataItem.sock = cs;
                dataItem.th = th1;

                // 将其,保存起来
                shareData.userArr.put(cs, dataItem);

                // 启动线程
                th1.start();

            } // end of while

        } catch (Exception e) {
            Common.debug("出现异常了");
            e.printStackTrace();

            // 终止程序
            System.exit(1);
        }

    } // end of start_service
}

// 读取,用户输入的数据,处理相应的业务
class HandleClient implements Runnable {
    public ShareData shareData;
    public Socket sc;

    public HandleClient(Socket sc, ShareData shareData) {
        // 初始化,共享数据
        this.shareData = shareData;
        this.sc = sc;
    }

    @Override
    public void run() {
        Common.debug("run 线程运行了");

        // 当前 线程的套接字的存储信息
        ShareDataItem userInfo = shareData.userArr.get(sc);

        Common.debug("服务器,退出了吗", userInfo.exitFlag);

        String str;
        // 是否退出
        while (!userInfo.exitFlag) {
            try {
                InputStream input = sc.getInputStream();
                // BufferedReader reader = new BufferedReader(input);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                // InputStreamReader reader = new InputStreamReader(input);

                Common.debug("线程,正在等待客户端,发送的消息");

                // // // 读取一行
                // str = reader.readLine();
                // Common.debug("recv: ", str);

                // 读取 JSON 数据
                // StringBuilder sb = new StringBuilder();
                // String line;
                // while ((line = reader.readLine()) != null) {
                // Common.debug("recv:A- ", line);
                // sb.append(line);
                // }

                // StringBuilder sb = new StringBuilder();
                // int bufferSize = 1024; // 指定缓冲区大小为 1024 字符，可以根据需要调整
                // char[] buffer = new char[bufferSize];
                // int bytesRead;

                // while ((bytesRead = reader.read(buffer, 0, bufferSize)) != -1) {
                // sb.append(buffer, 0, bytesRead);
                // // 如果你要处理读取的内容可以在这里进行操作
                // }

                str = reader.readLine();
                Common.debug("thread recv: ", str);

                if (str == null) {
                    Common.debug("对方,自动断开,连接");
                    userInfo.exitFlag = true;
                    handle_exit(sc); // 处理退出

                    continue;
                    // 处理线程退出的业务
                }

                // 解析数据
                JSONObject obj = new JSONObject(str);
                int type = obj.getInt("type");

                switch (type) {
                    case Common.FUN_USER_LOGIN:
                        handle_login(sc, obj); // 处理登录

                        break;

                    case Common.FUN_USER_EXIT:
                        // handle_exit(sc, obj); // 处理退出
                        userInfo.exitFlag = true;

                        break;

                    case Common.FUN_USER_LIST:
                        handle_list(sc, obj); // 获取,用户列表

                        break;

                    case Common.FUN_USER_CHAT:
                        handle_chat(sc, obj);

                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                // Common.debug("对方,自动断开,连接");

                userInfo.exitFlag = true;
                // 处理线程退出的业务
                // e.printStackTrace();
            }
            if (userInfo.exitFlag) {
                handle_exit(sc); // 处理退出
            }

        } // end of while
          // 线程退出后,要回收
        Common.debug("一个线程,结束");

    } // end of run

    // 处理,聊天
    void handle_chat(Socket sc, JSONObject obj) {
        Common.debug("handle_chat");

        String msg = "";
        int userExistFlag = 0;
        // 接收者
        ShareDataItem receiverItem = null;

        try {
            String receiver = obj.getString("receiver");

            for (ShareDataItem item : shareData.userArr.values()) {
                if (receiver.equals(item.name)) { // 存在,该用户
                    userExistFlag = 1;
                    receiverItem = item;
                    break;
                }
            }

            if (userExistFlag == 0) {
                msg = "用户,不存在";
            } else {
                // 存在,该用户
                JSONObject sendData2 = new JSONObject();

                String msg2 = "来自" + shareData.userArr.get(sc).name + "的消息: ";
                msg2 += obj.getString("msg");

                Common.debug("send data2: ", msg2);

                sendData2.put("type", obj.getInt("type"));
                sendData2.put("msg", msg2);

                msg = "用户存在, 已经发送";
                sendMsg(receiverItem.sock, sendData2);
            }

            Common.debug("send data: ", msg);
            // 将数据,进行封装
            JSONObject sendData = new JSONObject();
            sendData.put("type", obj.getInt("type"));

            sendData.put("msg", msg);

            sendMsg(sc, sendData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 处理退出
    void handle_exit(Socket sc) {
        // 怎么,可以在自己的线程内,等自己呢 --> 只能,有一个线程,去轮询. 然后,查看,回收的线程
        // try {
        // Common.debug("I join");
        // // 回收线程资源,再删除
        // shareData.userArr.get(sc).th.join();
        // Common.debug("I join ");

        // } catch (Exception e) {
        // Common.debug("回收线程资源, 失败");
        // e.printStackTrace();
        // }
        // 删除,当前用户的信息
        shareData.userArr.remove(sc);

        // 至于,将这个标记为置掉, 没必要了. 因为,自动退出了
    }

    // 处理,用户列表
    void handle_list(Socket sc, JSONObject obj) {
        Common.debug("handle_list");

        String msg = "用户列表: ";
        int successFlag = 0;

        try {
            for (ShareDataItem item : shareData.userArr.values()) {
                msg = msg + item.name + " ";
            }

            Common.debug("send data: ", msg);

            // 将数据,进行封装
            JSONObject sendData = new JSONObject();
            sendData.put("type", obj.getInt("type"));

            sendData.put("msg", msg);

            sendMsg(sc, sendData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 处理登录
    void handle_login(Socket sc, JSONObject obj) {
        String msg;
        int successFlag = 0;

        try {
            // 获取,用户名
            String name = obj.getString("name");
            String passwd = obj.getString("passwd");

            Common.debug("登录收到: ", name, passwd);

            if (!Common.UserPassArr.containsKey(name)) {
                msg = "用户不存在";
            } else {
                String passwd2 = Common.UserPassArr.get(name);

                if (passwd.equals(passwd2)) {
                    msg = "登录成功";
                    successFlag = 1;

                } else {
                    msg = "密码错误";
                }
            }
            if (successFlag == 1) {
                shareData.userArr.get(sc).successFlag = successFlag;
                shareData.userArr.get(sc).name = name;

            }

            Common.debug("send data: ", msg, successFlag);

            // 将数据,进行封装
            JSONObject sendData = new JSONObject();
            sendData.put("type", obj.getInt("type"));

            sendData.put("msg", msg);
            sendData.put("successFlag", successFlag);

            sendMsg(sc, sendData);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 发送消息
    void sendMsg(Socket sc, JSONObject obj) {
        Common.debug("sendMsg");

        try {
            // 写流
            OutputStream outputStream = sc.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            writer.write(obj.toString() + "\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
