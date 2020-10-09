package com.qr.scan.vo;

import com.qr.scan.entity.Camera;
import lombok.Data;

import java.util.List;

@Data
public class CameraScanRet {
    private Camera camera;
    private List<ScanRet> scanRetList;
}
