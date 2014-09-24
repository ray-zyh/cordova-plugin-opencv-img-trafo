package de.michaelskoehler.imgtrafo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import de.michaelskoehler.opencvandroidplayground.R;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.SurfaceView;
import android.view.LayoutInflater;
import android.view.View; 
import android.widget.ImageView;

public class ImgTrafo extends CordovaPlugin {
    public static final String ACTION_SHOW_ALERT_DIALOG = "showAlertDialog"; //plugin
    private static String debugVars = ""; //debugging
    
    //plugin
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
        	
        	// Case: showAlertDialog action
            if (ACTION_SHOW_ALERT_DIALOG.equals(action)) { 
            	/*
            	// Fetch arguments from cordova js plugin
            	JSONObject arg_object = args.getJSONObject(0);
            	String message = arg_object.getString("message");
            	*/
            	
            	// get some application variables
            	final Activity activity = this.cordova.getActivity();
            	Context context = activity.getApplicationContext();
            	Resources resources = context.getResources();
            	String packageName = context.getPackageName();
            	
            	// dynamical version of setContentView(R.layout.activity_main);
            	LayoutInflater inflater = LayoutInflater.from(context);
            	View appearance = inflater.inflate(resources.getIdentifier("activity_main", "layout", packageName),null);
            	
                BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this.cordova.getActivity()) {
                	@Override
                	public void onManagerConnected(int status) {
                		switch (status) {
            	    		case LoaderCallbackInterface.SUCCESS:
            	    		{
            	    			// OPENCV ACTIONS here:
            	    			// setting image resource from drawable via bitmap
            	    	 		Bitmap b_input = BitmapFactory.decodeResource(getResources(), R.drawable.left07);
            	    	 		Bitmap b_output = BitmapFactory.decodeResource(getResources(), R.drawable.left08);
            	    	 		
            	    	 		//b_output = imgtrafo(b_input, b_output, 216, 70, 421, 108, 305, 447, 120, 354);
            	    	 		b_output = canny(b_input);
            	    	 		
            	    	 		/*
            	    	 		saveImageToInternalStorage(b_output, "bild2.png");
            	    	 		
            	    	 		Bitmap b_read = readImageFromInternalStorage("bild2.png");
            	    	 		
            	    	        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
            	    	 		imageView.setImageBitmap(b_read);
            	    	 		
            	                // my alert hello world
            	                // new AlertDialog.Builder(activity).setTitle("Alert").setMessage("loading successful").show();
            	                */
            	    			
            	    		} break;
            	    		default:
            	    		{
            	    			super.onManagerConnected(status);
            	    		} break;
                		}
                	}
                };
            	
                // init opencv and start actions (see mLoaderCallback below)
            	OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, activity, mLoaderCallback);
            	
               callbackContext.success();
               return true;
            }
            
            // Case: other, non-supported action
            callbackContext.error("Invalid action");
            return false; 
            
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            
            // get stack trace as string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            // build error msg
            String errorMsg = "Debug-Vars: \n";
            errorMsg = errorMsg.concat(this.debugVars);
            errorMsg = errorMsg.concat(" \n Stack Trace: ");
            errorMsg = errorMsg.concat(sw.toString());
            
            callbackContext.error(errorMsg);
            return false;
        } 
    }
    
  private static Bitmap canny(Bitmap image) {
    	
    	// convert image to matrix
    	Mat Mat1 = new Mat(image.getWidth(), image.getHeight(), CvType.CV_32FC1);
    	Utils.bitmapToMat(image, Mat1);
    	
    	// create temporary matrix2
    	Mat Mat2 = new Mat(image.getWidth(), image.getHeight(), CvType.CV_32FC1);
    	
    	// convert image to grayscale
    	Imgproc.cvtColor(Mat1, Mat2, Imgproc.COLOR_BGR2GRAY);
    	
    	// doing a gaussian blur prevents getting a lot of false hits
    	Imgproc.GaussianBlur(Mat2, Mat1, new Size(3, 3), 2, 2); //?
    	
    	// now apply canny function
    	int param_threshold1 = 25; // manually defined
    	int param_threshold2 = param_threshold1*3; //Cannys recommendation
    	Imgproc.Canny(Mat1, Mat2, param_threshold1, param_threshold2);
    	
    	// ?
        Imgproc.cvtColor(Mat2, Mat1, Imgproc.COLOR_GRAY2BGRA, 4);

    	// convert matrix to output bitmap
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(Mat1, output);
        return output;
    }
    
    
    private static Bitmap imgtrafo(Bitmap image1, Bitmap image2, int p1_x, int p1_y, int p2_x, int p2_y, int p3_x, int p3_y, int p4_x, int p4_y) {
    	// set output size same size as input
    	int resultWidth = image1.getWidth();
        int resultHeight = image1.getHeight();
    	
    	Mat inputMat = new Mat(image1.getWidth(), image1.getHeight(), CvType.CV_32FC1);
        Utils.bitmapToMat(image1, inputMat);
        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_32FC1); 

        Point ocvPIn1 = new Point(p1_x, p1_y);
        Point ocvPIn2 = new Point(p2_x, p2_y);
        Point ocvPIn3 = new Point(p3_x, p3_y);
        Point ocvPIn4 = new Point(p4_x, p4_y);
        List<Point> source = new ArrayList<Point>();
        source.add(ocvPIn1);
        source.add(ocvPIn2);
        source.add(ocvPIn3);
        source.add(ocvPIn4);
        Mat inputQuad = Converters.vector_Point2f_to_Mat(source);

        Point ocvPOut1 = new Point(256, 40); // manually set
        Point ocvPOut2 = new Point(522, 62);
        Point ocvPOut3 = new Point(455, 479);
        Point ocvPOut4 = new Point(134, 404);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat outputQuad = Converters.vector_Point2f_to_Mat(dest);      

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(inputQuad, outputQuad);

        Imgproc.warpPerspective(inputMat, 
                                outputMat,
                                perspectiveTransform,
                                new Size(resultWidth, resultHeight)); //?

        Bitmap output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        return output;
    }
    
    private void saveImageToInternalStorage(Bitmap image, String filename_inclpng) {
    	try {
	    	Context context = this.cordova.getActivity().getApplicationContext();
	    	// Use the compress method on the Bitmap object to write image to
	    	// the OutputStream
	    	FileOutputStream fos = context.openFileOutput(filename_inclpng, Context.MODE_PRIVATE);
	
	    	// Writing the bitmap to the output stream
	    	image.compress(Bitmap.CompressFormat.PNG, 100, fos);
	    	fos.close();
    	} catch (Exception e) {
	    	Log.e("err in saveToInternalStorage()", e.getMessage());
    	}
    }
	
    private Bitmap readImageFromInternalStorage(String filename) {
		try {
	    	Context context = this.cordova.getActivity().getApplicationContext();
			File filePath = context.getFileStreamPath(filename);
			FileInputStream fi = new FileInputStream(filePath);
			return BitmapFactory.decodeStream(fi);
		} catch (Exception ex) {
			Log.e("err in readImageFromInternalStorage()", ex.getMessage());
			return null;
		}
	}
}