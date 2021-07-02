package com.sunmi.innerprinter;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ESCUtil {

	public static final byte ESC = 27;// Escape code
	public static final byte FS = 28;// Text separator
	public static final byte GS = 29;// Group separator
	public static final byte DLE = 16;// Data connection escape
	public static final byte EOT = 4;// End of transmission
	public static final byte ENQ = 5;// Query character
	public static final byte SP = 32;// Space
	public static final byte HT = 9;// Horizontal list
	public static final byte LF = 10;// Print and wrap (horizontal positioning)
	public static final byte CR = 13;// Home key
	public static final byte FF = 12;// Paper feeding control (print and return to standard mode (in page mode))
	public static final byte CAN = 24;// Void (cancel print data in page mode)

	// ------------------------Printer initialization-----------------------------

	private static String hexStr = "0123456789ABCDEF";
	private static String[] binaryArray = { "0000", "0001", "0010", "0011",
			"0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
			"1100", "1101", "1110", "1111" };

private static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode(new String(new byte[] { src0 })).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode(new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 | _b1);
		byte aret = Byte.decode("0x" + ret).byteValue();

		return aret;
	}
	public static String binaryStrToHexString(String binaryStr) {
		String hex = "";
		String f4 = binaryStr.substring(0, 4);
		String b4 = binaryStr.substring(4, 8);
		for (int i = 0; i < binaryArray.length; i++) {
			if (f4.equals(binaryArray[i]))
				hex += hexStr.substring(i, i + 1);
		}
		for (int i = 0; i < binaryArray.length; i++) {
			if (b4.equals(binaryArray[i]))
				hex += hexStr.substring(i, i + 1);
		}

		return hex;
	}
	
		public static byte[] HexStringToBinary(String hexString) {		
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;
		byte low = 0;
		for (int i = 0; i < len; i++) {			
			high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high & 0xF0 | low & 0x0F);
		}
		return bytes;
	}

	public static List<String> binaryListToHexStringList(List<String> list) {
		List<String> hexList = new ArrayList<String>();
		for (String binaryStr : list) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < binaryStr.length(); i += 8) {
				String str = binaryStr.substring(i, i + 8);
				String hexString = binaryStrToHexString(str);
				sb.append(hexString);
			}
			hexList.add(sb.toString());
		}
		return hexList;

	}
	public static byte[] sysCopy(List<byte[]> srcArrays) {
		int len = 0;
		for (byte[] srcArray : srcArrays) {
			len += srcArray.length;
		}
		byte[] destArray = new byte[len];
		int destLen = 0;
		for (byte[] srcArray : srcArrays) {
			System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
		return destArray;
	}

	public static byte[] hexList2Byte(List<String> list) {
		List<byte[]> commandList = new ArrayList<byte[]>();
		for (String hexStr : list) {
			commandList.add(HexStringToBinary(hexStr));
		}
		byte[] bytes = sysCopy(commandList);
		return bytes;
	}

	/**
	 * Printer initialization
	 * 
	 * @return
	 */
	public static byte[] init_printer() {
		byte[] result = new byte[2];
		result[0] = ESC;
		result[1] = 64;
		return result;
	}

	// ------------------------Wrap-----------------------------

	/**
	 * Wrap
	 * 
	 * @param lineNum要换几行
	 * @return
	 */
	public static byte[] nextLine(int lineNum) {
		byte[] result = new byte[lineNum];
		for (int i = 0; i < lineNum; i++) {
			result[i] = LF;
		}

		return result;
	}

	// ------------------------Underscore-----------------------------

	/**
	 * Draw an underline (1 point wide)
	 * 
	 * @return
	 */
	public static byte[] underlineWithOneDotWidthOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 1;
		return result;
	}

	/**
	 * Draw an underline (2 points wide)
	 * 
	 * @return
	 */
	public static byte[] underlineWithTwoDotWidthOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 2;
		return result;
	}

	/**
	 * Cancel underline
	 * 
	 * @return
	 */
	public static byte[] underlineOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 0;
		return result;
	}

	// ------------------------Bold-----------------------------

	/**
	 * Select bold mode
	 * 
	 * @return
	 */
	public static byte[] boldOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0xF;
		return result;
	}

	/**
	 * Cancel bold mode
	 * 
	 * @return
	 */
	public static byte[] boldOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0;
		return result;
	}

	// ------------------------Align-----------------------------

	/**
	 * Align left
	 * 
	 * @return
	 */
	public static byte[] alignLeft() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 0;
		return result;
	}

	/**
	 * Align center
	 * 
	 * @return
	 */
	public static byte[] alignCenter() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 1;
		return result;
	}

	/**
	 * Align right
	 * 
	 * @return
	 */
	public static byte[] alignRight() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 2;
		return result;
	}

	/**
	 * Move the col column to the right in the horizontal direction
	 * 
	 * @param col
	 * @return
	 */
	public static byte[] set_HT_position(byte col) {
		byte[] result = new byte[4];
		result[0] = ESC;
		result[1] = 68;
		result[2] = col;
		result[3] = 0;
		return result;
	}
	// ------------------------Font becomes bigger-----------------------------

	/**
	 * The font size becomes n times larger than the standard
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] fontSizeSetBig(int num) {
		byte realSize = 0;
		switch (num) {
		case 1:
			realSize = 0;
			break;
		case 2:
			realSize = 17;
			break;
		case 3:
			realSize = 34;
			break;
		case 4:
			realSize = 51;
			break;
		case 5:
			realSize = 68;
			break;
		case 6:
			realSize = 85;
			break;
		case 7:
			realSize = 102;
			break;
		case 8:
			realSize = 119;
			break;
		}
		byte[] result = new byte[3];
		result[0] = 29;
		result[1] = 33;
		result[2] = realSize;
		return result;
	}

	// ------------------------Font size becomes smaller-----------------------------

	/**
	 * Cancel double width and double height
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] fontSizeSetSmall(int num) {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 33;

		return result;
	}

	// ------------------------Cut paper-----------------------------

	/**
	 * Feed the paper and cut all
	 * 
	 * @return
	 */
	public static byte[] feedPaperCutAll() {
		byte[] result = new byte[4];
		result[0] = GS;
		result[1] = 86;
		result[2] = 65;
		result[3] = 0;
		return result;
	}

	/**
	 * Feed the paper and cut (leave a little left uncut)
	 * 
	 * @return
	 */
	public static byte[] feedPaperCutPartial() {
		byte[] result = new byte[4];
		result[0] = GS;
		result[1] = 86;
		result[2] = 66;
		result[3] = 0;
		return result;
	}

	// ------------------------Cut paper-----------------------------
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	public static byte[] byteMerger(byte[][] byteList) {

		int length = 0;
		for (int i = 0; i < byteList.length; i++) {
			length += byteList[i].length;
		}
		byte[] result = new byte[length];

		int index = 0;
		for (int i = 0; i < byteList.length; i++) {
			byte[] nowByte = byteList[i];
			for (int k = 0; k < byteList[i].length; k++) {
				result[index] = nowByte[k];
				index++;
			}
		}
		for (int i = 0; i < index; i++) {
			// CommonUtils.LogWuwei("", "result[" + i + "] is " + result[i]);
		}
		return result;
	}

	// --------------------
	public static byte[] generateMockData() {
		try {
			byte[] next2Line = ESCUtil.nextLine(2);
			byte[] title = "Menu (lunch) ** Wantong Center Store".getBytes("gb2312");

			byte[] boldOn = ESCUtil.boldOn();
			byte[] fontSize2Big = ESCUtil.fontSizeSetBig(3);
			byte[] center = ESCUtil.alignCenter();
			byte[] Focus = "Net 507".getBytes("gb2312");
			byte[] boldOff = ESCUtil.boldOff();
			byte[] fontSize2Small = ESCUtil.fontSizeSetSmall(3);

			byte[] left = ESCUtil.alignLeft();
			byte[] orderSerinum = "Order number: 11234".getBytes("gb2312");
			boldOn = ESCUtil.boldOn();
			byte[] fontSize1Big = ESCUtil.fontSizeSetBig(2);
			byte[] FocusOrderContent = "Leek and Egg Dumplings-Small (Single)".getBytes("gb2312");
			boldOff = ESCUtil.boldOff();
			byte[] fontSize1Small = ESCUtil.fontSizeSetSmall(2);

			next2Line = ESCUtil.nextLine(2);

			byte[] priceInfo = "Receivable: 22 yuan discount: 2.5 yuan".getBytes("gb2312");
			byte[] nextLine = ESCUtil.nextLine(1);

			byte[] priceShouldPay = "Actual collection: RMB 19.5".getBytes("gb2312");
			nextLine = ESCUtil.nextLine(1);

			byte[] takeTime = "Meal picking time: 2015-02-13 12:51:59".getBytes("gb2312");
			nextLine = ESCUtil.nextLine(1);
			byte[] setOrderTime = "Order time: 2015-02-13 12:35:15".getBytes("gb2312");

			byte[] tips_1 = "Follow on WeChat \"**\" Self-service orders are free of 1 yuan per day".getBytes("gb2312");
			nextLine = ESCUtil.nextLine(1);
			byte[] tips_2 = "After the meal, comment again and award 5 cents".getBytes("gb2312");
			byte[] next4Line = ESCUtil.nextLine(4);

			byte[] breakPartial = ESCUtil.feedPaperCutPartial();

			byte[][] cmdBytes = { title, nextLine, center, boldOn, fontSize2Big, Focus, boldOff, fontSize2Small,
					next2Line, left, orderSerinum, nextLine, center, boldOn, fontSize1Big, FocusOrderContent, boldOff,
					fontSize1Small, nextLine, left, next2Line, priceInfo, nextLine, priceShouldPay, next2Line, takeTime,
					nextLine, setOrderTime, next2Line, center, tips_1, nextLine, center, tips_2, next4Line,
					breakPartial };

			return ESCUtil.byteMerger(cmdBytes);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decodeBitmap(byte[] bitmapBytes) {

    Bitmap bmp = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

    int zeroCount = bmp.getWidth() % 8;
    String zeroStr = "";
    if (zeroCount > 0) {
        for (int i = 0; i < (8 - zeroCount); i++) {
            zeroStr = zeroStr + "0";
        }
    }

    List<String> list = new ArrayList<>();
    for (int i = 0; i < bmp.getHeight(); i++) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < bmp.getWidth(); j++) {
            int color = bmp.getPixel(j, i);

            int r = (color >> 16) & 0xff;
            int g = (color >> 8) & 0xff;
            int b = color & 0xff;

            // if color close to white，bit='0', else bit='1'
            if (r > 160 && g > 160 && b > 160)
                sb.append("0");
            else
                sb.append("1");
        }
        if (zeroCount > 0) {
            sb.append(zeroStr);
        }

        list.add(sb.toString());
    }

    List<String> bmpHexList = binaryListToHexStringList(list);
    List<String> commandList = new ArrayList<>();
    commandList.addAll(bmpHexList);

    return hexList2Byte(commandList);
}
public static byte[] decodeBitmap2(byte[] bitmapBytes) {
    Bitmap bmp = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
	return draw2PxPoint(bmp);
}

public static byte[] draw2PxPoint(Bitmap bmp) {
        //Used to store the converted bitmap data. Why add another 1000? This is to deal with when the picture height cannot be
        // Divide the situation at 24 o'clock. For example, the resolution of bitmap is 240 * 250, which occupies 7500 bytes,
        //But actually to store 11 rows of data, each row needs 24 * 240/8 = 720byte of space. Coupled with the overhead of some instruction storage,
        //So it is safe to apply for an additional 1000byte of space, otherwise an out-of-bounds array access exception will be thrown during runtime.
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
        byte[] data = new byte[size];
        int k = 0;
        //Command to set line spacing to 0
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // Print line by line
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            //Instructions for printing pictures
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); //nL
            data[k++] = (byte) (bmp.getWidth() / 256); //nH
            //For each row, print column by column
            for (int i = 0; i < bmp.getWidth(); i++) {
                //24 pixels in each column, divided into 3 bytes for storage
                for (int m = 0; m < 3; m++) {
                    //Each byte represents 8 pixels, 0 means white, 1 means black
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
            data[k++] = 10;//Wrap
        }
        return data;
    }
    /**
     * Grayscale pictures are black and white, black is 1, white is 0
     *
     * @param x   Abscissa
     * @param y   Y-axis
     * @param bit bitmap
     * @return
     */
    public static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // Take the two highest
            int green = (pixel & 0x0000ff00) >> 8; // Take two
            int blue = pixel & 0x000000ff; // Take the lower two
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }
     /**
     * Image gray scale conversion
     */
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //Gray conversion formula
        return gray;
    }
}


