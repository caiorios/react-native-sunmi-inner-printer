package com.sunmi.innerprinter;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

public class BytesUtil {
	/**
	 * Generate intermittent black block data
	 * @param w : Paper width, unit point
	 * @return
	 */
	public static byte[] initBlackBlock(int w){
		int ww = (w + 7)/8 ;
		int n = (ww + 11)/12;
		int hh = n * 24;
		byte[] data = new byte[ hh * ww + 10];
				
		data[0] = 0x0A;
		data[1] = 0x1D;
		data[2] = 0x76;
		data[3] = 0x30;
		data[4] = 0x00;
		
		data[5] = (byte)ww;//xL
		data[6] = (byte)(ww >> 8);//xH
		data[7] = (byte)hh;
		data[8] = (byte)(hh >> 8);
		
		int k = 9;
		for(int i=0; i < n; i++){
			for(int j=0; j<24; j++){
				for(int m =0; m<ww; m++){
					if(m/12 == i){
						data[k++] = (byte)0xFF;
					}else{
						data[k++] = 0;
					}
				}
			}
		}
		data[k++] = 0x0A;
		return data;
	}	
	/**
	 * Generate a large block of black data
	 * @param h : Black block height, unit point
	 * @param w : Black block width, unit point, multiple of 8
	 * @return
	 */
	public static byte[] initBlackBlock(int h, int w){
		int hh = h;
		int ww = (w - 1)/8 + 1;
		byte[] data = new byte[ hh * ww + 10];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = (byte)hh;
		data[7] = (byte)(hh >> 8);
		
		int k = 8;
		for(int i=0; i<hh; i++){
			for(int j=0; j<ww; j++){
				data[k++] = (byte)0xFF;
			}
		}
		data[k++] = 0x00;data[k++] = 0x00;
		return data;
	}	
	/**
	 * Generate gray block data
	 * @param h : Gray block height, unit point
	 * @param w : Gray block width, unit point, multiples of 8
	 * @return
	 */
	public static byte[] initGrayBlock(int h, int w){
		int hh = h;
		int ww = (w - 1)/8 + 1;
		byte[] data = new byte[ hh * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = (byte)hh;
		data[7] = (byte)(hh >> 8);
		
		int k = 8;
		byte m =(byte)0xAA;
		for(int i=0; i<hh; i++){
			m = (byte)~m;
			for(int j=0; j<ww; j++){
				data[k++] = m;
			}
		}
		//ata[k++] = 0x00;data[k++] = 0x00;
		return data;
	}
	
	/**
	 * Generate tabular data
	 * @param h : Number of grids per column, 32 dots per grid
	 * @param w : Number of grids per row, 32 dots wide per grid
	 * @return
	 */
	public static byte[] initTable(int h, int w){
		int hh = h * 32;
		int ww = w * 4;
		
		byte[] data = new byte[ hh * ww + 10];
		
		
		data[0] = 0x0A;
		data[1] = 0x1D;
		data[2] = 0x76;
		data[3] = 0x30;
		data[4] = 0x00;
		
		data[5] = (byte)ww;//xL
		data[6] = (byte)(ww >> 8);//xH
		data[7] = (byte)hh;
		data[8] = (byte)(hh >> 8);
		
		int k = 9;
		int m = 31;
		for(int i=0; i<h; i++){
			for(int j=0; j<w; j++){
				data[k++] = (byte)0xFF;
				data[k++] = (byte)0xFF;
				data[k++] = (byte)0xFF;
				data[k++] = (byte)0xFF;
			}
            if(i == h-1) m =30;
			for(int t=0; t< m; t++){
				for(int j=0; j<w-1; j++){
					data[k++] = (byte)0x80;
					data[k++] = (byte)0;
					data[k++] = (byte)0;
					data[k++] = (byte)0;	
				}
				data[k++] = (byte)0x80;
				data[k++] = (byte)0;
				data[k++] = (byte)0;
				data[k++] = (byte)0x01;				
			}
		}
		for(int j=0; j<w; j++){
			data[k++] = (byte)0xFF;
			data[k++] = (byte)0xFF;
			data[k++] = (byte)0xFF;
			data[k++] = (byte)0xFF;
		}		
		data[k++] = 0x0A;
		return data;
	}
	
    public static byte[] getGbk(String paramString)
    {
		byte[] arrayOfByte = null;
		try 
		{
			arrayOfByte = paramString.getBytes("GBK");  //It must be placed in the try
		}
		catch (Exception   ex) {
				ex.printStackTrace();
		}
		return arrayOfByte;
    }
    
	public static byte[] setWH(int mode) {
		byte[] returnText = new byte[3]; // GS! 11H double width and height
		returnText[0] = 0x1D;
		returnText[1] = 0x21;

		switch (mode) // 1-none; 2-times wide; 3-times high; 4-times wide and double high
		{
		case 2:
			returnText[2] = 0x10;
			break;
		case 3:
			returnText[2] = 0x01;
			break;
		case 4:
			returnText[2] = 0x11;
			break;
		default:
			returnText[2] = 0x00;
			break;
		}

		return returnText;
	}
    //Set magnification 1 to 8 times (0-7)
    public static byte[] setZoom(int level){
    	byte[] rv = new byte[3];
    	rv[0] = 0x1D;
    	rv[1] = 0x21;
    	rv[2] = (byte)((level & 0x07)<<4 | (level & 0x07));   	
    	return rv;
    }
	public static byte[] setAlignCenter(int align) {
		byte[] returnText = new byte[5]; // Align ESC a
		returnText[0] = 0x20;
		returnText[1] = 0x0A;
		returnText[2] = 0x1B;
		returnText[3] = 0x61;

		switch (align) // 0-left alignment; 1-center alignment; 2-right alignment
		{
		case 1:
			returnText[4] = 0x01;
			break;
		case 2:
			returnText[4] = 0x02;
			break;
		default:
			returnText[4] = 0x00;
			break;
		}
		return returnText;
	}

	public static byte[] setBold(boolean dist) {
		byte[] returnText = new byte[3]; // Bold ESC E
		returnText[0] = 0x1B;
		returnText[1] = 0x45;

		if (dist) {
			returnText[2] = 0x01; // Means bold
		} else {
			returnText[2] = 0x00;
		}
		return returnText;
	}

	/***************************************************************************
	 * add by yidie 2012-01-10 Function: Set the absolute printing position. Parameters: int Position the cursor position in the current line. The value range is 0 to 576 points. Description:
	 * In the normal size of the font, each Chinese character is 24 points, and the English character is 12 points. If it is located after the nth Chinese character, position=24*n
	 * If it is after the nth half-width character, position=12*n
	 ****************************************************************************/

	public static byte[] setCusorPosition(int position) {
		byte[] returnText = new byte[4]; // Current line, set absolute printing position ESC $ bL bH
		returnText[0] = 0x1B;
		returnText[1] = 0x24;
		returnText[2] = (byte)position;
		returnText[3] = (byte)(position >> 8);
		return returnText;
	}
	
	public static byte[] PrintBarcode(String stBarcode) {
		int iLength = stBarcode.length() + 4;
		byte[] returnText = new byte[iLength];

		returnText[0] = 0x1D;
		returnText[1] = 'k';
		returnText[2] = 0x45;
		returnText[3] = (byte) stBarcode.length(); // Barcode length

		System.arraycopy(stBarcode.getBytes(), 0, returnText, 4,
				stBarcode.getBytes().length);

		return returnText;
	}

	public static byte[] CutPaper() {
		byte[] returnText = {0x20,0x0A, 0x1D, 0x56, 0x42, 0x00 }; // Cut paper; GS V
																	// 66D 0D
		return returnText;
	}
	
	public static byte[] selfCheck(){
		byte[] returnText = {0x1F, 0x1B, 0x1F, 0x53};
		return returnText;
	}
	
	public static byte[] getPrinterStatus(){
		byte[] data = {0x0A,0x10,0x04,0x01};
		return data;
	}
	
	
    public  static String getHexStringFromBytes(byte[] data){
    	if(data == null || data.length <= 0){
    		return null;
    	}    	
		String hexString = "0123456789ABCDEF";
		int size = data.length * 2;
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < data.length; i++) {
			sb.append(hexString.charAt((data[i] & 0xF0) >> 4));
			sb.append(hexString.charAt((data[i] & 0x0F) >> 0));
		}
		return sb.toString();    	
    }	

	//A QR code
	/**
	* Print QR code
	* @param code:			QR code data
	* @param modulesize:	Two-dimensional code block size (unit: point, value 1 to 16)
	* @param errorlevel:	QR code error correction level (0 to 3)
	*                0 -- Error correction level L (7%)
	*                1 -- Error correction level M (15%)
	*                2 -- Error correction level Q (25%)
	*                3 -- Error correction level H (30%)
	*/
   
	public static byte[] getPrintQRCode(String code, int modulesize, int errorlevel){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(setQRCodeSize(modulesize));
			buffer.write(setQRCodeErrorLevel(errorlevel));
			buffer.write(getQCodeBytes(code));
			buffer.write(getBytesForPrintQRCode(true));
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();				
	}
	
	//Two QR codes
	/**
	* Print QR code
	* @param code1:			QR code data
	* @param code2:			QR code data
	* @param modulesize:	Two-dimensional code block size (unit: point, value 1 to 16)
	* @param errorlevel:	QR code error correction level (0 to 3)
	*                0 -- Error correction level L (7%)
	*                1 -- Error correction level M (15%)
	*                2 -- Error correction level Q (25%)
	*                3 -- Error correction level H (30%)
	*/
	
	public static byte[] getPrintDoubleQRCode(String code1, String code2, int modulesize, int errorlevel){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(setQRCodeSize(modulesize));
			buffer.write(setQRCodeErrorLevel(errorlevel));
			buffer.write(getQCodeBytes(code1));
			buffer.write(getBytesForPrintQRCode(false));
			buffer.write(getQCodeBytes(code2));
			
			//Add horizontal interval
			buffer.write(new byte[]{0x1B, 0x5C, 0x30, 0x00});
			
			buffer.write(getBytesForPrintQRCode(true));
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();				
	}

	//One-dimensional code
	public static byte[] getPrintBarCode(String data, int symbology, 
			int height,	int width, int textposition){

		if(symbology < 0 || symbology > 8){
			return new byte[]{0x0A};
		}
		if(width < 2 || width > 6){
			width = 2;
		}
		if(textposition <0 || textposition > 3){
			textposition = 0;
		}
		if(height < 1 || height>255){
			height = 162;
		}
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(new byte[]{0x1D,0x66,0x01,0x1D,0x48,(byte)textposition,
					0x1D,0x77,(byte)width,0x1D,0x68,(byte)height,0x0A});
			
			byte[] barcode = data.getBytes();
			
			if(symbology == 8){
				buffer.write(new byte[]{0x1D,0x6B,0x49,(byte)(barcode.length+2),0x7B,0x42});
			}else{
				buffer.write(new byte[]{0x1D,0x6B,(byte)(symbology + 0x41),(byte)barcode.length});
			}
			
			buffer.write(barcode);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();				
	}	
	
////////////////////////////////////////////////////////////////////////////////////
//////////////////////////          private                /////////////////////////	
////////////////////////////////////////////////////////////////////////////////////
	

	private static byte[] setQRCodeSize(int modulesize){
		//Two-dimensional code block size setting instruction
		byte[] dtmp = new byte[8];
		dtmp[0] = 0x1D;				
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x43;
		dtmp[7] = (byte)modulesize;		
		return dtmp;
	}
	private static byte[] setQRCodeErrorLevel(int errorlevel){
		//QR code error correction level setting instruction
		byte[] dtmp = new byte[8];
		dtmp[0] = 0x1D;				
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x45;
		dtmp[7] = (byte)(48+errorlevel);
		return dtmp;		
	}
	private static byte[] getBytesForPrintQRCode(boolean single){
		//Print the QR code of the stored data
		byte[] dtmp;
		if(single){		//Only one QRCode is printed on the same line, followed by a line feed
			dtmp = new byte[9];
			dtmp[8] = 0x0A;
		}else{
			dtmp = new byte[8];
		}
		dtmp[0] = 0x1D;				
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x51;
		dtmp[7] = 0x30;					
		return dtmp;
	}
	private static byte[] getQCodeBytes(String code){
		//QR code deposit instruction
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			byte[] d = getGbk(code);
			int len = d.length + 3;
			if(len > 7092)len = 7092;
			buffer.write((byte)0x1D);
			buffer.write((byte)0x28);
			buffer.write((byte)0x6B);
			buffer.write((byte)len);
			buffer.write((byte)(len >> 8));
			buffer.write((byte)0x31);
			buffer.write((byte)0x50);
			buffer.write((byte)0x30);
			for(int i=0; i<d.length && i<len; i++){
				buffer.write(d[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();		
	}    
	/**
	* Take the form picture
	* @param rows: the number of rows in the table
	* @param cols: the number of columns in the table
	* @param size: The side length of each square, in points
	* @return
	*/
	public static Bitmap getTableBitmapFromData(int rows, int cols, int size){
		int[] pixels = createTableData(rows, cols, size);
		return getBitmapFromData(pixels, cols * size, rows * size);
	}

	protected static int[] createTableData(int rows, int cols, int size){
		int width = cols * size;
		int height = rows * size;
		int[] pixels = new int[width * height];
		int k = 0;
		for(int j=0; j<height; j++){
			for(int i=0; i<width; i++){
				if(i==width-1 || j==height-1 || (i % size)==0 || (j % size) == 0){
					pixels[k++] = 0xff000000;
				}else{
					pixels[k++] = 0xffffffff;
				}
			}
		}
		return pixels;
	}	
	protected static Bitmap getBitmapFromData(int[] pixels, int width, int height){
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;		
	}
	
    /**
     * Single character to byte
     * @param c
     * @return
     */
    private static byte charToByte(char c) {  
        return (byte) "0123456789ABCDEF".indexOf(c);  
    } 
    
    /**
     * Hex string to byte array
     * @param hexstring
     * @return
     */
    @SuppressLint("DefaultLocale") 
    public static byte[] getBytesFromHexString(String hexstring){
    	if(hexstring == null || hexstring.equals("")){
    		return null;
    	}
    	hexstring = hexstring.replace(" ", "");
    	hexstring = hexstring.toUpperCase();
    	int size = hexstring.length()/2;
    	char[] hexarray = hexstring.toCharArray();
    	byte[] rv = new byte[size];
    	for(int i=0; i<size; i++){
            int pos = i * 2;  
            rv[i] = (byte) (charToByte(hexarray[pos]) << 4 | charToByte(hexarray[pos + 1]));     		
    	}
    	return rv;
    } 
    
	protected static int[] createLineData(int size, int width){
		int[] pixels = new int[width * (size + 6)];
		int k = 0;
		for(int j=0; j<3; j++){
			for(int i=0; i<width; i++){
				pixels[k++] = 0xffffffff;
			}
		}
		
		for(int j = 0; j < size; j++){
			for(int i=0; i<width; i++){
				pixels[k++] = 0xff000000;
			}
		}
		
		for(int j=0; j<3; j++){
			for(int i=0; i<width; i++){
				pixels[k++] = 0xffffffff;
			}
		}
		return pixels;
	} 
	
	public static byte[] initLine1(int w, int type){
		byte[][] kk = new byte[][]{
				{0x00,0x00,0x7c,0x7c,0x7c,0x00,0x00},
				{0x00,0x00,(byte) 0xff,(byte) 0xff,(byte) 0xff,0x00,0x00},
				{0x00,0x44,0x44,(byte) 0xff,0x44,0x44,0x00},
				{0x00,0x22,0x55,(byte) 0x88,0x55,0x22,0x00},
				{0x08,0x08,0x1c,0x7f,0x1c,0x08,0x08},
				{0x08,0x14,0x22,0x41,0x22,0x14,0x08},
				{0x08,0x14,0x2a,0x55,0x2a,0x14,0x08},
				{0x08,0x1c,0x3e,0x7f,0x3e,0x1c,0x08},
				{0x49,0x22,0x14,0x49,0x14,0x22,0x49},
				{0x63,0x77,0x3e,0x1c,0x3e,0x77,0x63},
				{0x70,0x20,(byte) 0xaf,(byte) 0xaa,(byte) 0xfa,0x02,0x07},
				{(byte) 0xef,0x28,(byte) 0xee,(byte) 0xaa,(byte) 0xee,(byte) 0x82,(byte) 0xfe},
				};
		
		int ww = (w + 7)/8;

		byte[] data = new byte[ 13 * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = 13;  //Height 13
		data[7] = 0;
		
		int k = 8;
		for(int i=0; i < 3 * ww; i++){
				data[k++] = 0;
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][0];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][1];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][2];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][3];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][4];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][5];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][6];
		}		
		for(int i=0; i < 3 * ww; i++){
			data[k++] = 0;
		}
		return data;
	}
	public static byte[] initLine2(int w){
		int ww = (w + 7)/8;

		byte[] data = new byte[ 12 * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = 12;  //Height 13
		data[7] = 0;
		
		int k = 8;
		for(int i=0; i < 5 * ww; i++){
				data[k++] = 0;
		}
		for(int i=0; i < ww; i++){
			data[k++] = 0x7f;
		}
		for(int i=0; i < ww; i++){
			data[k++] = 0x7f;
		}
		for(int i=0; i < 5 * ww; i++){
			data[k++] = 0;
		}
		return data;
	}	
	
	public static Bitmap getLineBitmapFromData(int size, int width){
		int[] pixels = createLineData(size, width);
		return getBitmapFromData(pixels, width, size + 6);
	}
	private static byte getBitMatrixColor(BitMatrix bits, int x, int y){
		int width = bits.getWidth();
		int height = bits.getHeight();
		if( x >= width || y >= height || x < 0 || y < 0)return 0;
		if(bits.get(x, y)){
			return 1;
		}else{
			return 0;
		}
	}
	
	public static byte[] getBytesFromBitMatrix(BitMatrix bits){
		if(bits == null)return null;
		
		int h = bits.getHeight();
		int w = (bits.getWidth()+7) / 8;
		byte[] rv = new byte[h * w + 8];
		
		rv[0] = 0x1D;
		rv[1] = 0x76;
		rv[2] = 0x30;
		rv[3] = 0x00;
		
		rv[4] = (byte)w;//xL
		rv[5] = (byte)(w >> 8);//xH
		rv[6] = (byte)h;
		rv[7] = (byte)(h >> 8);	
		
		int k = 8;
		for(int i=0; i<h; i++){
			for(int j=0; j<w; j++){
				for(int n=0; n<8; n++){
					byte b = getBitMatrixColor(bits, j * 8 + n, i);
					rv[k] += rv[k] + b;					
				}
				k++;
			}
		}		
		return rv;
	}
	
	public static byte[] getZXingQRCode(String data, int size){
        try {        
        	Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        	hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        	//Image data conversion, using matrix conversion
			BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints);
			System.out.println("bitmatrix height:" + bitMatrix.getHeight() + " width:" + bitMatrix.getWidth());
			return getBytesFromBitMatrix(bitMatrix);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getHorizontalLine(int w,  int size, int type){
		int ww = (w + 7)/8;
		int hh = size + 6;
		
		byte kk = (byte)0xff;	//solid line
		
		if(type == 0){
			kk = 0x3f;			//dotted line
		}
		
		byte[] data = new byte[ hh * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = (byte)hh;  //height
		data[7] = (byte)(hh >> 8);
		
		int k = 8;
		for(int i=0; i < 3 * ww; i++){
				data[k++] = 0;
		}
		for(int j=0; j < size; j++){
			for(int i=0; i < ww; i++){
				data[k++] = kk;
			}
		}
		for(int i=0; i < 3 * ww; i++){
			data[k++] = 0;
		}
		return data;
	}		
}
