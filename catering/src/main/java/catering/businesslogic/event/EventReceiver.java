package catering.businesslogic.event;

import catering.businesslogic.menu.Menu;

/**
 * Interface for receiving event-related notifications.
 * Implemented by classes that need to respond to event changes.
 */
public interface EventReceiver {

    public void updateEventCreated(Event event);

    public void updateEventModified(Event event);

    public void updateEventDeleted(Event event);

    public void updateServiceCreated(Event event, Service service);

    public void updateServiceModified(Service service);

    public void updateServiceDeleted(Service service);

    public void updateMenuAssigned(Service service, Menu menu);

    public void updateMenuRemoved(Service service);

    public void updateNoteAdded(Note note);

    public void updateNoteRemoved(Note note);

    public void updateNoteModified(Note note);

}