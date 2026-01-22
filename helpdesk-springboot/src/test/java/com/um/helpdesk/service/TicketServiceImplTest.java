package com.um.helpdesk.service;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.repository.DepartmentRepository;
import com.um.helpdesk.repository.TicketRepository;
import com.um.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for Ticket Assignment Module
 * Tests all 4 core functionalities
 *
 * @author Abdul Azim
 */
@DisplayName("Ticket Assignment Module - Unit Tests")
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Department itDepartment;
    private Department facilitiesDepartment;
    private TechnicianSupportStaff technicianBob;
    private TechnicianSupportStaff technicianSiti;
    private TechnicianSupportStaff technicianKumar;
    private Student student;
    private Ticket wifiTicket;
    private Ticket acTicket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test departments
        itDepartment = new Department("Information Technology", "IT");
        itDepartment.setId(1L);

        facilitiesDepartment = new Department("Facilities Management", "FACILITIES");
        facilitiesDepartment.setId(2L);

        // Create test technicians
        technicianBob = new TechnicianSupportStaff();
        technicianBob.setId(1L);
        technicianBob.setFullName("Bob Lee");
        technicianBob.setDepartment(itDepartment);
        technicianBob.setActive(true);

        technicianSiti = new TechnicianSupportStaff();
        technicianSiti.setId(2L);
        technicianSiti.setFullName("Siti Software Expert");
        technicianSiti.setDepartment(itDepartment);
        technicianSiti.setActive(true);

        technicianKumar = new TechnicianSupportStaff();
        technicianKumar.setId(3L);
        technicianKumar.setFullName("Kumar Facilities");
        technicianKumar.setDepartment(facilitiesDepartment);
        technicianKumar.setActive(true);

        // Create test student
        student = new Student();
        student.setId(10L);
        student.setFullName("Ali Student");

        // Create test tickets
        wifiTicket = new Ticket();
        wifiTicket.setId(1L);
        wifiTicket.setTitle("Cannot connect to campus WiFi");
        wifiTicket.setDescription("My laptop cannot connect to the university WiFi network. It shows authentication error.");
        wifiTicket.setCategory("Network");
        wifiTicket.setPriority(TicketPriority.HIGH);
        wifiTicket.setSubmittedBy(student);
        wifiTicket.setStatus(TicketStatus.OPEN);
        wifiTicket.setSubmittedAt(LocalDateTime.now());

        acTicket = new Ticket();
        acTicket.setId(2L);
        acTicket.setTitle("Air conditioning not working");
        acTicket.setDescription("The air conditioning unit in lecture room A301 has not been working for 2 days.");
        acTicket.setCategory("Facilities");
        acTicket.setPriority(TicketPriority.MEDIUM);
        acTicket.setSubmittedBy(student);
        acTicket.setStatus(TicketStatus.OPEN);
        acTicket.setSubmittedAt(LocalDateTime.now());
    }

    // ========== FUNCTIONALITY 1: Automated Departmental Routing Tests ==========

    @Test
    @DisplayName("F1: Should analyze ticket content and route to IT department based on keywords")
    void testAnalyzeDepartmentFromTicket_IT() {
        // Arrange
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(itDepartment, facilitiesDepartment));

        // Act
        Department result = ticketService.analyzeDepartmentFromTicket(wifiTicket);

        // Assert
        assertNotNull(result, "Department should not be null");
        assertEquals("IT", result.getCode(), "Should route to IT department");
        assertEquals("Information Technology", result.getName());
    }

    @Test
    @DisplayName("F1: Should analyze ticket content and route to Facilities department based on keywords")
    void testAnalyzeDepartmentFromTicket_Facilities() {
        // Arrange
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(itDepartment, facilitiesDepartment));

        // Act
        Department result = ticketService.analyzeDepartmentFromTicket(acTicket);

        // Assert
        assertNotNull(result, "Department should not be null");
        assertEquals("FACILITIES", result.getCode(), "Should route to Facilities department");
        assertEquals("Facilities Management", result.getName());
    }

    @Test
    @DisplayName("F1: Should route ticket to department and update ticket")
    void testRouteTicketToDepartment() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(itDepartment, facilitiesDepartment));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act
        Department result = ticketService.routeTicketToDepartment(1L);

        // Assert
        assertNotNull(result);
        assertEquals("IT", result.getCode());
        verify(ticketRepository).save(wifiTicket);
        assertEquals(itDepartment, wifiTicket.getAssignedDepartment());
        assertEquals(TicketStatus.OPEN, wifiTicket.getStatus());
    }

    @Test
    @DisplayName("F1: Should auto-route and assign to least busy technician")
    void testAutoRouteAndAssign() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(itDepartment, facilitiesDepartment));
        when(userRepository.findAll()).thenReturn(Arrays.asList(technicianBob, technicianSiti, technicianKumar));
        when(ticketRepository.countActiveTicketsByTechnician(1L)).thenReturn(2L);
        when(ticketRepository.countActiveTicketsByTechnician(2L)).thenReturn(1L);  // Siti has less workload
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act
        Ticket result = ticketService.autoRouteAndAssign(1L);

        // Assert
        assertNotNull(result);
        assertEquals(itDepartment, result.getAssignedDepartment());
        assertEquals(technicianSiti, result.getAssignedTo(), "Should assign to least busy technician");
        assertEquals(TicketStatus.ASSIGNED, result.getStatus());
        assertNotNull(result.getAssignedAt());
        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    // ========== FUNCTIONALITY 2: Ticket Claiming and Self-Assignment Tests ==========

    @Test
    @DisplayName("F2: Should get unassigned tickets for department")
    void testGetUnassignedTicketsForDepartment() {
        // Arrange
        List<Ticket> expectedTickets = Arrays.asList(wifiTicket, acTicket);
        when(ticketRepository.findUnassignedByDepartment(1L)).thenReturn(expectedTickets);

        // Act
        List<Ticket> result = ticketService.getUnassignedTicketsForDepartment(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ticketRepository).findUnassignedByDepartment(1L);
    }

    @Test
    @DisplayName("F2: Should allow technician to claim unassigned ticket")
    void testClaimTicket_Success() {
        // Arrange
        wifiTicket.setAssignedDepartment(itDepartment);
        wifiTicket.setAssignedTo(null);  // Unassigned

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(technicianBob));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act
        Ticket result = ticketService.claimTicket(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(technicianBob, result.getAssignedTo());
        assertEquals(TicketStatus.ASSIGNED, result.getStatus());
        assertNotNull(result.getAssignedAt());
        verify(ticketRepository).save(wifiTicket);
    }

    @Test
    @DisplayName("F2: Should throw error when claiming already assigned ticket")
    void testClaimTicket_AlreadyAssigned() {
        // Arrange
        wifiTicket.setAssignedTo(technicianSiti);  // Already assigned

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.claimTicket(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("already assigned"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("F2: Should throw error when technician not in ticket's department")
    void testClaimTicket_WrongDepartment() {
        // Arrange
        wifiTicket.setAssignedDepartment(itDepartment);
        wifiTicket.setAssignedTo(null);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(technicianKumar));  // Facilities tech

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.claimTicket(1L, 3L);
        });

        assertTrue(exception.getMessage().contains("not in the ticket's assigned department"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("F2: Should get claimable tickets for technician")
    void testGetClaimableTickets() {
        // Arrange
        List<Ticket> expectedTickets = Arrays.asList(wifiTicket);
        when(userRepository.findById(1L)).thenReturn(Optional.of(technicianBob));
        when(ticketRepository.findUnassignedByDepartment(1L)).thenReturn(expectedTickets);

        // Act
        List<Ticket> result = ticketService.getClaimableTickets(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(wifiTicket, result.get(0));
    }

    // ========== FUNCTIONALITY 3: Internal Re-assignment and Chaining Tests ==========

    @Test
    @DisplayName("F3: Should reassign ticket to colleague in same department")
    void testReassignTicketInternally_Success() {
        // Arrange
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);
        wifiTicket.setReassignmentCount(0);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(technicianSiti));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act
        Ticket result = ticketService.reassignTicketInternally(1L, 2L, "Network specialist needed");

        // Assert
        assertNotNull(result);
        assertEquals(technicianSiti, result.getAssignedTo(), "Should be assigned to new technician");
        assertEquals(technicianBob, result.getPreviousAssignee(), "Should track previous assignee");
        assertEquals(1, result.getReassignmentCount(), "Should increment reassignment count");
        assertEquals(TicketStatus.ASSIGNED, result.getStatus());
        assertNotNull(result.getAssignedAt());
        verify(ticketRepository).save(wifiTicket);
    }

    @Test
    @DisplayName("F3: Should throw error when reassigning to different department")
    void testReassignTicketInternally_DifferentDepartment() {
        // Arrange
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(technicianKumar));  // Facilities tech

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.reassignTicketInternally(1L, 3L, "Test");
        });

        assertTrue(exception.getMessage().contains("different departments"));
        assertTrue(exception.getMessage().contains("transfer"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("F3: Should enforce reassignment limit")
    void testReassignTicketInternally_LimitReached() {
        // Arrange
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setReassignmentCount(3);  // Already at limit

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.reassignTicketInternally(1L, 2L, "Test");
        });

        assertTrue(exception.getMessage().contains("maximum reassignment limit"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("F3: Should return assignment history chain")
    void testGetAssignmentHistory() {
        // Arrange
        wifiTicket.setPreviousAssignee(technicianBob);
        wifiTicket.setAssignedTo(technicianSiti);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));

        // Act
        List<TechnicianSupportStaff> result = ticketService.getAssignmentHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(technicianBob, result.get(0), "First in chain should be previous assignee");
        assertEquals(technicianSiti, result.get(1), "Second in chain should be current assignee");
    }

    @Test
    @DisplayName("F3: Should check if ticket can be reassigned")
    void testCanReassign() {
        // Arrange
        wifiTicket.setReassignmentCount(2);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));

        // Act
        boolean result = ticketService.canReassign(1L);

        // Assert
        assertTrue(result, "Should allow reassignment when count < 3");

        // Test limit
        wifiTicket.setReassignmentCount(3);
        boolean result2 = ticketService.canReassign(1L);
        assertFalse(result2, "Should not allow reassignment when count >= 3");
    }

    // ========== FUNCTIONALITY 4: Inter-Departmental Transfer Tests ==========

    @Test
    @DisplayName("F4: Should transfer ticket to different department")
    void testTransferTicketToDepartment() {
        // Arrange
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);
        wifiTicket.setReassignmentCount(0);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(facilitiesDepartment));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act
        Ticket result = ticketService.transferTicketToDepartment(1L, 2L, "Wrong department");

        // Assert
        assertNotNull(result);
        assertEquals(facilitiesDepartment, result.getAssignedDepartment(), "Should be in new department");
        assertNull(result.getAssignedTo(), "Should unassign technician");
        assertEquals(technicianBob, result.getPreviousAssignee(), "Should track previous assignee");
        assertEquals(TicketStatus.OPEN, result.getStatus(), "Should reset to OPEN");
        assertEquals(1, result.getReassignmentCount(), "Should increment count");
        verify(ticketRepository).save(wifiTicket);
    }

    @Test
    @DisplayName("F4: Should transfer and assign to specific technician")
    void testTransferAndAssign() {
        // Arrange
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(facilitiesDepartment));
        when(userRepository.findById(3L)).thenReturn(Optional.of(technicianKumar));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act
        Ticket result = ticketService.transferAndAssign(1L, 2L, 3L, "Facilities issue");

        // Assert
        assertNotNull(result);
        assertEquals(facilitiesDepartment, result.getAssignedDepartment());
        assertEquals(technicianKumar, result.getAssignedTo(), "Should be assigned to new tech");
        assertEquals(TicketStatus.ASSIGNED, result.getStatus());
        assertNotNull(result.getAssignedAt());
        verify(ticketRepository, times(2)).save(wifiTicket);
    }

    @Test
    @DisplayName("F4: Should throw error when technician not in target department")
    void testTransferAndAssign_WrongDepartment() {
        // Arrange
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(facilitiesDepartment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(technicianBob));  // IT tech, wrong dept
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.transferAndAssign(1L, 2L, 1L, "Test");
        });

        assertTrue(exception.getMessage().contains("not in the target department"));
    }

    // ========== Basic Operations Tests ==========

    @Test
    @DisplayName("Should create new ticket")
    void testCreateTicket() {
        // Arrange
        when(ticketRepository.save(any(Ticket.class))).thenReturn(wifiTicket);

        // Act
        Ticket result = ticketService.createTicket(wifiTicket);

        // Assert
        assertNotNull(result);
        assertEquals(TicketStatus.OPEN, result.getStatus());
        assertNotNull(result.getSubmittedAt());
        verify(ticketRepository).save(wifiTicket);
    }

    @Test
    @DisplayName("Should get ticket by ID")
    void testGetTicketById() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(wifiTicket));

        // Act
        Ticket result = ticketService.getTicketById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cannot connect to campus WiFi", result.getTitle());
    }

    @Test
    @DisplayName("Should get all tickets")
    void testGetAllTickets() {
        // Arrange
        List<Ticket> expectedTickets = Arrays.asList(wifiTicket, acTicket);
        when(ticketRepository.findAll()).thenReturn(expectedTickets);

        // Act
        List<Ticket> result = ticketService.getAllTickets();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should get technician workload")
    void testGetTechnicianWorkload() {
        // Arrange
        when(ticketRepository.countActiveTicketsByTechnician(1L)).thenReturn(5L);

        // Act
        int result = ticketService.getTechnicianWorkload(1L);

        // Assert
        assertEquals(5, result);
    }

    @Test
    @DisplayName("Should find least busy technician")
    void testFindLeastBusyTechnician() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(technicianBob, technicianSiti));
        when(ticketRepository.countActiveTicketsByTechnician(1L)).thenReturn(5L);
        when(ticketRepository.countActiveTicketsByTechnician(2L)).thenReturn(2L);  // Siti has less

        // Act
        TechnicianSupportStaff result = ticketService.findLeastBusyTechnician(1L);

        // Assert
        assertNotNull(result);
        assertEquals(technicianSiti, result, "Should return technician with least workload");
    }

    @Test
    @DisplayName("Should get department workload")
    void testGetDepartmentWorkload() {
        // Arrange
        when(ticketRepository.countActiveTicketsByDepartment(1L)).thenReturn(10L);

        // Act
        int result = ticketService.getDepartmentWorkload(1L);

        // Assert
        assertEquals(10, result);
    }
}
