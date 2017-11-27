package com.jia.fingerprint.model;

import java.util.List;

/**
 * Describtion: 登录实体类
 * Created by jia on 2017/9/7.
 * 人之所以能，是相信能
 */
public class Login {

    /**
     * data : [{"site":{"name":"中国证券业协会","domain":"hzph.p.webtrn.cn:80","code":"localhost"},"loginId":"F41422","nickName":"","unTyxlLoginToken":"aHpwaC5wLndlYnRybi5jbjo4MHx8YjRjOGE2OTMyOWZmNDkyNGIyYmFiNGI1MmQ1MThhMzZ8fEY0MTQyMnx8bG9jYWxob3N0","photo":"","loginType":"b4c8a69329ff4924b2bab4b52d518a36"}]
     * errorCode : 1
     * errorMsg : 登录成功
     */

    private String errorCode;
    private String errorMsg;
    private List<DataBean> data;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * site : {"name":"中国证券业协会","domain":"hzph.p.webtrn.cn:80","code":"localhost"}
         * loginId : F41422
         * nickName :
         * unTyxlLoginToken : aHpwaC5wLndlYnRybi5jbjo4MHx8YjRjOGE2OTMyOWZmNDkyNGIyYmFiNGI1MmQ1MThhMzZ8fEY0MTQyMnx8bG9jYWxob3N0
         * photo :
         * loginType : b4c8a69329ff4924b2bab4b52d518a36
         */

        private SiteBean site;
        private String loginId;
        private String nickName;
        private String unTyxlLoginToken;
        private String photo;
        private String loginType;

        public SiteBean getSite() {
            return site;
        }

        public void setSite(SiteBean site) {
            this.site = site;
        }

        public String getLoginId() {
            return loginId;
        }

        public void setLoginId(String loginId) {
            this.loginId = loginId;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getUnTyxlLoginToken() {
            return unTyxlLoginToken;
        }

        public void setUnTyxlLoginToken(String unTyxlLoginToken) {
            this.unTyxlLoginToken = unTyxlLoginToken;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getLoginType() {
            return loginType;
        }

        public void setLoginType(String loginType) {
            this.loginType = loginType;
        }

        public static class SiteBean {
            /**
             * name : 中国证券业协会
             * domain : hzph.p.webtrn.cn:80
             * code : localhost
             */

            private String name;
            private String domain;
            private String code;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDomain() {
                return domain;
            }

            public void setDomain(String domain) {
                this.domain = domain;
            }

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "site=" + site +
                    ", loginId='" + loginId + '\'' +
                    ", nickName='" + nickName + '\'' +
                    ", unTyxlLoginToken='" + unTyxlLoginToken + '\'' +
                    ", photo='" + photo + '\'' +
                    ", loginType='" + loginType + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Login{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", data=" + data +
                '}';
    }
}
