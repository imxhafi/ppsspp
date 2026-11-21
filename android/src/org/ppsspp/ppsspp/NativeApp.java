package org.ppsspp.ppsspp;

import android.os.Build;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;

import androidx.annotation.RequiresApi;

public class NativeApp {
	private static final String TAG = "PPSSPPNativeActivity";

	public static final int DEVICE_ID_DEFAULT = 0;
	public static final int DEVICE_ID_KEYBOARD = 1;
	public static final int DEVICE_ID_MOUSE = 2;
	public static final int DEVICE_ID_PAD_0 = 10;

	public static final int DEVICE_TYPE_MOBILE = 0;
	public static final int DEVICE_TYPE_TV = 1;
	public static final int DEVICE_TYPE_DESKTOP = 2;
	public static final int DEVICE_TYPE_VR = 3;

	// These are matched with the C++ RequestManager result codes, and also Activity.RESULT_OK/RESULT_CANCELED.
	public static final int RESULT_OK = -1;
	public static final int RESULT_CANCELED = 0;
	public static final int RESULT_ERROR_ACTIVITY_NOT_FOUND = 1;
	public static final int RESULT_ERROR_OTHER_ACTIVITY_ERROR = 2;

	public static native void init(String model, int deviceType, String languageRegion, String apkPath, String dataDir, String externalStorageDir, String extFilesDir, String nativeLibDir, String additionalStorageDirs, String cacheDir, String shortcutParam, String installerName, int androidVersion, String board);
	public static native void audioInit();
	public static native void audioShutdown();
	public static native void audioConfig(int optimalFramesPerBuffer, int optimalSampleRate);

	public static native void audioRecording_SetSampleRate(int sampleRate);
	public static native void audioRecording_Start();
	public static native void audioRecording_Stop();

	public static native void computeDesiredBackbufferDimensions();
	public static native int getDesiredBackbufferWidth();
	public static native int getDesiredBackbufferHeight();

	public static native void setDisplayParameters(int display_xres, int display_yres, int dpi, float refreshRate);
	// rotation is the Surface.ROTATION_* of the display. A change in it alone (a 180 degree flip) keeps the size
	// but the swapchain still has to be recreated, for the pre-rotation.
	public static native void backbufferResize(int bufferWidth, int bufferHeight, int rotation);

	// A second display, like a TV, that the game is shown on. addOutput returns the output's id, or 0.
	// removeOutput returns once the native side is done with the surface.
	public static native int addOutput(Surface surface, int width, int height);
	public static native void resizeOutput(int id, int width, int height);
	public static native void removeOutput(int id);
	public static native void setDeviceName(String name);

	public static native boolean isAtTopLevel();

	// These have Android semantics: Resume is always called on bootup, after init
	public static native void pause();
	public static native void resume();

	public static native void shutdown();

	public static native boolean keyDown(int deviceId, int key, boolean isRepeat);
	public static native boolean keyUp(int deviceId, int key);
	public static native boolean keyChar(int deviceId, int unicodeChar);

	public static native void joystickAxis(int deviceId, int []axis, float []value, int count);

	public static native boolean mouseWheelEvent(float x, float y);

	// Sensor/input data. These are asynchronous, beware!
	public static native void touch(float x, float y, int data, int pointerId);

	public static native void accelerometer(float x, float y, float z);
	public static native void gyroscope(float x, float y, float z, long timestampNs);

	public static native void mouse(float x, float y, int button, int action);
	public static native void mouseDelta(float x, float y);
	public static native void sendMessageFromJava(String msg, String arg);
	public static native void sendRequestResult(int seqID, boolean result, String value, int iValue);
	public static native void setAchievementsHostOverride(String host);
	public static native void clearAchievementsHostOverride();
	public static native String queryConfig(String queryName);
	// The content URIs that the recent list, the remembered shortcuts and the settings still point at.
	// Null if the config in memory may not be the user's (see HaveUsersConfig).
	public static native String[] getReferencedUris();
	// Remembers these as home screen shortcut targets, oldest first, and saves the config. Only call during
	// Initialize. False if that didn't work out.
	public static native boolean addShortcutUris(String[] uris);

	public static native int getSelectedCamera();
	public static native int getDisplayFramerateMode();
	public static native void setGpsDataAndroid(long time, float hdop, float latitude, float longitude, float altitude, float speed, float bearing);
	public static native void setSatInfoAndroid(short index, short id, short elevation, short azimuth, short snr, short good);
	public static native void pushCameraImageAndroid(byte[] image);

	// Wrappers
	public static void reportException(Exception e, String data) {
		StringBuilder str = new StringBuilder(e.toString() + "\n" + e.getMessage() + "\n");
		if (data != null) {
			str.append(data).append("\n");
		}
		// could also use import android.util.Log; String stackTrace = Log.getStackTraceString(exception);
		int count = 0;
		for (StackTraceElement ste : e.getStackTrace()) {
			str.append(ste).append("\n");
			// Only bother with the top of the stack.
			if (count > 3) {
				break;
			}
			count++;
		}
		NativeApp.sendMessageFromJava("exception", str.toString());
	}

	public static void reportError(String errorStr) {
		NativeApp.sendMessageFromJava("exception", errorStr);
	}

	@RequiresApi(Build.VERSION_CODES.N)
	private static void processMouseDelta(final MotionEvent ev) {
		if ((ev.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
			float dx = ev.getAxisValue(MotionEvent.AXIS_RELATIVE_X);
			float dy = ev.getAxisValue(MotionEvent.AXIS_RELATIVE_Y);
			// Log.i(TAG, "Mouse delta: " + dx + " " + dy);
			NativeApp.mouseDelta(dx, dy);
		}
	}

	public static boolean isFromSource(final InputEvent ev, int source) {
		return (ev.getSource() & source) == source;
	}

	private static void onMouseEventMotion(final MotionEvent ev) {
		// Log.i(TAG, "motion mouse event");
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: {
				if (PpssppActivity.useModernMouseEvents) {
					return;
				}
				//Log.i(TAG, "Surface Action down. button state: " + ev.getButtonState());
				NativeApp.mouse(ev.getX(), ev.getY(), 1, 1);
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (PpssppActivity.useModernMouseEvents) {
					return;
				}
				//Log.i(TAG, "Surface Action up. button state: " + ev.getButtonState());
				NativeApp.mouse(ev.getX(), ev.getY(), 1, 2);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// This still needs handling here, even if new events are used.
				//Log.i(TAG, "Surface Action move. button state: " + ev.getButtonState());
				NativeApp.mouse(ev.getX(), ev.getY(), 0, 0);
				break;
			}
			default: {
				Log.i(TAG, "Unhandled modern mouse action: " + ev.getAction());
				break;
			}
		}
	}


	// Optional gesture recognition. Events are still forwarded to the normal touch path below,
	// so the existing PPSSPP on-screen controls continue to receive their original input.
	private static boolean gestureTracking;
	private static boolean gestureMultiTouch;
	private static boolean gesturePinchFired;
	private static float gestureDownX;
	private static float gestureDownY;
	private static float gestureTwoFingerDistance;
	private static long gestureDownTime;
	private static long lastGestureTapTime;

	private static int readGestureValue(String key) {
		try {
			return Integer.parseInt(queryConfig(key));
		} catch (Exception e) {
			return 0;
		}
	}

	private static boolean gestureControlEnabled() {
		String value = queryConfig("GestureControlEnabled");
		return "1".equals(value) || "true".equalsIgnoreCase(value);
	}

	private static int gestureKeyCode(int action) {
		switch (action) {
		case 1: return KeyEvent.KEYCODE_BUTTON_A;      // Cross
		case 2: return KeyEvent.KEYCODE_BUTTON_B;      // Circle
		case 3: return KeyEvent.KEYCODE_BUTTON_X;      // Square
		case 4: return KeyEvent.KEYCODE_BUTTON_Y;      // Triangle
		case 5: return KeyEvent.KEYCODE_BUTTON_L1;
		case 6: return KeyEvent.KEYCODE_BUTTON_R1;
		case 7: return KeyEvent.KEYCODE_BUTTON_START;
		case 8: return KeyEvent.KEYCODE_BUTTON_SELECT;
		default: return 0;
		}
	}

	private static void fireGesture(int action) {
		int key = gestureKeyCode(action);
		if (key == 0) {
			return;
		}
		keyDown(DEVICE_ID_DEFAULT, key, false);
		keyUp(DEVICE_ID_DEFAULT, key);
	}

	public static void fireMotionGesture(String configKey) {
		if (gestureControlEnabled()) {
			fireGesture(readGestureValue(configKey));
		}
	}

	private static float readGestureFloat(String key, float fallback) {
		try {
			return Float.parseFloat(queryConfig(key));
		} catch (Exception e) {
			return fallback;
		}
	}

	private static float pointerDistance(MotionEvent ev) {
		if (ev.getPointerCount() < 2) {
			return 0.0f;
		}
		float dx = ev.getX(0) - ev.getX(1);
		float dy = ev.getY(0) - ev.getY(1);
		return (float)Math.sqrt(dx * dx + dy * dy);
	}

	private static void processGestureEvent(MotionEvent ev) {
		final int action = ev.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			gestureTracking = gestureControlEnabled();
			gestureMultiTouch = false;
			gesturePinchFired = false;
			gestureDownX = ev.getX();
			gestureDownY = ev.getY();
			gestureDownTime = ev.getEventTime();
			return;
		}
		if (!gestureTracking) {
			return;
		}
		if (action == MotionEvent.ACTION_CANCEL) {
			gestureTracking = false;
			return;
		}
		if (action == MotionEvent.ACTION_POINTER_DOWN && ev.getPointerCount() >= 2) {
			gestureMultiTouch = true;
			gestureTwoFingerDistance = pointerDistance(ev);
			return;
		}
		if (gestureMultiTouch) {
			if (action == MotionEvent.ACTION_MOVE && ev.getPointerCount() >= 2 && !gesturePinchFired) {
				float delta = pointerDistance(ev) - gestureTwoFingerDistance;
				if (Math.abs(delta) >= 55.0f) {
					fireGesture(readGestureValue(delta < 0 ? "PinchInGesture" : "PinchOutGesture"));
					gesturePinchFired = true;
				}
				return;
			}
			if (action == MotionEvent.ACTION_POINTER_UP && ev.getPointerCount() == 2) {
				if (!gesturePinchFired && ev.getEventTime() - gestureDownTime <= 500) {
					fireGesture(readGestureValue("TwoFingerTapGesture"));
				}
				gestureTracking = false;
			}
			return;
		}
		if (ev.getPointerCount() > 1) {
			gestureMultiTouch = true;
			return;
		}
		if (action != MotionEvent.ACTION_UP) {
			return;
		}

		gestureTracking = false;
		final float dx = ev.getX() - gestureDownX;
		final float dy = ev.getY() - gestureDownY;
		final long duration = ev.getEventTime() - gestureDownTime;
		final float sensitivity = Math.max(0.5f, readGestureFloat("SwipeSensitivity", 1.0f));
		final float threshold = 90.0f / sensitivity;
		if (Math.max(Math.abs(dx), Math.abs(dy)) >= threshold) {
			if (Math.abs(dx) > Math.abs(dy)) {
				fireGesture(readGestureValue(dx < 0 ? "SwipeLeft" : "SwipeRight"));
			} else {
				fireGesture(readGestureValue(dy < 0 ? "SwipeUp" : "SwipeDown"));
			}
		} else if (duration >= 500) {
			fireGesture(readGestureValue("LongPressGesture"));
		} else {
			fireGesture(readGestureValue("TapGesture"));
			if (duration <= 300) {
				long now = ev.getEventTime();
				if (now - lastGestureTapTime <= 300) {
					fireGesture(readGestureValue("DoubleTapGesture"));
					lastGestureTapTime = 0;
				} else {
					lastGestureTapTime = now;
				}
			}
		}
	}

	// Forwarded from onTouchEvent in the SurfaceView classes.
	public static void processTouchEvent(final MotionEvent ev) {
		if (!isFromSource(ev, InputDevice.SOURCE_MOUSE)) {
			processGestureEvent(ev);
		}
		if (isFromSource(ev, InputDevice.SOURCE_MOUSE)) {
			// This is where workable mouse support arrived.
			onMouseEventMotion(ev);
		}

		// Log.i(TAG, "processing touch event");
		for (int i = 0; i < ev.getPointerCount(); i++) {
			int pid = ev.getPointerId(i);
			int code = 0;

			final int action = ev.getActionMasked();

			// These code bits are now the same as the constants in input_state.h.
			switch (action) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					// Log.i(TAG, "ACTION_DOWN");
					if (ev.getActionIndex() == i)
						code = 2;
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					// Log.i(TAG, "ACTION_UP");
					if (ev.getActionIndex() == i)
						code = 4;
					break;
				case MotionEvent.ACTION_CANCEL:
					// The whole gesture is over (the system took it, for example), so every pointer in the
					// event is, not just the one at the action index. Otherwise the rest stay held down
					// on the native side. UP | CANCEL, so the UI lets go without clicking.
					Log.i(TAG, "ACTION_CANCEL");
					code = 4 | 8;
					break;
				case MotionEvent.ACTION_MOVE: {
					code = 1;
					if (Build.VERSION.SDK_INT >= 24) {
						processMouseDelta(ev);
					}
					break;
				}
				default:
					break;
			}

			if (code != 0) {
				int tool = ev.getToolType(i);
				code |= tool << 10; // We use the Android tool type codes
				NativeApp.touch(ev.getX(i), ev.getY(i), code, pid);
			}
		}
	}
}
