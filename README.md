### EScan

#### gif还是用了zxing的gif。因为ui上面没有变化。

<img src="/gif/scan.gif" width="320px" />

将zxing和zbar解析二维码功能进行结合，zbar性能高，但是在扫描的时候是在主线程，导致整体扫描会让动画卡死，zxing本身是处在多线程的所以不会出现此问题

目前功能点 ：

    1. 扫一扫，高度灵敏功能。

    2. 生成二维码的工具类,这里工具类还是采用了zxing的方法来进行生成。ZxingUtils


#### 如何使用

在工程的gradle下

        implementation 'com.ccx1:escan:1.0.1'

在project的gradle中的allprojects中加入

        allprojects {
            repositories {
                google()
                jcenter()
                maven {url 'https://dl.bintray.com/ci250454344/EScan'}
            }
        }


使用方法，直接进行findviewbyid可以直接开始扫描，需要照相机权限.

写了一个ParsingCompleteListener 监听器.如果只是当前页面，用此回调即可，如果不用此回调，可以使用setResult进行返回

一个是会有识别type


        mScannerView = view.findViewById(R.id.findview);
        mScannerView.setOnParsingCompleteListener(new ParsingCompleteListener() {

            @Override
            public void onComplete(String text, String handingTime, DecodeType type) {
                switch (type) {
                    case EMAIL:
                        break;
                    case TEXT:
                        break;
                    case NUMBER:
                        break;
                    case URL:
                        break;
                }
            }
        });


当销毁的时候，需要调用release（）进行资源释放



        @Override
        public void onDestroy() {
            mScannerView.release();
            super.onDestroy();
        }


当扫描成功之后。界面就会停止扫描。如果需要恢复，可以调用reset方法进行重置


         // 扫描结束,重置扫描
         mScannerView.reset();


解释无力，还是看demo把，很简单


<img src="/gif/encoding.gif" width="320px" />


生成二维码的调用方法是ZXingUtils.encodeAsBitmap(当前的context, 需要生成的文字)。会返回一个bitmap。如果有rxjava，这步可以搞成异步


         mBitmap = ZXingUtils.encodeAsBitmap(getActivity(), text);


新增条形码扫描，原先不灵敏，只能横向扫描才能产生结果，目前做了优化，支持横向，纵向，斜度的暂时还不支持.

新增解析图片，emm，只支持bitmap形式传递，不服你打我。

uri不想做了，反正最终就是以bitmap形式来的，如果是rxjava，这里也可以搞成异步，性能消耗小一点，我试了一下，解析一张二维码只用0.079秒，还是蛮高效的


        DecodeResult decodeResult = ZXingUtils.encodeImage(BitmapFactory.decodeResource(getResources(), R.mipmap.max));


max是我传的二维码图，你们想传文件也好，什么都可以，BitmapFactory都能给搞成bitmap。我就不搞了。接下来是这是参数


        public static DecodeResult encodeImage(Bitmap bitmap)

然后。DecodeResult目前只有两个个字段。一个是结果，一个是处理时间（秀优越用的）.

新增打开闪光灯

        public void openFlash(boolean isOpen)

使用方法是

        mScannerView.openFlash(isOpen);

#### 新增方法

1. 设置扫描框线条粗细

        mScannerView.setScannerViewStrokeWidth(5);

2. 设置扫描框大小

        mScannerView.setScanWidthAndHeight(80);

3. 设置扫描框颜色

        mScannerView.setScannerViewColor(Color.RED);

4. 设置扫描框中间线条的波动时间

        mScannerView.setAnimationDelay(20);

5. 设置扫描框中心线条到底部的距离。

        mScannerView.setLineRollingDist(30);

6. 设置扫描框中心线条的样式

        mScannerView.setShader(shader);


新增自定义属性 ，是否打开前置摄像头。没写代码切换

        <attr name="openFront" format="boolean"/>



