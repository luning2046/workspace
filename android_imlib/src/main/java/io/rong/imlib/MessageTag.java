package io.rong.imlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消息的注解，以声明消息的标识、是否记入未读消息数和是否存储为消息历史记录。
 *
 * @see io.rong.imlib.RongIMClient.Message
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageTag {
    /**
     * 空值，不表示任何意义。
     */
	public final static int NONE = 0x0;
	/**
	 * 消息需要被存储到消息历史记录。
	 */
	public final static int ISPERSISTED = 0x1;
	/**
	 * 消息需要被记入未读消息数。
	 */
	public final static int ISCOUNTED = 0x2;

    /**
     * 消息对象名称。
     *
     * 请不要以 "RC:" 开头， "RC:" 为官方保留前缀。
     *
     * @return 消息对象名称的返回值。
     */
	String value();

    /**
     * 消息的标识。
     *
     * 传入的值可以为 MessageTag.NONE、MessageTag.ISPERSISTED 或 MessageTag.ISCOUNTED。
     *
     * @return 标识的返回值。
     */
	int flag() default NONE;
}
