package medicationManager;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.thorbek.myqrscanner2.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
/* Import ZBar Class files */

public class ScanActivity extends Activity
{
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;

	String ndc="";
	TextView scanText;
	Button scanButton;

	ImageScanner scanner;
	
	private boolean barcodeScanned = false;
	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	} 

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();

		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
		preview.addView(mPreview);

		scanText = (TextView)findViewById(R.id.scanText);

		scanButton = (Button)findViewById(R.id.ScanButton);

		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (barcodeScanned) {
					barcodeScanned = false;
					scanText.setText("Scanning...");
					mCamera.setPreviewCallback(previewCb);
					mCamera.startPreview();
					previewing = true;
					mCamera.autoFocus(autoFocusCB);
				}
			}
		});
	}

	public void onPause() {
		super.onPause();
		releaseCamera();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e){
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				String output="";
				for (Symbol sym : syms) {
					output=sym.getData();
					output=output.replaceAll("\\D","");
					if(output.substring(0,4).equals("0100")||output.substring(0,4).equals("0110"))
						output=output.substring(4);
					
					if(output.substring(0,2).equals("03"))
						output=output.substring(2);
					else if(output.charAt(0)=='3')
						output=output.substring(1);
					if(output.length()>11)
					{
						output=output.substring(0,12);
					}	
					if(output.length()>10&&output.charAt(10)!='0')
					{
						output=output.substring(0,10);
						output=output.substring(0,9)+"0"+output.substring(9);
					}
					if (output.length()<11)
					{
						output=output.substring(0,5)+"0"+output.substring(5);
					}
						output=output.substring(0,5)+"-"+output.substring(5,9)+"-"+output.substring(9);
						ndc=output;
					scanText.setText(output);
					barcodeScanned = true;
					makeToast(output);

					
					
					Intent out=new Intent();
					out.putExtra("NDC", ndc);
					if (getParent() == null) {
					    setResult(Activity.RESULT_OK, out);
					} else {
					    getParent().setResult(Activity.RESULT_OK, out);
					}
					finish();
				}
			}
		}
	};
	
	
	private void makeToast(String string) {
		Toast.makeText(getBaseContext(), string, Toast.LENGTH_LONG).show();
	}

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
}