package com.qr.scan.vo;

import com.google.zxing.Result;
import com.qr.scan.entity.CameraPoint;
import lombok.Data;

import java.awt.image.BufferedImage;

@Data
public class ScanRet  {
    private CameraPoint cameraPoint;
    private BufferedImage image;
    private Result[] qrCodes;
}
