package com.um.helpdesk.ticketassignment;

import com.um.helpdesk.entity.*;
import com.um.helpdesk.ticketassignment.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for Ticket Assignment Module (OSGi Version)
 * Tests all 4 core functionalities
 *
 * @author Abdul Azim
 */
@DisplayName("Ticket Assignment Module - OSGi Unit Tests")
class TicketServiceImplTest {

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private TicketServiceImpl ticketService;

    private Department itDepartment;
    private Department facilitiesDepartment;
    private TechnicianSupportStaff technicianBob;
    private TechnicianSupportStaff technicianSiti;
    private TechnicianSupportStaff technicianKumar;
    private Student student;
    private Ticket wifiTicket;
    private Ticket projectorTicket;

    @BeforeEach
    void setUp() {
        // Mock EntityManager and Transaction
        entityManager = mock(EntityManager.class);
        transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        ticketService = new TicketServiceImpl(entityManager);

        // Setup test data
        setupDepartments();
        setupTechnicians();
        setupStudent();
        setupTickets();
    }

    private void setupDepartments() {
        itDepartment = new Department();
        itDepartment.setId(1L);
        itDepartment.setName("Information Technology");
        itDepartment.setCode("IT");

        facilitiesDepartment = new Department();
        facilitiesDepartment.setId(2L);
        facilitiesDepartment.setName("Facilities Management");
        facilitiesDepartment.setCode("FACILITIES");
    }

    private void setupTechnicians() {
        technicianBob = new TechnicianSupportStaff();
        technicianBob.setId(1L);
        technicianBob.setFullName("Bob Lee");
        technicianBob.setEmail("bob@um.edu.my");
        technicianBob.setDepartment(itDepartment);

        technicianSiti = new TechnicianSupportStaff();
        technicianSiti.setId(2L);
        technicianSiti.setFullName("Siti Ahmad");
        technicianSiti.setEmail("siti@um.edu.my");
        technicianSiti.setDepartment(itDepartment);

        technicianKumar = new TechnicianSupportStaff();
        technicianKumar.setId(3L);
        technicianKumar.setFullName("Kumar");
        technicianKumar.setEmail("kumar@um.edu.my");
        technicianKumar.setDepartment(facilitiesDepartment);
    }

    private void setupStudent() {
        student = new Student();
        student.setId(4L);
        student.setFullName("Lily Tan");
        student.setEmail("lily@siswa.um.edu.my");
    }

    private void setupTickets() {
        wifiTicket = new Ticket();
        wifiTicket.setId(1L);
        wifiTicket.setTitle("Cannot connect to WiFi");
        wifiTicket.setDescription("My laptop cannot connect to university WiFi");
        wifiTicket.setCategory("Network");
        wifiTicket.setStatus(TicketStatus.OPEN);
        wifiTicket.setPriority(TicketPriority.HIGH);
        wifiTicket.setSubmittedBy(student);
        wifiTicket.setSubmittedAt(LocalDateTime.now());

        projectorTicket = new Ticket();
        projectorTicket.setId(2L);
        projectorTicket.setTitle("Projector not working");
        projectorTicket.setDescription("Projector in LT1 won't turn on");
        projectorTicket.setCategory("Equipment");
        projectorTicket.setStatus(TicketStatus.OPEN);
        projectorTicket.setPriority(TicketPriority.MEDIUM);
        projectorTicket.setSubmittedBy(student);
        projectorTicket.setSubmittedAt(LocalDateTime.now());
    }

    // ========== FUNCTIONALITY 1: Automated Departmental Routing ==========

    @Test
    @DisplayName("F1: Should analyze ticket content and route to IT department based on keywords")
    void testAnalyzeDepartmentFromTicket_IT() {
        TypedQuery<Department> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Department.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query); // Support method chaining
        when(query.getResultList()).thenReturn(Arrays.asList(itDepartment, facilitiesDepartment));

        Department result = ticketService.analyzeDepartmentFromTicket(wifiTicket);

        assertEquals("IT", result.getCode(), "Should route to IT department based on 'wifi' keyword");
    }

    @Test
    @DisplayName("F1: Should route ticket to Facilities department based on keywords")
    void testAnalyzeDepartmentFromTicket_Facilities() {
        TypedQuery<Department> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Department.class))).thenReturn(query);
        when(query.setParameter(eq("code"), eq("FACILITIES"))).thenReturn(query);
        when(query.setParameter(eq("code"), eq("IT"))).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(facilitiesDepartment));

        Department result = ticketService.analyzeDepartmentFromTicket(projectorTicket);

        assertEquals("FACILITIES", result.getCode(), "Should route to Facilities based on 'projector' keyword");
    }

    @Test
    @DisplayName("F1: Should default to IT department when no keywords match")
    void testAnalyzeDepartmentFromTicket_DefaultToIT() {
        Ticket genericTicket = new Ticket();
        genericTicket.setTitle("General inquiry");
        genericTicket.setDescription("I have a question");
        genericTicket.setCategory("Other");

        TypedQuery<Department> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Department.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query); // Support method chaining
        when(query.getResultList()).thenReturn(Arrays.asList(itDepartment, facilitiesDepartment));

        Department result = ticketService.analyzeDepartmentFromTicket(genericTicket);

        assertEquals("IT", result.getCode(), "Should default to IT when no keywords match");
    }

    @Test
    @DisplayName("F1: Should route ticket and update ticket's assigned department")
    void testRouteTicketToDepartment() {
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        TypedQuery<Department> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Department.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query); // Support method chaining
        when(query.getResultList()).thenReturn(Arrays.asList(itDepartment, facilitiesDepartment));
        when(entityManager.merge(any(Ticket.class))).thenReturn(wifiTicket);

        Department result = ticketService.routeTicketToDepartment(1L);

        assertEquals(itDepartment, result);
        assertEquals(itDepartment, wifiTicket.getAssignedDepartment());
        verify(entityManager).merge(wifiTicket);
    }

    // ========== FUNCTIONALITY 2: Ticket Claiming and Self-Assignment ==========

    @Test
    @DisplayName("F2: Should allow technician to claim unassigned ticket")
    void testClaimTicket_Success() {
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(TechnicianSupportStaff.class, 1L)).thenReturn(technicianBob);
        when(entityManager.merge(any(Ticket.class))).thenReturn(wifiTicket);

        Ticket result = ticketService.claimTicket(1L, 1L);

        assertEquals(technicianBob, result.getAssignedTo());
        assertEquals(TicketStatus.ASSIGNED, result.getStatus());
        assertNotNull(result.getAssignedAt());
        verify(entityManager).merge(wifiTicket);
    }

    @Test
    @DisplayName("F2: Should throw exception when ticket is already assigned")
    void testClaimTicket_AlreadyAssigned() {
        wifiTicket.setAssignedTo(technicianBob);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.claimTicket(1L, 2L);
        });

        assertTrue(exception.getMessage().contains("already assigned"));
    }

    @Test
    @DisplayName("F2: Should throw exception when technician is not in ticket's department")
    void testClaimTicket_WrongDepartment() {
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(TechnicianSupportStaff.class, 3L)).thenReturn(technicianKumar);  // Kumar is in Facilities

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.claimTicket(1L, 3L);
        });

        assertTrue(exception.getMessage().contains("not in the ticket's assigned department"));
    }

    @Test
    @DisplayName("F2: Should return unassigned tickets for a department")
    void testGetUnassignedTicketsForDepartment() {
        wifiTicket.setAssignedDepartment(itDepartment);
        TypedQuery<Ticket> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Ticket.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(wifiTicket));

        List<Ticket> result = ticketService.getUnassignedTicketsForDepartment(1L);

        assertEquals(1, result.size());
        assertEquals(wifiTicket, result.get(0));
    }

    @Test
    @DisplayName("F2: Should return claimable tickets for technician in their department")
    void testGetClaimableTickets() {
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(TechnicianSupportStaff.class, 1L)).thenReturn(technicianBob);
        TypedQuery<Ticket> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Ticket.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(wifiTicket));

        List<Ticket> result = ticketService.getClaimableTickets(1L);

        assertEquals(1, result.size());
    }

    // ========== FUNCTIONALITY 3: Internal Re-assignment and Chaining ==========

    @Test
    @DisplayName("F3: Should reassign ticket to colleague in same department")
    void testReassignTicketInternally_Success() {
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);
        wifiTicket.setReassignmentCount(0);

        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(TechnicianSupportStaff.class, 2L)).thenReturn(technicianSiti);
        when(entityManager.merge(any(Ticket.class))).thenReturn(wifiTicket);

        Ticket result = ticketService.reassignTicketInternally(1L, 2L, "Network specialist needed");

        assertEquals(technicianSiti, result.getAssignedTo());
        assertEquals(technicianBob, result.getPreviousAssignee());
        assertEquals(1, result.getReassignmentCount());
        verify(entityManager).merge(wifiTicket);
    }

    @Test
    @DisplayName("F3: Should enforce reassignment limit")
    void testReassignTicketInternally_LimitReached() {
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setReassignmentCount(3);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.reassignTicketInternally(1L, 2L, "Test");
        });

        assertTrue(exception.getMessage().contains("maximum reassignment limit"));
    }

    @Test
    @DisplayName("F3: Should throw exception when reassigning to different department")
    void testReassignTicketInternally_DifferentDepartment() {
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(TechnicianSupportStaff.class, 3L)).thenReturn(technicianKumar);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.reassignTicketInternally(1L, 3L, "Test");
        });

        assertTrue(exception.getMessage().contains("different departments"));
    }

    @Test
    @DisplayName("F3: Should track assignment history with previous assignee")
    void testGetAssignmentHistory() {
        wifiTicket.setPreviousAssignee(technicianBob);
        wifiTicket.setAssignedTo(technicianSiti);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);

        List<TechnicianSupportStaff> history = ticketService.getAssignmentHistory(1L);

        assertEquals(2, history.size());
        assertTrue(history.contains(technicianBob));
        assertTrue(history.contains(technicianSiti));
    }

    @Test
    @DisplayName("F3: Should check if ticket can be reassigned")
    void testCanReassign() {
        wifiTicket.setReassignmentCount(2);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);

        boolean result = ticketService.canReassign(1L);

        assertTrue(result, "Should allow reassignment when count is below limit");
    }

    // ========== FUNCTIONALITY 4: Inter-Departmental Transfer ==========

    @Test
    @DisplayName("F4: Should transfer ticket to different department")
    void testTransferTicketToDepartment() {
        wifiTicket.setAssignedTo(technicianBob);
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(Department.class, 2L)).thenReturn(facilitiesDepartment);
        when(entityManager.merge(any(Ticket.class))).thenReturn(wifiTicket);

        Ticket result = ticketService.transferTicketToDepartment(1L, 2L, "Wrong department");

        assertEquals(facilitiesDepartment, result.getAssignedDepartment());
        assertNull(result.getAssignedTo(), "Should unassign technician when transferring");
        assertEquals(TicketStatus.OPEN, result.getStatus());
        assertEquals(technicianBob, result.getPreviousAssignee());
        verify(entityManager).merge(wifiTicket);
    }

    @Test
    @DisplayName("F4: Should throw exception when transferring to same department")
    void testTransferTicketToDepartment_SameDepartment() {
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(Department.class, 1L)).thenReturn(itDepartment);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.transferTicketToDepartment(1L, 1L, "Test");
        });

        assertTrue(exception.getMessage().contains("already assigned to this department"));
    }

    @Test
    @DisplayName("F4: Should transfer and assign to specific technician")
    void testTransferAndAssign() {
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(Department.class, 2L)).thenReturn(facilitiesDepartment);
        when(entityManager.find(TechnicianSupportStaff.class, 3L)).thenReturn(technicianKumar);
        when(entityManager.merge(any(Ticket.class))).thenReturn(wifiTicket);

        Ticket result = ticketService.transferAndAssign(1L, 2L, 3L, "Need facilities expertise");

        assertEquals(facilitiesDepartment, result.getAssignedDepartment());
        assertEquals(technicianKumar, result.getAssignedTo());
        assertEquals(TicketStatus.ASSIGNED, result.getStatus());
    }

    @Test
    @DisplayName("F4: Should throw exception when assigned technician not in target department")
    void testTransferAndAssign_TechnicianNotInDepartment() {
        wifiTicket.setAssignedDepartment(itDepartment);
        when(entityManager.find(Ticket.class, 1L)).thenReturn(wifiTicket);
        when(entityManager.find(Department.class, 2L)).thenReturn(facilitiesDepartment);
        when(entityManager.find(TechnicianSupportStaff.class, 1L)).thenReturn(technicianBob);  // Bob is in IT, not Facilities

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.transferAndAssign(1L, 2L, 1L, "Test");
        });

        assertTrue(exception.getMessage().contains("not in the target department"));
    }

    // ========== ANALYTICS ==========

    @Test
    @DisplayName("Analytics: Should calculate technician workload")
    void testGetTechnicianWorkload() {
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(5L);

        int workload = ticketService.getTechnicianWorkload(1L);

        assertEquals(5, workload);
    }

    @Test
    @DisplayName("Analytics: Should find least busy technician in department")
    void testFindLeastBusyTechnician() {
        // Mock technician query
        TypedQuery<TechnicianSupportStaff> techQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(TechnicianSupportStaff.class))).thenReturn(techQuery);
        when(techQuery.setParameter(anyString(), any())).thenReturn(techQuery);
        when(techQuery.setMaxResults(1)).thenReturn(techQuery);
        when(techQuery.getResultList()).thenReturn(Arrays.asList(technicianBob, technicianSiti));

        // Mock workload count query (used by getTechnicianWorkload)
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(3L);

        TechnicianSupportStaff result = ticketService.findLeastBusyTechnician(1L);

        assertNotNull(result);
        assertTrue(result.equals(technicianBob) || result.equals(technicianSiti));
    }

    @Test
    @DisplayName("Analytics: Should calculate department workload")
    void testGetDepartmentWorkload() {
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(10L);

        int workload = ticketService.getDepartmentWorkload(1L);

        assertEquals(10, workload);
    }
}
