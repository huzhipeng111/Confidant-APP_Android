package com.stratagile.pnrouter.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class FeedbackList extends BaseBackA {

    /**
     * msg : Request success
     * code : 0
     * feedbackList : [{"number":"202006111746177013","question":"测试","scenario":"Circle/Account","id":"32e7883905ed4059b3e852a805535059","type":"Improvement","imageList":["/data/feedback/da0e89a0d37946f287bce37d2d5ee3a8.jpg"],"email":"hzpvip008@163.com","status":"SUBMIT","createDate":"2020-06-11 17:46:18"}]
     */

    private List<FeedbackListBean> feedbackList;


    public List<FeedbackListBean> getFeedbackList() {
        return feedbackList;
    }

    public void setFeedbackList(List<FeedbackListBean> feedbackList) {
        this.feedbackList = feedbackList;
    }

    public static class FeedbackListBean implements Parcelable {
        /**
         * number : 202006111746177013
         * question : 测试
         * scenario : Circle/Account
         * id : 32e7883905ed4059b3e852a805535059
         * type : Improvement
         * imageList : ["/data/feedback/da0e89a0d37946f287bce37d2d5ee3a8.jpg"]
         * email : hzpvip008@163.com
         * status : SUBMIT
         * createDate : 2020-06-11 17:46:18
         */

        private String number;
        private String question;
        private String scenario;
        private String id;
        private String type;

        public String getResolvedDate() {
            return resolvedDate;
        }

        public void setResolvedDate(String resolvedDate) {
            this.resolvedDate = resolvedDate;
        }

        private String resolvedDate;

        protected FeedbackListBean(Parcel in) {
            number = in.readString();
            question = in.readString();
            scenario = in.readString();
            id = in.readString();
            type = in.readString();
            email = in.readString();
            status = in.readString();
            resolvedDate = in.readString();
            createDate = in.readString();
            imageList = in.createStringArrayList();
            replayList = in.createTypedArrayList(ReplayList.CREATOR);
            replayList1 = in.readParcelable(ReplayList.class.getClassLoader());
        }

        public static final Creator<FeedbackListBean> CREATOR = new Creator<FeedbackListBean>() {
            @Override
            public FeedbackListBean createFromParcel(Parcel in) {
                return new FeedbackListBean(in);
            }

            @Override
            public FeedbackListBean[] newArray(int size) {
                return new FeedbackListBean[size];
            }
        };

        public List<ReplayList> getReplayListBean() {
            return replayList;
        }

        public void setReplayListBean(List<ReplayList> replayList) {
            this.replayList = replayList;
        }

        private String email;
        private String status;
        private String createDate;
        private List<String> imageList;
        private List<ReplayList> replayList;
        private ReplayList replayList1;


        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getScenario() {
            return scenario;
        }

        public void setScenario(String scenario) {
            this.scenario = scenario;
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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

        public List<String> getImageList() {
            return imageList;
        }

        public void setImageList(List<String> imageList) {
            this.imageList = imageList;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(number);
            dest.writeString(question);
            dest.writeString(scenario);
            dest.writeString(id);
            dest.writeString(type);
            dest.writeString(email);
            dest.writeString(status);
            dest.writeString(resolvedDate);
            dest.writeString(createDate);
            dest.writeStringList(imageList);
            dest.writeTypedList(replayList);
            dest.writeParcelable(replayList1, flags);
        }
    }

    public static class ReplayList implements Parcelable{

        /**
         * type : SERVICE_REPLY
         * userName : superusr
         * imageList : ["/userfiles/1/images/guanggao/2020/06/00a236da64ab41b094bc9b95eb2d2c96.png"]
         * email :
         * content : sfdsfdsfdsfdsfdsf
         * createDate : 2020-06-12 09:46:13
         */

        private String type;
        private String userName;
        private String email;
        private String content;
        private String createDate;
        private List<String> imageList;

        public ReplayList() {
        }

        protected ReplayList(Parcel in) {
            type = in.readString();
            userName = in.readString();
            email = in.readString();
            content = in.readString();
            createDate = in.readString();
            imageList = in.createStringArrayList();
        }

        public static final Creator<ReplayList> CREATOR = new Creator<ReplayList>() {
            @Override
            public ReplayList createFromParcel(Parcel in) {
                return new ReplayList(in);
            }

            @Override
            public ReplayList[] newArray(int size) {
                return new ReplayList[size];
            }
        };

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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(type);
            dest.writeString(userName);
            dest.writeString(email);
            dest.writeString(content);
            dest.writeString(createDate);
            dest.writeStringList(imageList);
        }
    }
}
