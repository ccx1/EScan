/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ccx.escan.decode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ccx.escan.camera.CameraManager;
import com.ccx.escan.conts.Conts;
import com.ccx.escan.reader.MultiFormatReader;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.Map;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final MultiFormatReader multiFormatReader;
    private final CameraManager cameraManager;
    private final ResultHandler captureActivityHandler;
    private final Map<DecodeHintType, Object> hints;
    private final ImageScanner scanner;
    private boolean running = true;

    DecodeHandler(CameraManager cameraManager, ResultHandler captureActivityHandler,
        Map<DecodeHintType, Object> hints) {
        this.cameraManager = cameraManager;
        this.captureActivityHandler = captureActivityHandler;
        this.hints = hints;
        multiFormatReader = new MultiFormatReader(cameraManager);
        multiFormatReader.setHints(hints);
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
    }

    @Override
    public void handleMessage(Message message) {
        if (message == null || !running) {
            return;
        }
        if (message.what == 1) {
            running = false;
            Looper.myLooper().quit();
        } else {
            decode((byte[])message.obj, message.arg1, message.arg2);
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        // 因为zbar性能好，所以不需要将数据进行旋转。节省性能
        long start = System.currentTimeMillis();
        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);
        int result = scanner.scanImage(barcode);
        long end;
        if (result != 0) {
            SymbolSet syms = scanner.getResults();
            // 扫码结果页
            for (Symbol sym : syms) {
                end = System.currentTimeMillis();
                DecodeResult decodeResult = new DecodeResult();
                decodeResult.handingTime = (end - start + 0f) / 1000 + "";
                decodeResult.rawResult = sym.getData();
                Message message = Message.obtain(captureActivityHandler, Conts.Scan.SUECCESS, decodeResult);
                message.sendToTarget();
                return;
            }
        }
        // 如果zbar不行,再换成旋转后的zxing
        //竖屏
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }
        int tmp = width;
        width = height;
        height = tmp;
        data = rotatedData;

        //        if (width < height) {
        //            // portrait
        //            byte[] rotatedData = new byte[data.length];
        //            for (int x = 0; x < width; x++) {
        //                for (int y = 0; y < height; y++)
        //                    rotatedData[y * width + width - x - 1] = data[y + x * height];
        //            }
        //            data = rotatedData;
        //        }

        Result rawResult = getResult(data, width, height);
        // 检测条形码需要使用，如果默认里面不包含条形码，则不走此方法
       /* if (rawResult == null &&
                ((Vector) hints.get(DecodeHintType.POSSIBLE_FORMATS)).containsAll(DecodeFormatManager
                .PRODUCT_FORMATS)) {
            rawResult = getResult(rotateByteDegree90(data, width, height), width, height);
        }*/

        if (rawResult != null) {
            end = System.currentTimeMillis();
            DecodeResult decodeResult = new DecodeResult();
            decodeResult.handingTime = (end - start + 0f) / 1000 + "";
            decodeResult.rawResult = rawResult.getText();
            Message message = Message.obtain(captureActivityHandler, Conts.Scan.SUECCESS, decodeResult);
            message.sendToTarget();
            return;
        }

        captureActivityHandler.sendEmptyMessage(Conts.Scan.FAIL);
    }

    private Result getResult(byte[] data, int width, int height) {
        Result rawResult = null;
        LuminanceSource source = cameraManager.buildLuminanceSource(data, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            multiFormatReader.reset();
        }

        return rawResult;
    }

    private byte[] rotateByteDegree90(byte[] data, int width, int height) {
        byte[] neoData = new byte[width * height * 3 / 2];
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                neoData[i] = data[y * width + x];
                i++;
            }
        }
        i = width * height * 3 / 2 - 1;
        for (int x = width - 1; x > 0; x = x - 2) {
            for (int y = 0; y < height / 2; y++) {
                neoData[i] = data[(width * height) + (y * width) + x];
                i--;
                neoData[i] = data[(width * height) + (y * width) + (x - 1)];
                i--;
            }
        }
        return neoData;
    }

}
