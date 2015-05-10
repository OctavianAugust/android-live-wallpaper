package circle.box.gallery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ConvertImage {

	private static final int PIXEL_SIZE_IMAGE = 200;

	// private static final String LOG_TAG = "monitoring";

	public static String convertImageToBase64String(String filePath) {
		File imagePath = new File(filePath);
		String encodedImage = null;
		try {
			if (imagePath.exists()) {
				Bitmap myBitmap = decodeFile(imagePath, PIXEL_SIZE_IMAGE,
						PIXEL_SIZE_IMAGE, false);
				byte[] byteArrayImage = getByteArrayfromBitmap(myBitmap);

				return encodedImage = Base64.encodeToString(byteArrayImage,
						Base64.DEFAULT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encodedImage;
	}

	public static byte[] getByteArrayfromBitmap(Bitmap bitmap) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, bos);
		return bos.toByteArray();
	};

	public static byte[] convertBase64StringToByteArray(String source) {
		byte[] rawImage = Base64.decode(source.getBytes(), Base64.DEFAULT);
		return rawImage;
	}

	// decodes image and scales it to reduce memory consumption
	public static Bitmap decodeFile(File bitmapFile, int requiredWidth,
			int requiredHeight, boolean quickAndDirty) {
		try {
			// Decode image size
			BitmapFactory.Options bitmapSizeOptions = new BitmapFactory.Options();
			bitmapSizeOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(bitmapFile), null,
					bitmapSizeOptions);

			// load image using inSampleSize adapted to required image size
			BitmapFactory.Options bitmapDecodeOptions = new BitmapFactory.Options();
			bitmapDecodeOptions.inTempStorage = new byte[16 * 1024];
			bitmapDecodeOptions.inSampleSize = computeInSampleSize(
					bitmapSizeOptions, requiredWidth, requiredHeight, false);
			bitmapDecodeOptions.inPurgeable = true;
			bitmapDecodeOptions.inDither = !quickAndDirty;
			bitmapDecodeOptions.inPreferredConfig = quickAndDirty ? Bitmap.Config.RGB_565
					: Bitmap.Config.ARGB_8888;

			Bitmap decodedBitmap = BitmapFactory.decodeStream(
					new FileInputStream(bitmapFile), null, bitmapDecodeOptions);

			// scale bitmap to mathc required size (and keep aspect ratio)

			float srcWidth = (float) bitmapDecodeOptions.outWidth;
			float srcHeight = (float) bitmapDecodeOptions.outHeight;

			float dstWidth = (float) requiredWidth;
			float dstHeight = (float) requiredHeight;

			float srcAspectRatio = srcWidth / srcHeight;
			float dstAspectRatio = dstWidth / dstHeight;

			// recycleDecodedBitmap is used to know if we must recycle
			// intermediary
			// 'decodedBitmap'
			// (DO NOT recycle it right away: wait for end of bitmap
			// manipulation process to avoid
			// java.lang.RuntimeException: Canvas: trying to use a recycled
			// bitmap
			// android.graphics.Bitmap@416ee7d8
			// I do not excatly understand why, but this way it's OK

			boolean recycleDecodedBitmap = false;

			Bitmap scaledBitmap = decodedBitmap;
			if (srcAspectRatio < dstAspectRatio) {
				scaledBitmap = getScaledBitmap(decodedBitmap, (int) dstWidth,
						(int) (srcHeight * (dstWidth / srcWidth)));
				// will recycle recycleDecodedBitmap
				recycleDecodedBitmap = true;
			} else if (srcAspectRatio > dstAspectRatio) {
				scaledBitmap = getScaledBitmap(decodedBitmap,
						(int) (srcWidth * (dstHeight / srcHeight)),
						(int) dstHeight);
				recycleDecodedBitmap = true;
			}

			// crop image to match required image size

			Bitmap croppedBitmap = scaledBitmap;

			if (recycleDecodedBitmap) {
				decodedBitmap.recycle();
			}
			decodedBitmap = null;

			scaledBitmap = null;
			return croppedBitmap;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static int computeInSampleSize(BitmapFactory.Options options,
			int dstWidth, int dstHeight, boolean powerOf2) {
		int inSampleSize = 1;

		// Raw height and width of image
		final int srcHeight = options.outHeight;
		final int srcWidth = options.outWidth;

		if (powerOf2) {
			// Find the correct scale value. It should be the power of 2.

			int tmpWidth = srcWidth, tmpHeight = srcHeight;
			while (true) {
				if (tmpWidth < dstWidth || tmpHeight < dstHeight)
					break;
				tmpWidth /= 2;
				tmpHeight /= 2;
				inSampleSize *= 2;
			}
		} else {
			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) srcHeight
					/ (float) dstHeight);
			final int widthRatio = Math.round((float) srcWidth
					/ (float) dstWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap getScaledBitmap(Bitmap bitmap, int newWidth,
			int newHeight) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

}
