package com.example.myshop;

import java.util.List;

    public class Order {
        private String orderId;
        private List<Product> productList;
        private int totalAmount;
        private String userId;
        private long timestamp;
        private String status;

        public Order() {
        }

        public Order(String orderId, List<Product> productList, int totalAmount, String userId, long timestamp, String status) {
            this.orderId = orderId;
            this.productList = productList;
            this.userId = userId;
            this.totalAmount = totalAmount;
            this.timestamp = timestamp;
            this.status = status;
        }

        public String getOrderId() {
            return orderId;
        }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public List<Product> getProductList() {
            return productList;
        }

        public void setProductList(List<Product> productList) {
            this.productList = productList;
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


