import java.util.HashMap;
// package javaChat;

public class Common {
    public static final int PORT = 6000;

    public static final String ADDR = "localhost";

    // public static final boolean debug_flag = true;
    public static final boolean debug_flag = false;

    public static final int FUN_USER_LOGIN = 1;
    public static final int FUN_USER_EXIT = 2;
    public static final int FUN_USER_LIST = 3;
    public static final int FUN_USER_CHAT = 4;

    // 功能选项
    // public enum ChatFunc {
    // FUN_USER_LOGIN, // 登录
    // FUN_USER_EXIT,

    // FUN_USER_LIST,

    // FUN_USER_CHAT,
    // };

    // 用户密码
    public static HashMap<String, String> UserPassArr = new HashMap<>() {
        {
            put("h1", "p1");
            put("h2", "p2");
            put("h3", "p3");
        }
    };

    // 可变参数版本的 debug 函数
    public static void debug(Object... args) {
        if (!debug_flag)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("DEBUG: ");
        for (Object arg : args) {
            sb.append(arg).append(", ");
        }
        // 删除最后一个逗号和空格
        if (args.length > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        System.out.println(sb.toString());
    }

    // 检查,是否数字
    public static boolean checkNumber(String str) {
        if (str.matches("\\d+")) {
            return true;
        } else {
            return false;
        }
    }

}
