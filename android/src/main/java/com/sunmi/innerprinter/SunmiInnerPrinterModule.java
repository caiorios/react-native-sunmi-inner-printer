package com.sunmi.innerprinter;

import android.content.BroadcastReceiver;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;
import android.widget.Toast;

import java.util.Map;
import java.io.IOException;

import woyou.aidlservice.jiuiv5.IWoyouService;
import woyou.aidlservice.jiuiv5.ICallback;
import android.os.RemoteException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.graphics.Bitmap;

import java.nio.charset.StandardCharsets;

import android.util.Log;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.content.IntentFilter;

import java.util.Map;
import java.util.HashMap;

public class SunmiInnerPrinterModule extends ReactContextBaseJavaModule {
    public static ReactApplicationContext reactApplicationContext = null;
    private IWoyouService woyouService;
    private BitmapUtils bitMapUtils;
    private PrinterReceiver receiver = new PrinterReceiver();

    // Out of paper abnormal
    public final static String OUT_OF_PAPER_ACTION = "woyou.aidlservice.jiuv5.OUT_OF_PAPER_ACTION";
    // Print error
    public final static String ERROR_ACTION = "woyou.aidlservice.jiuv5.ERROR_ACTION";
    // Can print
    public final static String NORMAL_ACTION = "woyou.aidlservice.jiuv5.NORMAL_ACTION";
    // Open the lid
    public final static String COVER_OPEN_ACTION = "woyou.aidlservice.jiuv5.COVER_OPEN_ACTION";
    // Abnormal lid closing
    public final static String COVER_ERROR_ACTION = "woyou.aidlservice.jiuv5.COVER_ERROR_ACTION";
    // Cutter abnormal 1-card cutter
    public final static String KNIFE_ERROR_1_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_1";
    // Cutter abnormal 2-cutter repair
    public final static String KNIFE_ERROR_2_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_2";
    // The print head is overheated abnormally
    public final static String OVER_HEATING_ACITON = "woyou.aidlservice.jiuv5.OVER_HEATING_ACITON";
    // 打印机固件开始升级
    public final static String FIRMWARE_UPDATING_ACITON = "woyou.aidlservice.jiuv5.FIRMWARE_UPDATING_ACITON";

    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service disconnected: " + name);
            woyouService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Service connected: " + name);
            woyouService = IWoyouService.Stub.asInterface(service);
        }
    };

    private static final String TAG = "SunmiInnerPrinterModule";

    public SunmiInnerPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        reactContext.startService(intent);
        reactContext.bindService(intent, connService, Context.BIND_AUTO_CREATE);
        bitMapUtils = new BitmapUtils(reactContext);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(OUT_OF_PAPER_ACTION);
        mFilter.addAction(ERROR_ACTION);
        mFilter.addAction(NORMAL_ACTION);
        mFilter.addAction(COVER_OPEN_ACTION);
        mFilter.addAction(COVER_ERROR_ACTION);
        mFilter.addAction(KNIFE_ERROR_1_ACTION);
        mFilter.addAction(KNIFE_ERROR_2_ACTION);
        mFilter.addAction(OVER_HEATING_ACITON);
        mFilter.addAction(FIRMWARE_UPDATING_ACITON);
        getReactApplicationContext().registerReceiver(receiver, mFilter);
        Log.d("PrinterReceiver", "------------ init ");
    }

    @Override
    public String getName() {
        return "SunmiInnerPrinter";
    }


    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        final Map<String, Object> constantsChildren = new HashMap<>();

        constantsChildren.put("OUT_OF_PAPER_ACTION", OUT_OF_PAPER_ACTION);
        constantsChildren.put("ERROR_ACTION", ERROR_ACTION);
        constantsChildren.put("NORMAL_ACTION", NORMAL_ACTION);
        constantsChildren.put("COVER_OPEN_ACTION", COVER_OPEN_ACTION);
        constantsChildren.put("COVER_ERROR_ACTION", COVER_ERROR_ACTION);
        constantsChildren.put("KNIFE_ERROR_1_ACTION", KNIFE_ERROR_1_ACTION);
        constantsChildren.put("KNIFE_ERROR_2_ACTION", KNIFE_ERROR_2_ACTION);
        constantsChildren.put("OVER_HEATING_ACITON", OVER_HEATING_ACITON);
        constantsChildren.put("FIRMWARE_UPDATING_ACITON", FIRMWARE_UPDATING_ACITON);

        constants.put("Constants", constantsChildren);

        constants.put("hasPrinter", hasPrinter());

        try {
            constants.put("printerVersion", getPrinterVersion());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerSerialNo", getPrinterSerialNo());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerModal", getPrinterModal());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }

        return constants;
    }


    /**
     * Initialize the printer, reset the logic program of the printer, but do not clear the data in the buffer area, so
     * Incomplete print jobs will continue after reset
     *
     * @return
     */
    @ReactMethod
    public void printerInit(final Promise p) {
        final IWoyouService printerService = woyouService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printerInit(new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Printer self-test, the printer will print a self-test page
     *
     * @param callback 回调
     */
    @ReactMethod
    public void printerSelfChecking(final Promise p) {
        final IWoyouService printerService = woyouService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printerSelfChecking(new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Get the serial number of the printer board
     */
    @ReactMethod
    public void getPrinterSerialNo(final Promise p) {
        try {
            p.resolve(getPrinterSerialNo());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterSerialNo() throws Exception {
        final IWoyouService printerService = woyouService;
        return printerService.getPrinterSerialNo();
    }

    /**
     * Get the printer firmware version number
     */
    @ReactMethod
    public void getPrinterVersion(final Promise p) {
        try {
            p.resolve(getPrinterVersion());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterVersion() throws Exception {
        final IWoyouService printerService = woyouService;
        return printerService.getPrinterVersion();
    }

    /**
     * Get printer model
     */
    @ReactMethod
    public void getPrinterModal(final Promise p) {
        try {
            p.resolve(getPrinterModal());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterModal() throws Exception {
        //Caution: This method is not fully test -- Januslo 2018-08-11
        final IWoyouService printerService = woyouService;
        return printerService.getPrinterModal();
    }

    @ReactMethod
    public void hasPrinter(final Promise p) {
        try {
            p.resolve(hasPrinter());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    /**
     * Is there a printer service
     * return {boolean}
     */
    private boolean hasPrinter() {
        final IWoyouService printerService = woyouService;
        final boolean hasPrinterService = printerService != null;
        return hasPrinterService;
    }

    /**
     * Get the print length of the print head
     */
    @ReactMethod
    public void getPrintedLength(final Promise p) {
        final IWoyouService printerService = woyouService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.getPrintedLength(new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Printer paper feed (forced line feed, paper feed n lines after finishing the previous printing content)
     *
     * @param n:       Number of paper rows
     * @param callback Result callback
     * @return
     */
    @ReactMethod
    public void lineWrap(int n, final Promise p) {
        final IWoyouService ss = woyouService;
        final int count = n;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.lineWrap(count, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Print using original instructions
     *
     * @param data     instruction
     * @param callback Result callback
     */
    @ReactMethod
    public void sendRAWData(String base64EncriptedData, final Promise p) {
        final IWoyouService ss = woyouService;
        final byte[] d = Base64.decode(base64EncriptedData, Base64.DEFAULT);
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.sendRAWData(d, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Set the alignment mode, which will affect subsequent printing, unless initialized
     *
     * @param alignment: Alignment 0--left, 1--center, 2--right
     * @param callback   Result callback
     */
    @ReactMethod
    public void setAlignment(int alignment, final Promise p) {
        final IWoyouService ss = woyouService;
        final int align = alignment;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.setAlignment(align, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Setting the print font will affect subsequent printing unless it is initialized
     * (Currently only one font "gh" is supported, gh is a monospaced Chinese font, and more font options will be provided later)
     *
     * @param typeface: Font name
     */
    @ReactMethod
    public void setFontName(String typeface, final Promise p) {
        final IWoyouService ss = woyouService;
        final String tf = typeface;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.setFontName(tf, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Setting the font size will affect subsequent printing, unless initialized
     * Note: The font size is a printing method that exceeds the standard international directives.
     * Adjusting the font size will affect the character width, and the number of characters in each line will also change accordingly.
     * Therefore, the typesetting formed by monospaced fonts may be messy
     *
     * @param fontsize: font size
     */
    @ReactMethod
    public void setFontSize(float fontsize, final Promise p) {
        final IWoyouService ss = woyouService;
        final float fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.setFontSize(fs, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }


    /**
     * Print the text in the specified font, the font setting is only valid for this time
     *
     * @param text:     To print text
     * @param typeface: Font name (currently only "gh" font is supported)
     * @param fontsize: font size
     */
    @ReactMethod
    public void printTextWithFont(String text, String typeface, float fontsize, final Promise p) {
        final IWoyouService ss = woyouService;
        final String txt = text;
        final String tf = typeface;
        final float fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.printTextWithFont(txt, tf, fs, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Print a row of the table, you can specify the column width and alignment
     *
     * @param colsTextArr  Array of text strings for each column
     * @param colsWidthArr Array of column width (calculated in English characters, each Chinese character occupies two English characters, and each width is greater than 0)
     * @param colsAlign    Alignment of each column (0 to the left, 1 to the center, 2 to the right)
     *                     Remarks: The length of the array of the three parameters should be the same, if the width of colsText[i] is greater than colsWidth[i], the text will wrap
     */
    @ReactMethod
    public void printColumnsText(ReadableArray colsTextArr, ReadableArray colsWidthArr, ReadableArray colsAlign, final Promise p) {
        final IWoyouService ss = woyouService;
        final String[] clst = new String[colsTextArr.size()];
        for (int i = 0; i < colsTextArr.size(); i++) {
            clst[i] = colsTextArr.getString(i);
        }
        final int[] clsw = new int[colsWidthArr.size()];
        for (int i = 0; i < colsWidthArr.size(); i++) {
            clsw[i] = colsWidthArr.getInt(i);
        }
        final int[] clsa = new int[colsAlign.size()];
        for (int i = 0; i < colsAlign.size(); i++) {
            clsa[i] = colsAlign.getInt(i);
        }
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.printColumnsText(clst, clsw, clsa, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }


    /**
     * Print picture
     *
     * @param bitmap: Picture bitmap object (maximum width of 384 pixels, unable to print and callback abnormal function)
     */
    @ReactMethod
    public void printBitmap(String data, int width, int height, final Promise p) {
        try {
            final IWoyouService ss = woyouService;
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            final Bitmap bitMap = bitMapUtils.decodeBitmap(decoded, width, height);
            ThreadPoolManager.getInstance().executeTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        ss.printBitmap(bitMap, new ICallback.Stub() {
                            @Override
                            public void onPrintResult(int par1, String par2) {
                                Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                            }

                            @Override
                            public void onRunResult(boolean isSuccess) {
                                if (isSuccess) {
                                    p.resolve(null);
                                } else {
                                    p.reject("0", isSuccess + "");
                                }
                            }

                            @Override
                            public void onReturnString(String result) {
                                p.resolve(result);
                            }

                            @Override
                            public void onRaiseException(int code, String msg) {
                                p.reject("" + code, msg);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "ERROR: " + e.getMessage());
                        p.reject("" + 0, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
    }

    /**
     * Print one-dimensional barcode
     *
     * @param data:         Barcode data
     * @param symbology:    Barcode type
     *                      0 -- UPC-A，
     *                      1 -- UPC-E，
     *                      2 -- JAN13(EAN13)，
     *                      3 -- JAN8(EAN8)，
     *                      4 -- CODE39，
     *                      5 -- ITF，
     *                      6 -- CODABAR，
     *                      7 -- CODE93，
     *                      8 -- CODE128
     * @param height:       Barcode height, value 1 to 255, default 162
     * @param width:        Barcode width, value 2 to 6, default 2
     * @param textposition: Text position 0--do not print the text, 1--the text is above the bar code, 2--the text is below the bar code, 3--both the top and bottom of the bar code are printed
     */
    @ReactMethod
    public void printBarCode(String data, int symbology, int height, int width, int textposition, final Promise p) {
        final IWoyouService ss = woyouService;
        Log.i(TAG, "come: ss:" + ss);
        final String d = data;
        final int s = symbology;
        final int h = height;
        final int w = width;
        final int tp = textposition;

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.printBarCode(d, s, h, w, tp, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Print 2D barcode
     *
     * @param data:       QR code data
     * @param modulesize: Two-dimensional code block size (unit: point, value 1 to 16)
     * @param errorlevel: QR code error correction level (0 to 3),
     *                    0 -- Error correction level L (7%),
     *                    1 -- Error correction level M (15%),
     *                    2 -- Error correction level Q (25%),
     *                    3 -- Error correction level H (30%)
     */
    @ReactMethod
    public void printQRCode(String data, int modulesize, int errorlevel, final Promise p) {
        final IWoyouService ss = woyouService;
        Log.i(TAG, "come: ss:" + ss);
        final String d = data;
        final int size = modulesize;
        final int level = errorlevel;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.printQRCode(d, size, level, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Print the text, the width of the text is full of one line, it will automatically wrap and type, and the whole line will not be printed unless it is forced to wrap.
     * The text is output as the vector text width, that is, the width of each character is not equal
     *
     * @param text: Text string to be printed
     */
    @ReactMethod
    public void printOriginalText(String text, final Promise p) {
        final IWoyouService ss = woyouService;
        Log.i(TAG, "come: " + text + " ss:" + ss);
        final String txt = text;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.printOriginalText(txt, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    /**
     * Print buffer content
     */
    @ReactMethod
    public void commitPrinterBuffer() {
        final IWoyouService ss = woyouService;
        Log.i(TAG, "come: commit buffter ss:" + ss);
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.commitPrinterBuffer();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Enter buffer mode, all print calls will be cached, and print after calling commitPrinterBuffe()
     *
     * @param clean: Whether to clear the buffer content
     */
    @ReactMethod
    public void enterPrinterBuffer(boolean clean) {
        final IWoyouService ss = woyouService;
        Log.i(TAG, "come: " + clean + " ss:" + ss);
        final boolean c = clean;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.enterPrinterBuffer(c);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Exit buffer mode
     *
     * @param commit: Whether to print out the buffer content
     */
    @ReactMethod
    public void exitPrinterBuffer(boolean commit) {
        final IWoyouService ss = woyouService;
        Log.i(TAG, "come: " + commit + " ss:" + ss);
        final boolean com = commit;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.exitPrinterBuffer(com);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }


    @ReactMethod
    public void printString(String message, final Promise p) {
        final IWoyouService ss = woyouService;
        Log.i(TAG, "come: " + message + " ss:" + ss);
        final String msgs = message;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.printText(msgs, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int par1, String par2) {
                            Log.d(TAG, "ON PRINT RES: " + par1 + ", " + par2);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                p.resolve(null);
                            } else {
                                p.reject("0", isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            p.resolve(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            p.reject("" + code, msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    p.reject("" + 0, e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void clearBuffer() {
        final IWoyouService ss = woyouService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.clearBuffer();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void exitPrinterBufferWithCallback(final boolean commit, final Callback callback) {
        final IWoyouService ss = woyouService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    ss.exitPrinterBufferWithCallback(commit, new ICallback.Stub() {
                        @Override
                        public void onPrintResult(int code, String msg) {
                            Log.d(TAG, "ON PRINT RES: " + code + ", " + msg);
                            if (code == 0)
                                callback.invoke(true);
                            else
                                callback.invoke(false);
                        }

                        @Override
                        public void onRunResult(boolean isSuccess) {
                            callback.invoke(isSuccess);
                        }

                        @Override
                        public void onReturnString(String result) {
                            // callback.invoke(isSuccess);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callback.invoke(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callback.invoke(false);
                }
            }
        });
    }
}
