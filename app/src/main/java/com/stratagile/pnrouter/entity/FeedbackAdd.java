package com.stratagile.pnrouter.entity;

import java.util.List;

public class FeedbackAdd extends BaseBackA{

    /**
     * msg : Request success
     * code : 0
     * reply : {"type":"USER_ADD","userName":"hzp-meizu","userId":"1nWxVeOlAXJOH1P06DWLI2WgKidLtIwDekLqVopfr3c=","imageList":["/data/feedback/5f3a98bba4a24fd5a2573be9938ad75e.jpg","/data/feedback/59e815bf65274354a34252bdbe3690ea.jpg"],"email":"hzpvip008@163.com","content":"测试反馈追加","createDate":"2020-06-12 13:19:42"}
     */

    private FeedbackList.ReplayList reply;

    public FeedbackList.ReplayList getReply() {
        return reply;
    }

    public void setReply(FeedbackList.ReplayList reply) {
        this.reply = reply;
    }

    public static class ReplyBean {
        /**
         * type : USER_ADD
         * userName : hzp-meizu
         * userId : 1nWxVeOlAXJOH1P06DWLI2WgKidLtIwDekLqVopfr3c=
         * imageList : ["/data/feedback/5f3a98bba4a24fd5a2573be9938ad75e.jpg","/data/feedback/59e815bf65274354a34252bdbe3690ea.jpg"]
         * email : hzpvip008@163.com
         * content : 测试反馈追加
         * createDate : 2020-06-12 13:19:42
         */

        private String type;
        private String userName;
        private String userId;
        private String email;
        private String content;
        private String createDate;
        private List<String> imageList;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public List<String> getImageList() {
            return imageList;
        }

        public void setImageList(List<String> imageList) {
            this.imageList = imageList;
        }
    }
}
