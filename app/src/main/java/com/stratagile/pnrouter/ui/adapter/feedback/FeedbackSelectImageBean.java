package com.stratagile.pnrouter.ui.adapter.feedback;

import okhttp3.MultipartBody;

public class FeedbackSelectImageBean {
    private String filePath;
    private MultipartBody.Part part;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public MultipartBody.Part getPart() {
        return part;
    }

    public void setPart(MultipartBody.Part part) {
        this.part = part;
    }
}
