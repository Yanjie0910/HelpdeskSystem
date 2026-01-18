package com.um.helpdesk.ticketsubmission;

public class TicketSubmissionSimulationMain {
    public static void main(String[] args) {
        // 传 null => 进入 Simulation mode => 显示菜单并接受数字输入
        new Activator().start(null);
    }
}
