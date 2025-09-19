package catering.businesslogic.kitchen;

import static org.junit.jupiter.api.Assertions.*;

import catering.businesslogic.event.Event;
import catering.businesslogic.event.NoteException;
import catering.businesslogic.staff.*;
import catering.util.LogManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Note;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

@TestMethodOrder(OrderAnnotation.class)
public class StaffManagementTest {
    private static final Logger LOGGER = LogManager.getLogger(StaffManagementTest.class);

    private static CatERing app;
    private static User organizer;
    private static User owner;
    private static Event testEvent;
    private static Note testNote;
    private static StaffManager staffMgr;
    private static ArrayList<HolidayRequest> requests;

    @BeforeAll
    static void init(){
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup(){
        try {
             staffMgr = app.getStaffManager();

            // Set organizer
            organizer = User.loadUserByUsername("Chiara.CatERing");
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");
            LOGGER.info("Organizer loaded");

            // Set owner
            owner = User.loadUserByUsername("Francesca.CatERing");
            assertNotNull(owner, "Owner user should be loaded");
            assertTrue(owner.isOwner(), "User should have owner role");
            LOGGER.info("Owner loaded");

            // Set event
            testEvent = Event.loadByName("Gala Aziendale Annuale");
            assertNotNull(testEvent, "Test event should be loaded");
            LOGGER.info("Event loaded");

            // Set note
            testNote = testEvent.getNote();
            assertNotNull(testNote, "Test note should be loaded");
            LOGGER.info("Note loaded");

            // Set Holiday Requests
            requests = HolidayRequest.loadNewHolidaysRequests();
            assertNotNull(requests, "Requests should be loaded and not empty");
            LOGGER.info("Requests loaded");

            // Login
            app.getUserManager().fakeLogin(organizer.getUserName());
            assertEquals(organizer, app.getUserManager().getCurrentUser(), "Current user should be the organizer");

        }catch (UseCaseLogicException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    @Test
    @Order(1)
    void staffManagement(){
        LOGGER.info("Testing staff management");

        try{
            // Added a collaborator
            Collaborator collaborator = staffMgr.addCollaborator("Mirco", "mirco@email.com", new HashSet<>(Set.of(User.Role.SERVIZIO, User.Role.CUOCO)));
            assertNotNull(collaborator, "Collaborator should not be null");
            assertNotNull(collaborator.getUserName());
            assertTrue(collaborator.hasRole(User.Role.SERVIZIO));
            assertTrue(collaborator.hasRole(User.Role.CUOCO));
            LOGGER.info("Collaborator added");

            // Filled the info of the collaborator
            collaborator = staffMgr.fillOccasional(collaborator, "MRC03S", "Via di Mirco 23");
            assertTrue(((Occasional)collaborator).isInfoComplete(), "Info not complete");
            LOGGER.info("Collaborator info completed");

            // Modified collaborator
            collaborator = staffMgr.modifyCollaborator(collaborator, "Mirco", "mircolino@email.com", "MRC03S", "Via di Mirco 23", new HashSet<>(Set.of(User.Role.SERVIZIO, User.Role.CUOCO)), null, null);
            assertTrue(collaborator.getContact().contains("mircolino@email.com"), "Collaborator not modified");
            LOGGER.info("Collaborator modified");

            // Login with the owner
            app.getUserManager().fakeLogin(owner.getUserName());

            // Collaborator promoted
            collaborator = staffMgr.promoteOccasional(collaborator);
            assertFalse(collaborator.isOccasional(), "Collaborator should be a permanent");
            LOGGER.info("Collaborator promoted");

            // Check collaborator info
            collaborator = Collaborator.loadCollaboratorById(collaborator.getId());
            LOGGER.info("Collaborator loaded");
            LOGGER.info("\nUsername: " + collaborator.getUserName() + "\nName: " + collaborator.getName() + "\nContact: " + collaborator.getContact() + "\nCF: " + collaborator.getCF() + "\nAddress: " + collaborator.getAddress() + "\nAvailability: " + collaborator.isAvailable() + "\nHoliday Days: " + ((Permanent)collaborator).getHolidayDays() + "\nWork Hours: " + ((Permanent)collaborator).getWorkHours());
            LOGGER.info("\nRuoli:");
            collaborator.getRoles().forEach(role -> LOGGER.info(role.name()));

            // Loaded a collaborator
            collaborator = Collaborator.loadCollaboratorById(1); // 5 for error
            assertNotNull(collaborator, "Collaborator should not be null");
            assertTrue(collaborator.isOccasional());
            assertFalse(((Occasional)collaborator).isInfoComplete(), "The collaborator's info are not complete");
            LOGGER.info("Collaborator loaded");

            // Removed a collaborator
            staffMgr.removeCollaborator(collaborator);
            collaborator = Collaborator.loadCollaboratorById(1); // 5 error
            assertNull(collaborator, "Collaborator should be null");
            LOGGER.info("Collaborator removed");

            /* Error case for owner and yourself
            app.getUserManager().fakeLogin(organizer.getUserName());

            collaborator = Collaborator.loadCollaboratorById(6); // 8 is the owner
            staffMgr.removeCollaborator(collaborator);
            */

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        } catch (CollaboratorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(2)
    void noteManagement(){
        try{
            Note note;

            note = staffMgr.modifyNote(testEvent, testNote.getNote() + " Mirco è un ottimo collaboratore");
            assertNotNull(note, "Note should not be null");
            assertTrue(note.getNote().contains("Il Gala Aziendale Annuale ha eventato bene. Mirco è un ottimo collaboratore"), "Note not modified");
            note = Note.loadNoteByEvent(testEvent);
            assertTrue(note.getNote().contains("Il Gala Aziendale Annuale ha eventato bene. Mirco è un ottimo collaboratore"), "Note not modified");
            LOGGER.info("Note modified \nEvent Id: " + note.getEv().getId() + "\nNote: " + note.getNote());

            staffMgr.removeNote(note);
            assertNull(testEvent.getNote(), "Note should be null");
            LOGGER.info("Note removed");
            note = Note.loadNoteByEvent(testEvent);
            assertNull(note, "Note should be null");
            LOGGER.info("Note removed from database");

            note = staffMgr.addNote(testEvent, "nota di prova");
            assertNotNull(note, "Note should not be null");
            assertTrue(note.getNote().contains("nota di prova"), "Note not added");
            LOGGER.info("Note added");

            note = Note.loadNoteByEvent(testEvent);
            assertNotNull(note, "Note should not be null");
            assertTrue(note.getNote().contains("nota di prova"), "Note not added");
            LOGGER.info("Note correctly added to database");
            LOGGER.info("Event Id: " + note.getEv().getId() + "\nNote: " + note.getNote());

            organizer = User.loadUserByUsername("Giovanni.CatERing");
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");
            LOGGER.info("Organizer loaded");
            app.getUserManager().fakeLogin(organizer.getUserName());
            assertEquals(organizer, app.getUserManager().getCurrentUser(), "Current user should be the organizer");
            // error test correct
            note = staffMgr.modifyNote(testEvent, testNote.getNote() + " Mirco è un ottimo collaboratore");

            ArrayList<Event> eventBook = staffMgr.getEventBook();
            assertNotNull(eventBook, "Should be not null");
            LOGGER.info("Event book loaded");



        } catch (NoteException e) {
            fail(e.getMessage());
        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void holidayManagement(){
        try{
            // Login with the owner
            app.getUserManager().fakeLogin(owner.getUserName());

            HolidayRequest hr;
            Permanent cp;

            // Prima richiesta
            LOGGER.info("Request 1");
            hr = requests.get(0);
            cp = (Permanent) hr.getRequestOwner();
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            hr = staffMgr.holidaysApprovation(hr, true);
            LOGGER.info("Request approved Availability: " + cp.isAvailable());
            hr = HolidayRequest.loadRequestById(hr.getId());
            LOGGER.info("Request loaded");
            assertNotNull(hr, "Should not be null");
            assertNotNull(hr.getApprovalDate(), "Should not be null");
            assertTrue(hr.isApproved(), "Should be approved");

            cp = (Permanent) hr.getRequestOwner();
            assertFalse(cp.isAvailable(), "Should be not available");
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            requests.remove(0);

            // Seconda richiesta
            LOGGER.info("Request 2");
            hr = requests.get(0);
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            hr = staffMgr.holidaysApprovation(hr, true);
            LOGGER.info("Request denied");
            hr = HolidayRequest.loadRequestById(hr.getId());
            LOGGER.info("Request loaded");
            assertNotNull(hr, "Should not be null");
            assertFalse(hr.isApproved(), "Should not be approved");

            cp = (Permanent) hr.getRequestOwner();
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            requests.remove(0);

            // Terza richiesta
            LOGGER.info("Request 3");
            hr = requests.get(0);
            cp = (Permanent) hr.getRequestOwner();
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            hr = staffMgr.holidaysApprovation(hr, true);
            LOGGER.info("Request approved");
            hr = HolidayRequest.loadRequestById(hr.getId());
            LOGGER.info("Request loaded");
            assertNotNull(hr, "Should not be null");
            assertNotNull(hr.getApprovalDate(), "Should not be null");
            assertTrue(hr.isApproved(), "Should be approved");

            cp = (Permanent) hr.getRequestOwner();
            assertFalse(cp.isAvailable(), "Should be not available");
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            requests.remove(0);

            // Quarta richiesta
            LOGGER.info("Request 4");
            hr = requests.get(0);
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            hr = staffMgr.holidaysApprovation(hr, true);
            LOGGER.info("Request denied");
            hr = HolidayRequest.loadRequestById(hr.getId());
            LOGGER.info("Request loaded");
            assertNotNull(hr, "Should not be null");
            assertFalse(hr.isApproved(), "Should not be approved");

            cp = (Permanent) hr.getRequestOwner();
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            requests.remove(0);

            // Quinta richiesta
            LOGGER.info("Request 5");
            hr = requests.get(0);
            cp = (Permanent) hr.getRequestOwner();
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            hr = staffMgr.holidaysApprovation(hr, true);
            LOGGER.info("Request approved");
            hr = HolidayRequest.loadRequestById(hr.getId());
            LOGGER.info("Request loaded");
            assertNotNull(hr, "Should not be null");
            assertNotNull(hr.getApprovalDate(), "Should not be null");
            assertTrue(hr.isApproved(), "Should be approved");

            cp = (Permanent) hr.getRequestOwner();
            assertFalse(cp.isAvailable(), "Should be not available");
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            requests.remove(0);

            // Sesta richiesta
            LOGGER.info("Request 6");
            hr = requests.get(0);

            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            hr = staffMgr.holidaysApprovation(hr, false);
            LOGGER.info("Request denied");
            hr = HolidayRequest.loadRequestById(hr.getId());
            LOGGER.info("Request loaded");
            assertNotNull(hr, "Should not be null");
            assertFalse(hr.isApproved(), "Should not be approved");

            cp = (Permanent) hr.getRequestOwner();
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            requests.remove(0);

            // Settima richiesta
            LOGGER.info("Request 7");
            hr = requests.get(0);

            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            hr = staffMgr.holidaysApprovation(hr, true);
            LOGGER.info("Request denied");
            hr = HolidayRequest.loadRequestById(hr.getId());
            LOGGER.info("Request loaded");
            assertNotNull(hr, "Should not be null");
            assertFalse(hr.isApproved(), "Should not be approved");

            cp = (Permanent) hr.getRequestOwner();
            LOGGER.info("Holiday days: " + cp.getHolidayDays() + " Availability: " + cp.isAvailable());

            requests.remove(0);

            /* Ottava richiesta
            LOGGER.info("Request 8");
            hr = requests.get(0);

            hr = staffMgr.holidaysApprovation(hr, true);
            LOGGER.info("Request denied");

            requests.remove(0);*/

        } catch (CollaboratorException e) {
            fail(e.getMessage());
        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
