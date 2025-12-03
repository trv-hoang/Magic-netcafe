package com.netcafe.service;

import com.netcafe.dao.OrderDAO;
import com.netcafe.dao.TopupDAO;

import java.time.YearMonth;
import java.util.HashMap;

import java.util.Map;

public class ReportService {
    private final TopupDAO topupDAO = new TopupDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public Map<YearMonth, Long> getMonthlyRevenue() throws Exception {
        Map<YearMonth, Long> revenueMap = new HashMap<>();

        // 1. Get from Topups
        Map<YearMonth, Long> topupRevenue = topupDAO.getMonthlyRevenueMap();
        topupRevenue.forEach((ym, amount) -> revenueMap.merge(ym, amount, Long::sum));

        // 2. Get from Orders
        Map<YearMonth, Long> orderRevenue = orderDAO.getMonthlyRevenueMap();
        orderRevenue.forEach((ym, amount) -> revenueMap.merge(ym, amount, Long::sum));

        return revenueMap;
    }

    public Map<java.time.LocalDate, Long> getDailyRevenue() throws Exception {
        Map<java.time.LocalDate, Long> revenueMap = new HashMap<>();

        // 1. Get from Topups
        Map<java.time.LocalDate, Long> topupRevenue = topupDAO.getDailyRevenueMap();
        topupRevenue.forEach((date, amount) -> revenueMap.merge(date, amount, Long::sum));

        // 2. Get from Orders
        Map<java.time.LocalDate, Long> orderRevenue = orderDAO.getDailyRevenueMap();
        orderRevenue.forEach((date, amount) -> revenueMap.merge(date, amount, Long::sum));

        return revenueMap;
    }

    public Map<String, Integer> getTopSellingProducts(int limit) throws Exception {
        return orderDAO.getTopSellingProducts(limit);
    }

    public Map<String, Long> getTopSpenders(int limit) throws Exception {
        return topupDAO.getTopSpenders(limit);
    }
}
