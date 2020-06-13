package com.stratagile.pnrouter.entity;

public class AppDict extends BaseBackA {
    /**
     * data : {"toPromoteStartDate":"2020-05-19 00:00:00","conFeedbackScenario":"Message,Email,Circle/Account,Contacts,Encrypted Vault","toProMoteEndDate":"2020-06-19 23:59:59","conFeedbackType":"Bug,Crash,Improvement,New Requirement,Others"}
     * currentTimeMillis : 1591772223869
     */

    private long currentTimeMillis;
    private DataBean data;

    public DataBean getDataBean() {
        return data;
    }

    public void setDataBean(DataBean dataBean) {
        this.data = dataBean;
    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public static class DataBean {
        /**
         * toPromoteStartDate : 2020-05-19 00:00:00
         * conFeedbackScenario : Message,Email,Circle/Account,Contacts,Encrypted Vault
         * toProMoteEndDate : 2020-06-19 23:59:59
         * conFeedbackType : Bug,Crash,Improvement,New Requirement,Others
         */

        private String toPromoteStartDate;
        private String conFeedbackScenario;
        private String toProMoteEndDate;
        private String conFeedbackType;

        public String getToPromoteStartDate() {
            return toPromoteStartDate;
        }

        public void setToPromoteStartDate(String toPromoteStartDate) {
            this.toPromoteStartDate = toPromoteStartDate;
        }

        public String getConFeedbackScenario() {
            return conFeedbackScenario;
        }

        public void setConFeedbackScenario(String conFeedbackScenario) {
            this.conFeedbackScenario = conFeedbackScenario;
        }

        public String getToProMoteEndDate() {
            return toProMoteEndDate;
        }

        public void setToProMoteEndDate(String toProMoteEndDate) {
            this.toProMoteEndDate = toProMoteEndDate;
        }

        public String getConFeedbackType() {
            return conFeedbackType;
        }

        public void setConFeedbackType(String conFeedbackType) {
            this.conFeedbackType = conFeedbackType;
        }
    }
}
