package com.netcafe.service;

import com.netcafe.dao.ComputerDAO;
import com.netcafe.dao.MaintenanceDAO;
import com.netcafe.model.Computer;
import com.netcafe.model.MaintenanceRequest;

import java.util.List;

public class ComputerService {
    private final ComputerDAO computerDAO = new ComputerDAO();
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();

    public List<Computer> getAllComputers() throws Exception {
        return computerDAO.findAll();
    }

    public void updateComputerStatus(int computerId, Computer.Status status) throws Exception {
        computerDAO.updateStatus(computerId, status);
    }

    public void reportIssue(int computerId, int userId, String issue) throws Exception {
        MaintenanceRequest req = new MaintenanceRequest(computerId, userId, issue);
        maintenanceDAO.create(req);
        // Automatically mark computer as DIRTY or MAINTENANCE?
        // Let's mark as DIRTY for now so Admin notices.
        computerDAO.updateStatus(computerId, Computer.Status.DIRTY);
    }

    public List<MaintenanceRequest> getPendingMaintenanceRequests() throws Exception {
        return maintenanceDAO.findAllPending();
    }

    public void resolveMaintenanceRequest(int requestId, int computerId) throws Exception {
        maintenanceDAO.updateStatus(requestId, MaintenanceRequest.Status.RESOLVED);
        // Set computer back to AVAILABLE
        computerDAO.updateStatus(computerId, Computer.Status.AVAILABLE);
    }
}
