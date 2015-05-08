package io.rong.imlib;

/**
 * 未找到注解的异常。
 *
 * 在 SDK 中，以及开发者自己扩展的代码中，所有的消息类都需要添加 {@link MessageTag} 注解。
 *
 * @see io.rong.imlib.MessageTag
 */
public class AnnotationNotFoundException extends Exception {

	private static final long serialVersionUID = -6400740332532765705L;

}
