package io.rong.imlib;

/**
 * SDK 未进行初始化的异常。
 *
 * 未进行初始化通常是因为没有调用 {@link RongIMClient} 的 init 方法并传入正确的参数。
 *
 * @see RongIMClient
 */
public class NotInitializedException extends Exception {

	private static final long serialVersionUID = -556740511478414350L;

}
