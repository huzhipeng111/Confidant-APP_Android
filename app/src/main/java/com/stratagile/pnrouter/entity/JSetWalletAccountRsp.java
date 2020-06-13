package com.stratagile.pnrouter.entity;


public class JSetWalletAccountRsp extends BaseEntity {

    /**
     * timestamp : 1590047441
     * params : {"Action":"SetWalletAccount","RetCode":0,"ToId":"/nbIsOurUJ7RMXfQaWMFwMHjD5qYth/hRgJZhRlu9JE=","WalletType":1,"Address":"djdjdjdjdjjdjdjdjdjdjdjdjdjd"}
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
         * Action : SetWalletAccount
         * RetCode : 0
         * ToId : /nbIsOurUJ7RMXfQaWMFwMHjD5qYth/hRgJZhRlu9JE=
         * WalletType : 1
         * Address : djdjdjdjdjjdjdjdjdjdjdjdjdjd
         */

        private String Action;
        private int RetCode;
        private String ToId;
        private String WalletType;
        private String Address;

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

        public String getToId() {
            return ToId;
        }

        public void setToId(String ToId) {
            this.ToId = ToId;
        }

        public String getWalletType() {
            return WalletType;
        }

        public void setWalletType(String WalletType) {
            this.WalletType = WalletType;
        }

        public String getAddress() {
            return Address;
        }

        public void setAddress(String Address) {
            this.Address = Address;
        }
    }
}
