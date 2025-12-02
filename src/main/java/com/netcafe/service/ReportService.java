package com.netcafe.service;

import com.netcafe.dao.OrderDAO;
import com.netcafe.dao.TopupDAO;
import com.netcafe.model.Order;
import com.netcafe.model.Topup;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final TopupDAO topupDAO = new TopupDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public Map<YearMonth, Long> getMonthlyRevenue() throws Exception {
        Map<YearMonth, Long> revenueMap = new HashMap<>();
        // For simplicity in this demo, we will just fetch for the current month and
        // maybe a few past ones if we iterated.
        // But since the chart needs a Map, let's keep the existing logic of fetching
        // ALL and aggregating in memory
        // OR we can optimize. For now, let's use the new DAO methods for specific
        // months if we were building a specific report.
        // However, to populate a full chart, fetching all is easier for a small app.
        // Let's stick to the previous logic but maybe clean it up or use the new
        // methods if we want to query specific ranges.
        // Actually, the previous logic was fine for a small dataset.
        // Let's ADD the new methods for the Dashboard "Snapshot".

        // Re-implementing getMonthlyRevenue to be consistent with "Topups + Orders"
        // logic
        List<Topup> topups = topupDAO.findAll();
        for (Topup t : topups) {
            YearMonth ym = YearMonth.from(t.getCreatedAt());
            revenueMap.merge(ym, t.getAmount(), Long::sum);
        }
        List<Order> orders = orderDAO.findAll();
        for (Order o : orders) {
            if ("SERVED".equals(o.getStatus())) {
                YearMonth ym = YearMonth.from(o.getCreatedAt());
                revenueMap.merge(ym, o.getTotalPrice(), Long::sum);
            }
        }
        return revenueMap;
    }

    public Map<java.time.LocalDate, Long> getDailyRevenue() throws Exception {
        Map<java.time.LocalDate, Long> revenueMap = new HashMap<>();
        List<Topup> topups = topupDAO.findAll();
        for (Topup t : topups) {
            java.time.LocalDate date = t.getCreatedAt().toLocalDate();
            revenueMap.merge(date, t.getAmount(), Long::sum);
        }
        List<Order> orders = orderDAO.findAll();
        for (Order o : orders) {
            if ("SERVED".equals(o.getStatus())) {
                java.time.LocalDate date = o.getCreatedAt().toLocalDate();
                revenueMap.merge(date, o.getTotalPrice(), Long::sum);
            }
        }
        return revenueMap;
    }

    public Map<String, Integer> getTopSellingProducts(int limit) throws Exception {
        return orderDAO.getTopSellingProducts(limit);
    }

    public Map<String, Long> getTopSpenders(int limit) throws Exception {
        return topupDAO.getTopSpenders(limit);
    }
}
