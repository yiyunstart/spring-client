package com.qr.scan.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Hashtable;

public class QrcodeUtils {
    public static Result[] decodeQRcode(BufferedImage image) {
        Result[] qrCodeText = null;
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new GlobalHistogramBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);


            Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            //优化精度
            hints.put(DecodeHintType.TRY_HARDER, Boolean.FALSE);
            //复杂模式，开启PURE_BARCODE模式
//            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);

            QRCodeMultiReader qc = new QRCodeMultiReader();//一张图片有多张二维码取最后一个
            Result[] r = qc.decodeMultiple(binaryBitmap, hints);

            qrCodeText = r;
        } catch (Exception e) {
//            qrCodeText = detector.detectAndDecode(Imgcodecs.imread(qrCodePath, 1));
        }
        return qrCodeText;
    }

    public static BufferedImage printImg(BufferedImage image, Result[] qr) {
        if (qr == null || qr.length == 0) {
            return image;
        }


        Graphics2D g2d = image.createGraphics();

        g2d.drawImage(image, 0, 0, null);

        if (qr != null) {
            for ( int i = 0 ;i<qr.length ;i++){
                Result ret  = qr [i];
//                System.out.print("位置：");
//                Arrays.asList(ret.getResultPoints()).stream().forEach(p -> {
//                    System.out.print(":" + p.getX() + "," + p.getY());
//                });

                ResultPoint p1 = ret.getResultPoints()[0];
                ResultPoint p2 = ret.getResultPoints()[1];
                ResultPoint p3 = ret.getResultPoints()[2];
                ResultPoint p4 = ret.getResultPoints().length > 3 ? ret.getResultPoints()[3] : null;
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3.0f));

//                g2d.drawLine((int) p1.getX(), (int) p1.getY()
//                        , (int) p2.getX(), (int) p2.getY());
//
//                g2d.drawLine((int) p2.getX(), (int) p2.getY()
//                        , (int) p3.getX(), (int) p3.getY());

                double juli = Math.sqrt(Math.abs((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY())));
//
//                if (p4 != null) {
//                    int width = (int) (juli / 4 / 2);
//                    g2d.fillRect((int) p4.getX(), (int) p4.getY()
//                            , width, width);
//
//                    g2d.drawString(ret.getText(), p4.getX() - 200, p4.getY() + width * 4);
//                }
                double x = (p1.getX()+p2.getX()+p3.getX())/3;
                double y = (p1.getY()+p2.getY()+p3.getY())/3;
//                int width = (int) (juli / 4 / 2);

//                g2d.fillRect((int) x, (int) y
//                        , width, width);
                int owidth = (int) (juli *1.4);
                int r = (int) (owidth /2);
// 抗锯齿
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawOval((int) x-r,(int) y-r,owidth,owidth);
                Font myfont = new Font(null,Font.PLAIN,owidth);
                g2d.setFont(myfont);
                g2d.drawString(""+(i+1), (int) x-(r/2),(int) y+(r/2));
                System.out.println(ret.getText());
            };
        }

        g2d.dispose();
        return image;
//        ImageIO.write(image, "JPG", file1);
    }


}
