package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> amountList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime timeBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime timeEnd = LocalDateTime.of(localDate, LocalTime.MAX);

            //select  sum(amount) from orders where status = 5 and  order_time >timeBegin and   order_time < timeEnd
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("begin", timeBegin);
            map.put("end", timeEnd);
            Double amount = orderMapper.sumByMap(map);
            amount = amount == null ? 0.0 : amount;
            amountList.add(amount);
        }
        TurnoverReportVO reportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(amountList, ","))
                .build();

        return reportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //用户总量，select count(id) from user where create_time < end
        List<Integer> totalUserList = new ArrayList<>();

        //新增用户，select count(id) from user where create_time < end and create_time > start
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //订单总量，select count(id) from order where order_time < end and  order_time > start
        List<Integer> orderCountList = new ArrayList<>();

        //有效订单数量，select count(id) from order where order_time < end and  order_time > start and status = 5
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //每日订单数
            Integer orderCount = getOrders(beginTime, endTime, null);
            //每日有效订单数
            Integer validOrderCount = getOrders(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //有效订单总数
        Integer validTotalOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validTotalOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validTotalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

//        List<String> nameList = new ArrayList<>();
//        List<Integer> numberList = new ArrayList<>();
//        for (GoodsSalesDTO goodsSalesDTO : salesTop10) {
//            String name = goodsSalesDTO.getName();
//            Integer number = goodsSalesDTO.getNumber();
//            nameList.add(name);
//            numberList.add(number);
//        }
        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    private Integer getOrders(LocalDateTime begin, LocalDateTime end, Integer status) {

        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        Integer orders = orderMapper.countByMap(map);
        return orders;
    }

    @Override
    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        //1.获取前三十天运营数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX));
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //2.通过POI填充数据
        //基于模板文件创建一个新的Excel文件
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            sheet1.getRow(1).getCell(1).setCellValue("时间：" + beginDate + "至" + endDate);

            //营业额
            sheet1.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            //订单完成率
            sheet1.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            //新增用户数
            sheet1.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            //	有效订单
            sheet1.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            //平均客单价
            sheet1.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = beginDate.plusDays(i);
                BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                sheet1.getRow(7 + i).getCell(1).setCellValue(date.toString());
                sheet1.getRow(7 + i).getCell(2).setCellValue(businessDataVO.getTurnover());
                sheet1.getRow(7 + i).getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                sheet1.getRow(7 + i).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                sheet1.getRow(7 + i).getCell(5).setCellValue(businessDataVO.getUnitPrice());
                sheet1.getRow(7 + i).getCell(6).setCellValue(businessDataVO.getNewUsers());
            }

            //3.导出报表
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            excel.write(outputStream);

            excel.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
