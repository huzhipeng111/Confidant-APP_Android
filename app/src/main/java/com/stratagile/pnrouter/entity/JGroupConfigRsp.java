package com.stratagile.pnrouter.entity;

public class JGroupConfigRsp extends BaseEntity{


    /**
     * timestamp : 1553142986
     * params : {"Action":"GroupConfig","RetCode":0,"GId":"group1_admin36_time1553134930AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA","ToId":"8EAFEFA958FF15A10C5DFF698948987EB1C33F7A6AD4161DC53A7FD20F5B997EF5EBADB348BF","Type":2}
     */

    private ParamsBean params;

    public ParamsBean getParams() {
        return params;
    }

    public void setParams(ParamsBean params) {
        this.params = params;
    }

    public static class ParamsBean {
        /**
         * Action : GroupConfig
         * RetCode : 0
         * GId : group1_admin36_time1553134930AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
         * ToId : 8EAFEFA958FF15A10C5DFF698948987EB1C33F7A6AD4161DC53A7FD20F5B997EF5EBADB348BF
         * Type : 2
         */

        private String Action;
        private int RetCode;
        private String GId;
        private String ToId;
        private int Type;

        public String getAction() {
            return Action;
        }

        public void setAction(String Action) {
            this.Action = Action;
        }

        public int getRetCode() {
            return RetCode;
        }

        public void setRetCode(int RetCode) {
            this.RetCode = RetCode;
        }

        public String getGId() {
            return GId;
        }

        public void setGId(String GId) {
            this.GId = GId;
        }

        public String getToId() {
            return ToId;
        }

        public void setToId(String ToId) {
            this.ToId = ToId;
        }

        public int getType() {
            return Type;
        }

        public void setType(int Type) {
            this.Type = Type;
        }
    }
}
