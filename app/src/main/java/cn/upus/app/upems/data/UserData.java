package cn.upus.app.upems.data;

/**
 * Created by computer on 2016-09-06.
 */
public class UserData {

    public static final String USER_NAME = "USER_NAME";//用户帐号
    public static final String PASS_WORD = "PASS_WORD";//用户密码
    public static final String PASS_WORD_EYPT = "PASS_WORD_EYPT";//用户密码加密后
    public static final String NAME = "NAME";//用户姓名
    public static final String USER_LEVEL = "USER_LEVEL";//用户等级
    public static final String COMPANY = "COMPANY";//公司名称
    public static final String IS_LOGIN = "IS_LOGIN";//是否已经登录
    public static final String RECORD = "RECORD";//上次是否登录过
    public static final String REMEMBER_PASSWORD = "REMEMBER_PASSWORD";//是否记住密码

    public static final String BRANNO = "BRANNO";//部门编号
    public static final String MANNO = "MANNO";//工号

    public static final String ROLCODE = "ROLCODE";//司机编码
    public static final String CODE_NAME = "CODE_NAME";//系统类型 如物流系统 lis
    public static final String ROLENO = "ROLENO";//岗位类型

    public static final String CUSTNO = "CUSTNO";//设备注册编号 2018-09-04新增 快递员注册需要该参数

    public static final String LOCAL_IP = "LOCAL_IP";//WEB SERVICE 本地IP
    public static final String LOCAL_PORT = "LOCAL_PORT";//WEB SERVICE 本地端口
    public static final String LOCAL_WSDL = "LOCAL_WSDL";//WEB SERVICE 自定义地址
    public static final String LOCAL_COMPANY = "LOCAL_COMPANY";//WEB SERVICE 本地公司名称

    public static final String LOCAL_SERVICE = "LOCAL_SERVICE";//是否本地服务

    public static final String COMPNO = "COMPNO";//服务商编号
    public static final String WSDL = "WSDL";//获取到的服务器地址

    public static final String USER_ICON = "USER_ICON";//用户头像

    public static final String WEB_URL = "WEB_URL";//获取到的java服务器地址

    public static final String ENCLOSURE_URL = "ENCLOSURE_URL";//获取到的附件地址

    public static final String LOGIN_RETURN = "LOGIN_RETURN";//登录成功后返回（提示登录成功）

    public static final String COMPANY_LIST = "COMPANY_LIST";//登录公司列表 最多5条 如：USER_NAME_LIST_1 - USER_NAME_LIST_5
    public static final String USER_NAME_LIST = "USER_NAME_LIST";//登录用户记录列表 最多5条 如：USER_NAME_LIST_1 - USER_NAME_LIST_5

    public static final String PASS_WORD_LIST = "PASS_WORD_LIST";//登录密码记录列表

    public static final String DATA_TIME = "DATA_TIME";//录音的开始时间

    public static final String RECEIVE_RELEASE = "RECEIVE_RELEASE";//接收任务 或者OR 发布任务

    public static final String TASK_MANAGEMENT = "TASK_MANAGEMENT";//任务管理（自己发布的任务 0"需处理", 1"超时未处理", 2"已完成", 3"制作中"）（接收的任务 10"需处理", 11"超时未处理", 12"已完成"）

    public static final String TASK_LEFT_A = "TASK_LEFT_A";
    public static final String TASK_LEFT_B = "TASK_LEFT_B";
    public static final String TASK_LEFT_C = "TASK_LEFT_C";
    public static final String TASK_RIGHT_A = "TASK_RIGHT_A";
    public static final String TASK_RIGHT_B = "TASK_RIGHT_B";
    public static final String TASK_RIGHT_C = "TASK_RIGHT_C";
    public static final String TASK_RIGHT_D = "TASK_RIGHT_D";

    public static final String BAR_CODE_IP = "BAR_CODE_IP";//条码扫描SOCKET IP地址
    public static final String BAR_CODE_PORT = "BAR_CODE_PORT";//条码扫描SOCKET 端口号

    public static final String BEG_DATE = "BEG_DATE";//库存查询 开始日期
    public static final String END_DATE = "END_DATE";//库存查询 截止日期


    public static final String DEPOTNO_OUT = "depotno_out";//出库类型
    public static final String DEPOTNA_OUT = "depotna_out";//入库类型

    public static final String DEPOTNO_IN = "depotno_in";//出库类型
    public static final String DEPOTNA_IN = "depotna_in";//入库类型

    public static final String TYPENA_OUT = "typena_out";//出库仓库
    public static final String TYPENO_OUT = "typeno_out";//出库仓库

    public static final String TYPENA_IN = "typena_in";//入库仓库
    public static final String TYPENO_IN = "typeno_in";//入库仓库


    public static final String TYPENA_NUMBER_OUT = "TYPENA_NUMBER_OUT";//出库 供应商编号
    public static final String TYPENA_NAME_OUT = "TYPENA_NAME_OUT";//出库 供应商名称

    public static final String TYPENA_NUMBER_IN = "TYPENA_NUMBER_IN";//入库 部门编号
    public static final String TYPENA_NAME_IN = "TYPENA_NAME_IN";//入库 部门名称

    public static final String Latitude = "Latitude";//纬度
    public static final String Longitude = "Longitude";//经度

    public static final String CHAT_SERVICE_MESSAGE = "CHAT_SERVICE_MESSAGE";//聊天服务接收到的消息
    public static final String CHAT_VIEW_OPEN = "CHAT_VIEW_OPEN";//聊天界面是否打开
    public static final String CHAT_USER = "CHAT_USER";//当前聊天对象

    public static final String PRINT_HEIGHT = "PRINT_HEIGHT";//打印机高度

    public static final String DEVNO = "devno";//设备编号
    public static final String DEVID = "devid";//设备ID
    public static final String DEVCATE = "devcate";//设备类型

    public static final String DEVKIND = "devkind";//设备类型
    public static final String PROTKIND = "protkind";//锁板协议 20银龙 其它默认
    public static final String LOCKIND = "lockind";//0 开门通 1 关门通
    public static final String OPENKIND= "openkind";//0 USB / 1 串口 协议

    public static final String CARDNO = "cardno";//刷卡登录的加密卡号

    public static final String DIALOG_SHOW = "DIALOG_SHOW";//dialog弹出

    public static final String START = "START";//是否 检测当前程序是否主界面

    public static final String DEPSHOT = "depshot";//存件是否拍照
    public static final String TAKESHOT = "takeshot";//取件是否拍照

    public static final String fixplusEntitiesAD1 = "fixplusEntitiesAD1";//半屏广告
    public static final String fixplusEntitiesAD2 = "fixplusEntitiesAD2";//全屏广告

    public static final String isHorizontal = "isHorizontal";// 横屏 true / 竖排 false

    public static final String TTS_OPEN = "tts_open";//是否开启语音提示
}
