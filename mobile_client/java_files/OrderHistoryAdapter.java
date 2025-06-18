package com.example.myshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderHistoryAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Информация о заказе
        holder.orderInfo.setText("Информация о заказе:");
        holder.orderStatus.setText("Статус заказа: " + order.getStatus());
        holder.orderAmount.setText("Сумма заказа: " + order.getTotalAmount() + "₽");

        // Инициализируем OrderProductAdapter для отображения продуктов в заказе
        OrderProductAdapter productAdapter = new OrderProductAdapter(context, order.getProductList());
        holder.productsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.productsRecyclerView.setAdapter(productAdapter);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView orderInfo, orderStatus, orderAmount;
        RecyclerView productsRecyclerView;

        public OrderViewHolder(View itemView) {
            super(itemView);
            orderInfo = itemView.findViewById(R.id.orderInfo);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderAmount = itemView.findViewById(R.id.orderAmount);
            productsRecyclerView = itemView.findViewById(R.id.productsRecyclerView);
        }
    }
}
