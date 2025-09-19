package catering.businesslogic.event;

import catering.businesslogic.user.User;
import catering.persistence.EventPersistence;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;


public class Note {
    private static final Logger LOGGER = LogManager.getLogger(Note.class);
    private Event ev;
    private User owner;
    private String note;

    public Note(){}

    public static Note create(Event ev, User owner, String note){
        Note n = new Note();
        n.ev = ev;
        n.owner = owner;
        n.note = note;

        return n;
    }

    public Event getEv() {
        return ev;
    }

    public void setEv(Event ev) {
        this.ev = ev;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isOwner(User owner){
        return this.owner.equals(owner);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void addNote() {
        String query = "INSERT INTO Notes (event_id, owner_id, note) VALUES (?, ?, ?)";

        PersistenceManager.executeUpdate(query, ev.getId(), owner.getId(), note);
    }

    public void removeNote() {
        String query = "DELETE FROM Notes WHERE event_id = ?";

        PersistenceManager.executeUpdate(query, ev.getId());
    }

    public void modifyNote() {
        String query = "UPDATE Notes SET note = ? WHERE event_id = ?";

        PersistenceManager.executeUpdate(query, note, ev.getId());
    }

    public static Note loadNoteByEvent(Event event){
        String query = "SELECT * FROM Notes WHERE event_id = ?";

        AtomicReference<Note> n = new AtomicReference<>();

        PersistenceManager.executeQuery(query, rs -> {
            Note note = new Note();

            note.ev = event;
            note.owner = event.getOrg();
            note.note = rs.getString("note");

            n.set(note);
        },event.getId());

        return n.get();
    }
}
