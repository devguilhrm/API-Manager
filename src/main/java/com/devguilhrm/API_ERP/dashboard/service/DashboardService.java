package com.devguilhrm.API_ERP.dashboard.service;

import com.devguilhrm.API_ERP.dashboard.dto.DashboardDTO;

import java.time.LocalDate;

public interface DashboardService {

	default DashboardDTO getGlobalDashboard() {
		return getGlobalDashboard(null, null);
	}

	DashboardDTO getGlobalDashboard(LocalDate from, LocalDate to);
}
