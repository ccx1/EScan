package com.ccx.escan.listener;

import com.ccx.escan.DecodeType;

public interface ParsingCompleteListener {

    void onComplete(String text, String handingTime,DecodeType type);
}
