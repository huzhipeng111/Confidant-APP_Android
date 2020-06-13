package com.stratagile.pnrouter.entity;

import java.util.List;

public class JGetWalletAccountRsp extends BaseEntity {


    /**
     * timestamp : 1590048761
     * params : {"Action":"GetWalletAccount","ToId":"/nbIsOurUJ7RMXfQaWMFwMHjD5qYth/hRgJZhRlu9JE=","RetCode":0,"WalletNum":1,"Payload":[{"WalletType":1,"Address":"djdjdjdjdjjdjdjdjdjdjdjdjdjd"}],"Num":1}
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
         * Action : GetWalletAccount
         * ToId : /nbIsOurUJ7RMXfQaWMFwMHjD5qYth/hRgJZhRlu9JE=
         * RetCode : 0
         * WalletNum : 1
         * Payload : [{"WalletType":1,"Address":"djdjdjdjdjjdjdjdjdjdjdjdjdjd"}]
         * Num : 1
         */

        private String Action;
        private String ToId;
        private int RetCode;
        private int WalletNum;
        private int Num;
        private List<PayloadBean> Payload;

        public String getAction() {
            return Action;
        }

        public void setAction(String Action) {
            this.Action = Action;
        }

        public String getToId() {
            return ToId;
        }

        public void setToId(String ToId) {
            this.ToId = ToId;
        }

        public int getRetCode() {
            return RetCode;
        }

        public void setRetCode(int RetCode) {
            this.RetCode = RetCode;
        }

        public int getWalletNum() {
            return WalletNum;
        }

        public void setWalletNum(int WalletNum) {
            this.WalletNum = WalletNum;
        }

        public int getNum() {
            return Num;
        }

        public void setNum(int Num) {
            this.Num = Num;
        }

        public List<PayloadBean> getPayload() {
            return Payload;
        }

        public void setPayload(List<PayloadBean> Payload) {
            this.Payload = Payload;
        }

        public static class PayloadBean {
            /**
             * WalletType : 1
             * Address : djdjdjdjdjjdjdjdjdjdjdjdjdjd
             */

            private int WalletType;
            private String Address;

            public int getWalletType() {
                return WalletType;
            }

            public void setWalletType(int WalletType) {
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
}
