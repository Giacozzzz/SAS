package catering.persistence;

import catering.businesslogic.event.*;
import catering.businesslogic.menu.Menu;
import catering.util.LogManager;

import java.util.logging.Logger;

/**
 * Persistence class for Event operations.
 * Delegates to Event and Service classes for actual persistence.
 */
public class EventPersistence implements EventReceiver {
    private static final Logger LOGGER = LogManager.getLogger(EventPersistence.class);

    @Override
    public void updateEventCreated(Event event) {
        event.saveNewEvent();
    }

    @Override
    public void updateEventModified(Event event) {
        event.updateEvent();
    }

    @Override
    public void updateEventDeleted(Event event) {
        event.deleteEvent();
    }

    @Override
    public void updateServiceCreated(Event event, Service service) {
        service.saveNewService();
    }

    @Override
    public void updateServiceModified(Service service) {
        service.updateService();
    }

    @Override
    public void updateServiceDeleted(Service service) {
        service.deleteService();
    }

    @Override
    public void updateMenuAssigned(Service service, Menu menu) {
        service.assignMenuToService(menu);
    }

    @Override
    public void updateMenuRemoved(Service service) {
        service.removeMenuFromService();
    }

    @Override
    public void updateNoteAdded(Note n) {
        n.addNote();
    }

    @Override
    public void updateNoteRemoved(Note n) {
        n.removeNote();
    }

    @Override
    public void updateNoteModified(Note n){
        n.modifyNote();
    }

}