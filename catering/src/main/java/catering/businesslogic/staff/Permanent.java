package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;
import catering.util.DateUtils;
import catering.util.LogManager;


import java.sql.Date;
import java.util.HashSet;
import java.util.logging.Logger;

public class Permanent extends Collaborator{
    private static final Logger LOGGER = LogManager.getLogger(Permanent.class);

    private HashSet<HolidayRequest> Holidays;
    private int holidayDays;
    private int workHours;

    public Permanent(){}

    public static Collaborator create(Collaborator co){
        Collaborator cp = new Permanent();
        cp.setId(co.getId());
        cp.setUsername(co.getUserName());
        cp.setName(co.getName());
        cp.setContact(co.getContact());
        cp.setCF(co.getCF());
        cp.setAddress(co.getAddress());
        cp.setAvailability(co.isAvailable());
        cp.setRoles(co.getRoles());
        return cp;
    }

    public void requestHoliday(Date startDate, Date endDate){
        HolidayRequest hr = HolidayRequest.create(this, startDate, endDate);
        Holidays.add(hr);
        hr.addHolidayRequest();

    }

    public HashSet<HolidayRequest> getHolidays() {
        return Holidays;
    }

    public int getHolidayDays() {
        return holidayDays;
    }

    public void setHolidayDays(int holidayDays) {
        this.holidayDays = holidayDays;
    }

    public int getWorkHours() {
        return workHours;
    }

    public void setWorkHours(int workHours) {
        this.workHours = workHours;
    }

    public boolean hasHolidays(Date startDate, Date endDate){
        int difference = DateUtils.dateDifference(startDate, endDate);
        return (holidayDays - difference) >= 0;
    }

    public void holidayAssigned(boolean availability, int holidayDays){
        this.setAvailability(availability);
        this.holidayDays -= holidayDays;
    }

    public boolean hasWorkingHours(){
        return getWorkHours() > 0;
    }

    public void modifyCollaborator() {
        String query = "UPDATE Collaborators SET name = ?, contact = ?, CF = ?, address = ?, availability = ?, holiday_days = ?, work_hours = ? WHERE user_id = ?";

        PersistenceManager.executeUpdate(query, getName(), getContact(), getCF(), getAddress(), isAvailable(), holidayDays, workHours, getId());

        updateUser();
    }
}
