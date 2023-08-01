package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    //PENDING_PAYMENT 代付款
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 对付款超过15分钟的订单进行取消操作，每隔一分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeOutOrder() {
        log.info("定时处理超时订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时,自动取消");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 对昨日处于派送中的订单进行“已完成”设定，处理时段设定为凌晨一点
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, localDateTime);

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
