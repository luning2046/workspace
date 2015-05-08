package io.rong.imkit.veiw.gif;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 本类可以显示一个gif动画，其使用方法和android的其它view（如imageview)一样。<br>
 * 如果要显示的gif太大，会出现OOM的问题,因为不能申请到更多的内存. 出现异常后的帧无法解析,但保证了前面的帧是正常的.
 * 
 * @版权所有 北京新媒传信科技有限公司
 * @作者 原树旗
 * 
 */
public class GifView extends ImageView implements GifAction {

	/** 图片解码器 */
	private GifDecoder mGifDecoder = null;
	/** 当前要画的帧的图 */
	private Bitmap mCurrentImage = null;

	private int mResId = -1;

	private int mShowWidth = -1;
	private Rect mRect = null;

	private final String fNameSpace = "http://schemas.android.com/apk/res/android";

	private GifImageType mAnimationType = GifImageType.SYNC_DECODER;
	private int mStatus = 0;

	/**
	 * 解码过程中，动画显示的方式<br>
	 * 如果图片较大，那么解码过程会比较长，这个解码过程中，gif如何显示
	 * 
	 * @author 原树旗
	 * 
	 */
	public enum GifImageType {
		/**
		 * 在解码过程中，不显示图片，直到解码全部成功后，再显示
		 */
		WAIT_FINISH(0),
		/**
		 * 和解码过程同步，解码进行到哪里，图片显示到哪里
		 */
		SYNC_DECODER(1),
		/**
		 * 在解码过程中，只显示第一帧图片
		 */
		COVER(2);

		GifImageType(int i) {
			nativeInt = i;
		}

		final int nativeInt;
	}

	public GifView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GifView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		String value = attrs.getAttributeValue(fNameSpace, "src");
		if (value == null || value.length() < 1) {
			return;
		}
		value = value.substring(1);
		mResId = Integer.parseInt(value);
		setGifImage(mResId);
	}

	/**
	 * 设置图片，并开始解码
	 * 
	 * @param gif
	 * 要设置的图片
	 */
	private void setGifDecoderImage(byte[] gif) {
		freeDecoder(mGifDecoder);
		mGifDecoder = new GifDecoder(gif, this);
		mGifDecoder.start();
	}

	/**
	 * 设置图片，开始解码
	 * 
	 * @param is
	 * 要设置的图片
	 */
	private void setGifDecoderImage(InputStream is) {
		freeDecoder(mGifDecoder);
		mGifDecoder = new GifDecoder(is, this);
		//开始执行解析之前，需要将所有的状态置为默认
		mAnimationType = GifImageType.SYNC_DECODER;
		mStatus = 0;
		mGifDecoder.start();
	}

	private void freeDecoder(GifDecoder gifDecoder) {
		if (gifDecoder != null) {
			gifDecoder.free();
			gifDecoder = null;
		}
	}

	/**
	 * 以字节数据形式设置gif图片
	 * 
	 * @param gif
	 * 图片
	 */
	public void setGifImage(byte[] gif) {
		setGifDecoderImage(gif);
	}

	/**
	 * 以字节流形式设置gif图片
	 * 
	 * @param is
	 * 图片
	 */
	public void setGifImage(InputStream is) {
		setGifDecoderImage(is);
	}

	/**
	 * 以资源形式设置gif图片
	 * 
	 * @param resId
	 * gif图片的资源ID
	 */
	public void setGifImage(int resId) {
		Resources r = this.getResources();
		InputStream is = r.openRawResource(resId);
		setGifDecoderImage(is);
	}
	Paint mPaint = new Paint();
	@Override
	protected void onDraw(Canvas canvas) {
		if (mGifDecoder == null) {
			return;
		}
		if (mCurrentImage == null) {
			mCurrentImage = mGifDecoder.getImage();
		}
		if (mCurrentImage == null) {
			return;
		}
		int saveCount = canvas.getSaveCount();
		canvas.save();
		canvas.translate(getPaddingLeft(), getPaddingTop());
		mPaint.setAntiAlias(true);
		if (mShowWidth == -1) {
			canvas.drawBitmap(mCurrentImage, 0, 0, mPaint);
		} else {
			canvas.drawBitmap(mCurrentImage, null, mRect, mPaint);
		}
		canvas.restoreToCount(saveCount);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int pleft = getPaddingLeft();
		int pright = getPaddingRight();
		int ptop = getPaddingTop();
		int pbottom = getPaddingBottom();

		int widthSize;
		int heightSize;

		int w;
		int h;

		if (mGifDecoder == null) {
			w = 1;
			h = 1;
		} else {
			w = mGifDecoder.width;
			h = mGifDecoder.height;
		}

		w += pleft + pright;
		h += ptop + pbottom;

		w = Math.max(w, getSuggestedMinimumWidth());
		h = Math.max(h, getSuggestedMinimumHeight());

		widthSize = resolveSize(w, widthMeasureSpec);
		heightSize = resolveSize(h, heightMeasureSpec);

		setMeasuredDimension(widthSize, heightSize);
	}

	/**
	 * 只显示第一帧图片<br>
	 * 调用本方法后，gif不会显示动画，只会显示gif的第一帧图
	 */
	public void showCover() {
		mAnimationType = GifImageType.COVER;
		if (mGifDecoder != null) {
			int count = mGifDecoder.getFrameCount();
			if (count > 0) {
				stopAnimation();
				Bitmap currentImage = mGifDecoder.getImage();
				mCurrentImage = currentImage;
				invalidate();
				destory();
			}
		}
	}

	/**
	 * 设置gif在解码过程中的显示方式<br>
	 * <strong>本方法只能在setGifImage方法之前设置，否则设置无效</strong>
	 * 
	 * @param type
	 * 显示方式
	 */
	public void setGifImageType(GifImageType type) {
		if (mGifDecoder == null) {
			mAnimationType = type;
		}
	}

	/**
	 * 设置要显示的图片的大小<br>
	 * 当设置了图片大小 之后，会按照设置的大小来显示gif（按设置后的大小来进行拉伸或压缩）
	 * 
	 * @param width
	 * 要显示的图片宽
	 * @param height
	 * 要显示的图片高
	 */
	public void setShowDimension(int width, int height) {
		if (width > 0 && height > 0) {
			mShowWidth = width;
			mRect = new Rect();
			mRect.left = 0;
			mRect.top = 0;
			mRect.right = width;
			mRect.bottom = height;
		}
	}

	private final AnimationHandler mGifHandler = new AnimationHandler();

	@Override
	public void parseOk(boolean parseStatus, int frameIndex) {
		if (parseStatus) {
			if (mGifDecoder != null) {
				switch (mAnimationType) {
					case COVER:
						if (frameIndex == 1) {
							mCurrentImage = mGifDecoder.getImage();
							reDraw();
						}
						destory();
						break;
					case SYNC_DECODER:
						//如果当前动画是默认状态，再开始执行动画操作
						if (mStatus == 0) {
							mGifHandler.startAnimation(this);
						}
						break;
					default:
						break;
				}
			}
		}
	}

	private void reDraw() {
		if (redrawHandler != null) {
			Message msg = redrawHandler.obtainMessage();
			redrawHandler.sendMessage(msg);
		}
	}

	private final Handler redrawHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			invalidate();
		}
	};

	/**
	 * 停止显示动画<br>
	 */
	public void stopAnimation() {
		mGifHandler.stopAnimation(this);
	}

	/**
	 * 停止显示动画,所占用的资源也会释放<br>
	 * <b>activity销毁时最好调用此方法，释放gif占用的资源</b>
	 */
	public void destory() {
		mGifHandler.destory(this);
	}

	/**
	 * 开始显示动画效果
	 */
	public void startAnimation() {
		if (mStatus == AnimationHandler.fAnimation) {
			return;
		} else if (mStatus == AnimationHandler.fStop) {
			mGifHandler.startAnimation(this);
		} else {
			if (mResId != -1) {
				setGifImage(mResId);
			}
		}
	}

	/**
	 * 动画handler
	 */
	private static class AnimationHandler extends Handler {
		private static final int fAnimation = 2;
		private static final int fStop = 3;
		private static final int fDestory = 4;
		private WeakReference<GifView> mGifViewReference;

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
				case fAnimation:
					executeAnimation();
					break;
			}
		}

		private void executeAnimation() {
			GifView gv = mGifViewReference.get();
			if (gv != null && gv.mStatus == fAnimation) {
				gv.invalidate();
				GifFrame frame = gv.mGifDecoder.next();
				gv.mCurrentImage = frame.image;
				long sp = frame.delay;
				sendEmptyMessageDelayed(fAnimation, sp);
			}
		}

		/**
		 * 开始执行动画，多次调用无影响，如果已经开始动画，则不会重复执行
		 * @param gifView 需要开始的gifview
		 */
		public void startAnimation(GifView gifView) {
			if (gifView.mStatus != fAnimation) {
				gifView.mStatus = fAnimation;
				mGifViewReference = new WeakReference<GifView>(gifView);
				sendEmptyMessage(fAnimation);
			}
		}

		/**
		 * 停止动画执行
		 * @param gifView 需要停止的gifview
		 */
		public void stopAnimation(GifView gifView) {
			gifView.mStatus = fStop;
			removeMessages(fAnimation);
		}

		/**
		 * 销毁动画，释放资源
		 * @param gifView 需要销毁的gifview
		 */
		public void destory(GifView gifView) {
			gifView.mStatus = fDestory;
			gifView.mGifDecoder.free();
			if (mGifViewReference != null) {
				mGifViewReference.clear();
			}
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		destory();
	}
}
