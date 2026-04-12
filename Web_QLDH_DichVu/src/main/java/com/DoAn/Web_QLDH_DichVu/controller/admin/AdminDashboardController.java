package com.DoAn.Web_QLDH_DichVu.controller.admin;

import com.DoAn.Web_QLDH_DichVu.entity.BuffOrder;
import com.DoAn.Web_QLDH_DichVu.repository.BuffOrderRepository;
import com.DoAn.Web_QLDH_DichVu.repository.RechargeRequestRepository;
import com.DoAn.Web_QLDH_DichVu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepo;
    private final BuffOrderRepository orderRepo;
    private final RechargeRequestRepository rechargeRepo;

    @GetMapping
    public String showDashboard(Model model) {
        model.addAttribute("totalUsers", userRepo.count());
        model.addAttribute("totalOrders", orderRepo.count());
        model.addAttribute("totalRechargeRequests", rechargeRepo.count());

        List<BuffOrder> allOrders = orderRepo.findAll();

        long pending = allOrders.stream().filter(o -> o.getStatus().name().equals("PENDING")).count();
        long inProgress = allOrders.stream().filter(o -> o.getStatus().name().equals("IN_PROGRESS")).count();
        long completed = allOrders.stream().filter(o -> o.getStatus().name().equals("COMPLETED")).count();
        long cancelled = allOrders.stream().filter(o -> o.getStatus().name().equals("CANCELLED")).count();

        model.addAttribute("chartData", List.of(pending, inProgress, completed, cancelled));

        return "admin/dashboard";
    }
}