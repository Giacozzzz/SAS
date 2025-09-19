package catering.businesslogic.staff;

import catering.businesslogic.CatERing;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Collaborator extends User{
    private static final Logger LOGGER = LogManager.getLogger(Collaborator.class);

    private String name;
    private String contact;
    private String CF;
    private String address;
    private boolean availability;
    private ArrayList<Shift> shiftsTable;

    public Collaborator(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCF() {
        return CF;
    }

    public void setCF(String CF) {
        this.CF = CF;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public boolean isOccasional(){
        return this instanceof Occasional;
    }

    public ArrayList<Shift> getShiftsTableUser(){
        return Shift.getShiftsByUserId(getId());
    }

    public boolean isAssigned(){
        shiftsTable = Shift.getShiftsByUserId(getId());
        return !shiftsTable.isEmpty();
    }


    public void addCollaborator() {
        addUser();

        String query = "INSERT INTO Collaborators (user_id, name, contact) VALUES (?, ?, ?)";

        PersistenceManager.executeUpdate(query, getId(), name, contact);
    }

    public void removeCollaborator() {
        String query = "DELETE FROM Collaborators WHERE user_id = ?";

        PersistenceManager.executeUpdate(query, getId());

        HolidayRequest.removeHolidayrequestByUserId(getId());
        deleteUser();
    }

    public void fillInfo(){
        String query = "UPDATE Collaborators SET CF = ?, address = ? WHERE user_id = ?";

        PersistenceManager.executeUpdate(query, CF, address, getId());
    }

    public static Collaborator loadCollaboratorById(int userId) {
        User user = User.loadUserById(userId);
        if (user == null) {
            return null;
        }

        AtomicReference<Collaborator> collaborator= new AtomicReference<>();

        String query = "SELECT * FROM Collaborators WHERE user_id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            String name = rs.getString("name");
            String contact = rs.getString("contact");
            String CF = rs.getString("CF");
            String address = rs.getString("address");
            boolean availability = rs.getBoolean("availability");
            Integer holidayDays = rs.getObject("holiday_days") != null ? rs.getInt("holiday_days") : null;
            Integer workHours = rs.getObject("work_hours") != null ? rs.getInt("work_hours") : null;

            Collaborator co = Occasional.create(name, contact, user.getRoles());
            co.setUsername(user.getUserName());
            co.setId(userId);
            co.setAvailability(availability);
            if((CF != null && !CF.isEmpty()) && (address != null && !address.isEmpty())){
                co.setCF(CF);
                co.setAddress(address);
                if(holidayDays != null && workHours != null){
                    co = Permanent.create(co);
                    ((Permanent) co).setHolidayDays(holidayDays);
                    ((Permanent) co).setWorkHours(workHours);
                }
            }
            collaborator.set(co);
        }, userId);

        return collaborator.get();
    }
}
