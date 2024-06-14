//bmob的数据表

package com.example.gassistance;

import cn.bmob.v3.BmobObject;

public class Feedback extends BmobObject {
    private String content;
    private String contactInfo;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
