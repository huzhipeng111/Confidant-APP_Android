package com.stratagile.pnrouter.entity;

import java.util.List;

public class ActiveList extends BaseBackA {

    /**
     * msg : Request success
     * code : 0
     * messageList : [{"digest":"digest digest digest digest digest digest digest digest digest digest digest digest ","id":"402880e7509276fa01509282e6a40002","type":"PROMOTE","title":"title title title title title title title title","content":"content content content content content content content content content content content content content content content content content content content content content content content content content content content content content content ","status":"PUSHED","createDate":"2020-05-19 16:21:27"}]
     */

    private List<MessageListBean> messageList;

    public List<MessageListBean> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<MessageListBean> messageList) {
        this.messageList = messageList;
    }

    public static class MessageListBean {
        /**
         * digest : digest digest digest digest digest digest digest digest digest digest digest digest
         * id : 402880e7509276fa01509282e6a40002
         * type : PROMOTE
         * title : title title title title title title title title
         * content : content content content content content content content content content content content content content content content content content content content content content content content content content content content content content content
         * status : PUSHED
         * createDate : 2020-05-19 16:21:27
         */

        private String digest;
        private String id;
        private String type;
        private String title;
        private String content;
        private String status;
        private String createDate;

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }
    }
}
