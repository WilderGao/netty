package com.gaojiancheng.netty_learn.protocol_stack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:Wilder Gao
 * @time:2018/3/2
 * @Discription：消息头的定义
 */
public final class Header {
    /**
     * 0xabef 特定开头代表netty协议
     */
    private int crcCode = 0xabef0101;
    private int length;
    private long sessionID;
    private byte type;  //消息类型
    private byte priority;  //消息优先级
    /**
     * 附件信息
     */
    private Map<String , Object> attachment = new HashMap<>();

    public int getCrcCode() {
        return crcCode;
    }

    public void setCrcCode(int crcCode) {
        this.crcCode = crcCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getSessionID() {
        return sessionID;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "Header{" +
                "crcCode=" + crcCode +
                ", length=" + length +
                ", sessionID=" + sessionID +
                ", type=" + type +
                ", priority=" + priority +
                ", attachment=" + attachment +
                '}';
    }
}