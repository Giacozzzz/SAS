package catering.businesslogic.staff;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.*;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.user.User;
import catering.businesslogic.user.UserManager;
import catering.util.DateUtils;
import catering.util.LogManager;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

public class StaffManager {
    private static final Logger LOGGER = LogManager.getLogger(StaffManager.class);

    private ArrayList<StaffEventReceiver> eventReceivers;
    private Collaborator collaborator;

    public StaffManager(){
        eventReceivers = new ArrayList<>();
    }

    public Collaborator addCollaborator(String name, String contact, HashSet<User.Role> roles) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer() && !user.isOwner()){
            throw new UseCaseLogicException("User not authorized");
        }

        collaborator = Occasional.create(name, contact, roles);

        if(collaborator.hasRole(User.Role.PROPRIETARIO)){
            collaborator.removeRole(User.Role.PROPRIETARIO);
        }
        notifyCollaboratorAdded();

        return collaborator;
    }

    public void removeCollaborator(Collaborator co) throws UseCaseLogicException, CollaboratorException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer() && !user.isOwner()){
            throw new UseCaseLogicException("User not authorized");
        }
        if(co.isOwner() || co.getId() == user.getId()){
            throw new CollaboratorException("You cannot delete the owner or yourself");
        }
        if(co.isAssigned()){
            throw new CollaboratorException("Collaborator already assigned to a shift and cannot be deleted");
        }

        collaborator = co;
        notifyCollaboratorRemoved();
    }

    public Collaborator modifyCollaborator(Collaborator co, String name, String contact, String CF, String address, HashSet<User.Role> role, Integer holidayDays, Integer workingHours) throws UseCaseLogicException, CollaboratorException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer() && !user.isOwner()){
            throw new UseCaseLogicException("User not authorized");
        }

        if(name != null) {
            co.setName(name);
            co.setUsername(name + ".CatERing");
        }
        if(contact != null) co.setContact(contact);
        if(role != null) co.setRoles(role);
        if(co.isOccasional() && co.hasRole(User.Role.PROPRIETARIO)){
            co.removeRole(User.Role.PROPRIETARIO);
        }

        if(((Occasional) co).isInfoComplete()) {
            if (CF != null) co.setCF(CF);
            if (address != null) co.setAddress(address);
        }

        if(!co.isOccasional()){
            if(holidayDays != null) ((Permanent) co).setHolidayDays(holidayDays);
            if(workingHours != null) ((Permanent) co).setWorkHours(workingHours);
        }

        collaborator = co;
        notifyCollaboratorModified();

        return collaborator;
    }

    public Collaborator fillOccasional(Collaborator co, String CF, String address) throws UseCaseLogicException, CollaboratorException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer() && !user.isOwner()){
            throw new UseCaseLogicException("User not authorized");
        }
        if(!co.isOccasional() || ((Occasional)co).isInfoComplete()){
            throw new CollaboratorException("Info already completed");
        }

        if(CF != null && address != null) {
            co.setCF(CF);
            co.setAddress(address);
        }else{
            throw new CollaboratorException();
        }

        collaborator = co;
        notifyFilledOccasional();
        return collaborator;
    }

    public Collaborator promoteOccasional(Collaborator co) throws UseCaseLogicException, CollaboratorException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOwner()){
            throw new UseCaseLogicException("User not authorized");
        }
        if(!co.isOccasional()){
            throw new CollaboratorException("Collaborator is already a Permanent: he cannot be promoted");
        }

        collaborator = Permanent.create(co);
        ((Permanent) collaborator).setHolidayDays(0);
        ((Permanent) collaborator).setWorkHours(1000);

        notifyPromoteOccasional();
        return collaborator;
    }

    public HolidayRequest holidaysApprovation(HolidayRequest hr, boolean approval)
            throws UseCaseLogicException, CollaboratorException {

        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!user.isOwner()) throw new UseCaseLogicException("User not authorized");
        if (hr.getRequestOwner().isOccasional()){
            hr.removeHolidayRequest();
            throw new CollaboratorException("Collaborator is an Occasional");
        }

        hr.holidayApprovation(approval, new Date(System.currentTimeMillis()));

        Permanent p = (Permanent) hr.getRequestOwner();
        Date startDate = hr.getStartDate();
        Date endDate = hr.getEndDate();

        if (hr.isApproved() && !p.hasHolidays(startDate, endDate)) {
            hr.setApproval(false);
            LOGGER.info("No holidays available");
        }

        if (hr.isApproved() && hasShiftConflict(p, startDate, endDate)) {
            hr.setApproval(false);
            LOGGER.info("Shift conflict");
        }

        if (hr.isApproved() && hasHolidayConflict(p, hr)) {
            hr.setApproval(false);
            LOGGER.info("Holidays conflict");
        }

        if (hr.isApproved()) {
            p.holidayAssigned(false, DateUtils.dateDifference(startDate, endDate));
        }

        notifyHolidayApprovation(hr);
        return hr;
    }

    private boolean hasShiftConflict(Permanent p, Date start, Date end) {
        for (Shift shift : p.getShiftsTableUser()) {
            if (!shift.getDate().after(end) && !shift.getDate().before(start)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHolidayConflict(Permanent p, HolidayRequest hr) {
        for (HolidayRequest holiday : HolidayRequest.loadHolidayRequestsByUserId(p.getId())) {
            if(holiday.getId() != hr.getId() && holiday.isApproved()) {
                if (!holiday.getEndDate().before(hr.getStartDate()) && !holiday.getStartDate().after(hr.getEndDate())) {
                    return true;
                }
            }
        }
        return false;
    }


    public Note addNote(Event ev, String note) throws UseCaseLogicException, NoteException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer()){
            throw new UseCaseLogicException("User not authorized");
        }

        if(ev.getNote() != null){
            throw new NoteException("A note already exists for this event, so it cannot be added");
        }
        if(!ev.hasTerminated()){
            throw new NoteException("The event has not terminated yet");
        }

        return CatERing.getInstance().getEventManager().addNote(ev, note);
    }

    public void removeNote(Note n) throws UseCaseLogicException, NoteException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer()){
            throw new UseCaseLogicException("User not authorized");
        }

        if(n == null){
            throw new NoteException("No note exists for this event, so it cannot be deleted");
        }
        if(!n.isOwner(user)){
            throw new NoteException("User not authorized: you are not the author of this note");
        }

        CatERing.getInstance().getEventManager().removeNote(n);
    }

    public Note modifyNote(Event event, String note) throws UseCaseLogicException, NoteException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer()){
            throw new UseCaseLogicException("User not authorized");
        }

        Note n = event.getNote();
        if(n == null){
            throw new NoteException("No note exists for this event, so it cannot be modified");
        }
        if(!n.isOwner(user)){
            throw new NoteException("User not authorized: you are not the author of this note");
        }

        return CatERing.getInstance().getEventManager().modifyNote(n, note);
    }

    public ArrayList<Event> getEventBook() throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if(!user.isOrganizer() && !user.isOwner()){
            throw new UseCaseLogicException("User not authorized");
        }

        return CatERing.getInstance().getEventManager().getEventBook();
    }


    public void addEventReceiver(StaffEventReceiver rec){
        this.eventReceivers.add(rec);
    }

    public void removeEventReceiver(StaffEventReceiver rec){
        this.eventReceivers.remove(rec);
    }

    public void notifyCollaboratorAdded(){
        for(StaffEventReceiver receiver : eventReceivers){
            receiver.updateCollaboratorAdded(collaborator);
        }
    }

    public void notifyCollaboratorRemoved(){
        for(StaffEventReceiver receiver : eventReceivers){
            receiver.updateCollaboratorRemoved(collaborator);
        }
    }

    public void notifyCollaboratorModified(){
        for(StaffEventReceiver receiver : eventReceivers){
            receiver.updateCollaboratorModified(collaborator);
        }
    }

    public void notifyFilledOccasional(){
        for(StaffEventReceiver receiver : eventReceivers){
            receiver.updateFilledOccasional(collaborator);
        }
    }

    public void notifyPromoteOccasional(){
        for(StaffEventReceiver receiver : eventReceivers){
            receiver.updatePromoteOccasional(collaborator);
        }
    }

    public void notifyHolidayApprovation(HolidayRequest hr){
        for(StaffEventReceiver receiver : eventReceivers){
            receiver.updateHolidayApprovation(hr);
        }
    }
}
