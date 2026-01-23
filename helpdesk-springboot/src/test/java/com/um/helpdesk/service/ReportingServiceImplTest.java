package com.um.helpdesk.service;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.repository.SavedReportRepository;
import com.um.helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SavedReportRepository reportRepository;

    @InjectMocks
    private ReportingServiceImpl reportingService;

    private List<Ticket> mockTickets;

    @BeforeEach
    void setUp() {
        // Initialize common mock data
        mockTickets = new ArrayList<>();
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 1: MANAGE SAVED REPORTS (CRUD)
    // =================================================================

    @Test
    void testGenerateReport_SaveAndReturn() {
        // Arrange
        when(ticketRepository.findAll()).thenReturn(mockTickets);
        when(reportRepository.save(any(SavedReportArchive.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        SavedReportArchive result = reportingService.generateReport("UnitTest_Report", "CSV");

        // Assert
        assertNotNull(result);
        assertEquals("UnitTest_Report", result.getReportType());
        assertEquals("CSV", result.getFileFormat());
        verify(reportRepository, times(1)).save(any(SavedReportArchive.class));

        // Cleanup: Delete created dummy file
        File f = new File(result.getFilePath());
        if(f.exists()) f.delete();
    }

    @Test
    void testDeleteReport() {
        // Arrange
        Long reportId = 1L;
        SavedReportArchive mockReport = new SavedReportArchive();
        mockReport.setFilePath("dummy/path/file.csv");

        // Stub findById to return our mock report so delete logic can try to find file (optional)
        // Note: In your service implementation, you might strictly just call deleteById.
        // If you implemented file deletion logic, verify it here.

        // Act
        reportingService.deleteReport(reportId);

        // Assert
        verify(reportRepository, times(1)).deleteById(reportId);
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 2: CUSTOM DATA EXPORT (LOGIC CHECK)
    // =================================================================

    @Test
    void testGenerateReport_CreatesPhysicalFile() {
        // Arrange
        when(ticketRepository.findAll()).thenReturn(mockTickets);
        when(reportRepository.save(any(SavedReportArchive.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        SavedReportArchive result = reportingService.generateReport("FileCheck", "Text");

        // Assert
        File file = new File(result.getFilePath());
        assertTrue(file.exists(), "The physical report file should be created");
        assertTrue(file.length() > 0, "File should not be empty");

        // Cleanup
        file.delete();
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 3: ANALYZE FAILURE RATES (SLA LOGIC)
    // =================================================================

    @Test
    void testGetFailureRateAnalysis_WithBreaches() {
        // Arrange: Create a mix of Healthy and Overdue tickets
        LocalDateTime now = LocalDateTime.now();

        // 1. Healthy: Urgent ticket submitted 1 hour ago (SLA 24h)
        Ticket healthy = createMockTicket(TicketPriority.URGENT, TicketStatus.OPEN, now.minusHours(1));

        // 2. Failure: Urgent ticket submitted 30 hours ago (SLA 24h) -> BREACH
        Ticket breach = createMockTicket(TicketPriority.URGENT, TicketStatus.OPEN, now.minusHours(30));

        mockTickets.add(healthy);
        mockTickets.add(breach);

        when(ticketRepository.findAll()).thenReturn(mockTickets);

        // Act
        Map<String, Object> analysis = reportingService.getFailureRateAnalysis();

        // Assert
        assertEquals(2L, analysis.get("Total Tickets Analyzed"));
        assertEquals(1L, analysis.get("Overdue Tickets")); // 1 Breach
        assertEquals("50.00", analysis.get("Failure Rate (%)")); // 1 out of 2 = 50%
        assertEquals("CRITICAL", analysis.get("System Health")); // > 25% is usually Critical
    }

    // =================================================================
    // 🧪 TEST FUNCTIONALITY 4: FORECAST TICKET TRENDS (REGRESSION)
    // =================================================================

    @Test
    void testGetTicketTrendForecast_RisingTrend() {
        // Arrange: Simulate a rising trend (1 -> 2 -> 3 -> 4)
        LocalDateTime now = LocalDateTime.now();

        // Week -3: 1 Ticket
        mockTickets.add(createMockTicket(TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(3)));

        // Week -2: 2 Tickets
        mockTickets.add(createMockTicket(TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(2)));
        mockTickets.add(createMockTicket(TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(2)));

        // Week -1: 3 Tickets
        mockTickets.add(createMockTicket(TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(1)));
        mockTickets.add(createMockTicket(TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(1)));
        mockTickets.add(createMockTicket(TicketPriority.LOW, TicketStatus.CLOSED, now.minusWeeks(1)));

        // Current Week: 4 Tickets
        for(int i=0; i<4; i++) {
            mockTickets.add(createMockTicket(TicketPriority.LOW, TicketStatus.OPEN, now.minusHours(1)));
        }

        when(ticketRepository.findAll()).thenReturn(mockTickets);

        // Act
        Map<String, Integer> trends = reportingService.getTicketTrendForecast();

        // Assert
        // History: 1, 2, 3, 4
        // Linear Regression Prediction for next point (x=5) should be 5
        assertEquals(1, trends.get("3 Weeks Ago"));
        assertEquals(2, trends.get("2 Weeks Ago"));
        assertEquals(3, trends.get("Last Week"));
        assertEquals(4, trends.get("Current Week"));

        // Allow slight tolerance in regression depending on exact math impl, but perfect linear is exact
        assertEquals(5, trends.get("Next Week (Forecast)"));
    }

    // --- HELPER METHOD ---
    private Ticket createMockTicket(TicketPriority priority, TicketStatus status, LocalDateTime submittedAt) {
        Ticket t = new Ticket();
        t.setId((long) (Math.random() * 1000));
        t.setPriority(priority);
        t.setStatus(status);
        t.setSubmittedAt(submittedAt);
        t.setTitle("Mock Ticket");
        return t;
    }
}