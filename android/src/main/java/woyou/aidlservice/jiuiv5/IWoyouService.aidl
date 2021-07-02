//P, V series

package woyou.aidlservice.jiuiv5;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.ITax;
import android.graphics.Bitmap;
import com.sunmi.trans.TransBean;

interface IWoyouService
{	
	/**
	* Replace the original printer upgrade firmware interface (void updateFirmware())
	* Now changed to the data interface of the load package name, only the system call
	* Supported version: above 4.0.0
	*/
    boolean postPrintData(String packageName, in byte[] data, int offset, int length);

	/**
	* Printer firmware status
	* Return: 0--unknown, A5--bootloader, C3--print
	*/
	int getFirmwareStatus();
	
	/**
    * Get print service version
    * Back: WoyouService service version
    */
	String getServiceVersion();	
	
	/**
	* Initialize the printer, reset the logic program of the printer, but do not clear the data in the buffer area, so
	* Incomplete print jobs will continue after reset
	*/
	void printerInit(in ICallback callback);
			
	/**
	* Printer self-test, the printer will print a self-test page
	*/
	void printerSelfChecking(in ICallback callback);
	
	/**
	* Get the serial number of the printer board
	* Return: the serial number of the printer board
	*/	
	String getPrinterSerialNo();
	
	/**
	* Get the printer firmware version number
	* Return: Printer firmware version number
	*/
	String getPrinterVersion();	
	
	/**
	* Get printer model
	* Return: Printer model
	*/		
	String getPrinterModal();
	
	/**
	* Get the print length after the printer is powered on
	* callback onReturnString returns
	*/
	void getPrintedLength(in ICallback callback);
		
	/**
	* The printer feeds paper (mandatory line feed, paper feeds n lines after finishing the previous printing content)
	* n: Number of paper lines
	*/
	void lineWrap(int n, in ICallback callback);
				
	/**
	* epson command printing
	*/
	void sendRAWData(in byte[] data, in ICallback callback);
	
	/**
	* Setting the alignment mode will affect subsequent printing, unless initialized
	* Alignment: alignment 0--left, 1--center, 2--right
	*/
	void setAlignment(int alignment, in ICallback callback);

	/**
	* Set the print font, temporarily it can only be called by the system, and the external call is invalid
	*/
	void setFontName(String typeface, in ICallback callback);
	
	/**
	* Setting the font size will affect subsequent printing, unless initialized
	* Note: The font size is a printing method that exceeds the standard international directives.
	* Adjusting the font size will affect the character width, and the number of characters in each line will also change accordingly.
	* Therefore, the typesetting formed by monospaced fonts may be messy
	* fontsize: font size
	*/
	void setFontSize(float fontsize, in ICallback callback);
	
	/**
	* Print the text, the width of the text is full of one line, it will automatically wrap and type, and the whole line will not be printed unless it is forced to wrap.
	* text: the text string to be printed
	*/
	void printText(String text, in ICallback callback);

	/**
	* Print the text in the specified font, the font setting is only valid for this time
	* text: the text to be printed
    * typeface: font name (for the time being, it can only be called by the system, and the external call is invalid)
    * fontsize: font size
    */
	void printTextWithFont(String text, String typeface, float fontsize, in ICallback callback);

	/**
	* Print a row of the table, you can specify the column width and alignment
	* colsTextArr: array of text strings for each column
	* colsWidthArr: array of column widths (calculated in English characters, each Chinese character occupies two English characters, each width is greater than 0)
	* colsAlign: Alignment of each column (0 to the left, 1 to the center, 2 to the right)
	* Note: The length of the array of the three parameters should be the same, if the width of colsText[i] is greater than colsWidth[i], the text will wrap
	*/
	void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);

	/**
	* Print pictures
	* bitmap: The maximum width is 384 pixels, if the width is exceeded, the display will be incomplete; the size of the picture length * width <8M;
	*/
	void printBitmap(in Bitmap bitmap, in ICallback callback);
	
	/**
	* Print one-dimensional barcode
	* data: barcode data
	* symbology: barcode type
	* 0 - UPC-A, requires 12 digits (the last check digit must be correct), but it is limited by the width of the printer and the width of the barcode
	* 1 - UPC-E, requires 8 digits (the last check digit must be correct), but it is limited by the width of the printer and the width of the barcode
	* 2 - JAN13 (EAN13), requires 13 digits (the last check digit must be correct), but it is limited by the width of the printer and the width of the barcode
	* 3 - JAN8 (EAN8), requires 8 digits (the last check digit must be correct), but it is limited by the width of the printer and the width of the barcode
	* 4 - CODE39, numbers in English and 8 special symbols with * at the beginning and end, but it is limited by the width of the printer and the width of the bar code
	* 5 - ITF, the characters are numbers and less than 14 digits, but are limited by the width of the printer and the width of the barcode
	* 6 - CODABAR, the start and end must be A-D, the data is 0-9 and 6 special characters, the length is arbitrary but is limited by the width of the printer and the width of the barcode
	* 7 - CODE93, any character, any length but limited by the width of the printer and the width of the bar code
	* 8 - CODE128 can have any characters and length but is limited by the width of the printer and the width of the barcode
	* height: barcode height, value 1 to 255, default 162
	* width: barcode width, value 2 to 6, default 2
	* textposition: text position 0--do not print text, 1--text above the bar code, 2--text below the bar code, 3--print both above and below the bar code
	*/
	void printBarCode(String data, int symbology, int height, int width, int textposition,  in ICallback callback);
		
	/**
	* Print two-dimensional barcode
	* data: QR code data
	* modulesize: QR code block size (unit: point, value 1 to 16)
	* errorlevel: QR code error correction level (0 to 3),
	* 0 - Error correction level L (7%),
	* 1 - Error correction level M (15%),
	* 2 - Error correction level Q (25%),
	* 3 - Error correction level H (30%)
	*/
	void printQRCode(String data, int modulesize, int errorlevel, in ICallback callback);
	
	/**
	* Print the text, the width of the text is full of one line, it will automatically wrap and type, and the whole line will not be printed unless it is forced to wrap.
	* The text is output as the vector text width, that is, the width of each character is not equal
	* text: the text string to be printed
	*/
	void printOriginalText(String text, in ICallback callback);	
	
	/**
	* lib package printing dedicated interface
	* transbean: print task list
	*/
	void commitPrint(in TransBean[] transbean, in ICallback callback);
	
	/**
	* Print buffer content
	*/
	void commitPrinterBuffer();

	/**
	* Enter transaction mode, all print calls will be cached;
	* Call commitPrinterBuffe(), exitPrinterBuffer(true), commitPrinterBufferWithCallback(),
	* Print only after exitPrinterBufferWithCallback(true);
	* clean: If you have not exited the transaction mode before, whether to clear the cached buffer content
	*/
	void enterPrinterBuffer(in boolean clean);
	
	/**
	* Exit buffer mode
	* commit: whether to print out the buffer content
	*/
	void exitPrinterBuffer(in boolean commit);

	/**
	* Send CNC commands
	* data: tax control order
	*/
	void tax(in byte [] data,in ITax callback); 

	/**
	* Get the model of the printer head
	* callback onReturnString returns
	*/
	void getPrinterFactory(in ICallback callback); 

    /**
	* Clear printer cache data (system call only, external call is invalid)
	*/
	void clearBuffer(); 
	
	/**
	* Print buffer content with feedback
	*/
	void commitPrinterBufferWithCallback(in ICallback callback);
	
	/**
	* Exit buffer printing mode with feedback
	* commit: whether to submit the contents of the buffer
	*/
	void exitPrinterBufferWithCallback(in boolean commit, in ICallback callback);
	
	/**
	* Print a row of the table, you can specify the column width and alignment
	* colsTextArr: array of text strings for each column
	* colsWidthArr: The width weight of each column is the proportion of each column
	* colsAlign: Alignment of each column (0 to the left, 1 to the center, 2 to the right)
	* Note: The length of the array of the three parameters should be the same, if the width of colsText[i] is greater than colsWidth[i], the text will wrap
	*/
	void printColumnsString(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);
	
	/**
	* Get the latest status of the printer
	* Return: Printer status feedback 1 Normal 2 In preparation 3 Abnormal communication 4 Out of paper 5 Overheating 505: No printer 507: Update failure
	*/
	int updatePrinterState();

    /**
	* Custom print pictures
	* bitmap: image bitmap object (maximum width is 384 pixels, the image cannot be printed if it exceeds 1M)
	* type: There are currently two printing methods: 0, the same as printBitmap 1, a black and white image with a threshold of 200, 2, a grayscale image
	* Supported version: P1-v3.2.0 and above
	* P14g-v1.1.6 and above
	* V1s-v3.1.6 and above
	* V2-v1.0.0 and above
	*/
    void printBitmapCustom(in Bitmap bitmap, in int type, in ICallback callback);

    /**
	* Obtain the forced font doubling status
	* Return 0: Not enabled 1: Double width 2: Double height 3: Double height and double width
	* Supported version: P1-v3.2.0 and above
	* P14g-v1.2.0 and above
	* V1s-v3.2.0 and above
	* V2-v1.0.0 and above
	*/
    int getForcedDouble();

    /**
	* Whether to force the anti-white style to be enabled
	* Return true: enable false: not enable
	* Supported version: P1-v3.2.0 and above
	* P14g-v1.2.0 and above
	* V1s-v3.2.0 and above
	* V2-v1.0.0 and above
	*/
    boolean isForcedAntiWhite();

    /**
	* Whether to force the bold style to be enabled
	* Return true: enable false: not enable
	* Supported version: P1-v3.2.0 and above
	* P14g-v1.2.0 and above
	* V1s-v3.2.0 and above
	* V2-v1.0.0 and above
	*/
    boolean isForcedBold();

    /**
	* Whether to force the underline style to be enabled
	* Return true: enable false: not enable
	* Supported version: P1-v3.2.0 and above
	* P14g-v1.2.0 and above
	* V1s-v3.2.0 and above
	* V2-v1.0.0 and above
	*/
    boolean isForcedUnderline();

    /**
	* Obtain the mandatory row height status
	* Return -1: Not enabled 0~255: Force line height pixel height
	* Supported version: P1-v3.2.0 and above
	* P14g-v1.2.0 and above
	* V1s-v3.2.0 and above
	* V2-v1.0.0 and above
	*/
    int getForcedRowHeight();

    /**
	* Get the current font
	* Return 0: Sunmi font 1.0 1: Sunmi font 2.0
	* Supported version: P1-v3.2.0 and above
	* P14g-v1.2.0 and above
	* V1s-v3.2.0 and above
	* V2-v1.0.0 and above
	*/
    int getFontName();
}