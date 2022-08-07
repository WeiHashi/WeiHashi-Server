package cn.devmeteor.weihashi.data

enum class ResultCode(var code: Int, var msg: String) {
    ADD_SUCCESS(10001, "添加成功"),
    DELETE_SUCCESS(1002, "删除成功"),
    UPDATE_SUCCESS(10003, "修改成功"),
    GET_SUCCESS(10004, "获取成功"),
    OPERATION_SUCCESS(10005, "操作成功"),
    INIT_SUCCESS(10006,"初始化成功"),

    INVALID_REQUEST(20001, "非法请求"),
    INVALID_PARAM(20002,"参数值非法"),

    INVALID_DEVICE(30001,"该设备未经过授权，设备信息已提交审核，请联系开发者进行审核"),
    DEVICE_VERIFYING(30002,"该设备暂未通过审核，请联系开发者进行审核"),
    VERIFY_OK(30003,"授权成功"),

    NEED_LOGIN(40000,"需要重新登录"),
    LOGIN_OK(40001,"登录成功"),
    LOGIN_FAIL(40002,"用户名或密码错误"),
    ORIGIN_ERROR(40003,"原密码错误"),
    TOKEN_INVALID(40004,"token失效"),
    VERIFY_ERROR(40005,"验证码错误"),
    CONNECT_ERROR(40006,"教务系统连接失败"),

    MEMBER_EXIST(50001,"该用户已存在"),
    MEMBER_IN_OTHER_CLASS(50002,"该用户已加入其他班级"),
    CADRE_EXCEED_CEILING(50003,"班干部数量已达上限"),
    CLASS_NOT_EXISTS(50004,"班级不存在"),
    USER_NOT_EXISTS(50005,"用户不存在"),
    NO_MESSAGE(50006,"没有消息"),
    NO_SAVED_COLLECTION(50007,"未填写过该问卷"),
    CONTENT_NOT_EXIST(50008,"内容不存在"),

    TOP_EXCEED_CEILING(60001,"置顶数量已达上限"),
    TOP_CANCELED(60002,"置顶数量已达上限，已取消置顶发布"),

    INIT_FAILED(70001,"初始化失败"),
    GET_CODE_FAILED(70002,"获取验证码失败"),
    GET_FAILED(70003,"获取失败"),
    NO_LESSON(70004,"未获取到本学期课程"),
    NO_GRADE(70005,"没有成绩数据"),
    RESOLVE_ERROR(70006,"数据解析异常")

}