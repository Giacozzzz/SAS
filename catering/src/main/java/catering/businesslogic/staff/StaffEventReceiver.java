package catering.businesslogic.staff;

public interface StaffEventReceiver {

    public void updateCollaboratorAdded(Collaborator co);

    public void updateCollaboratorRemoved(Collaborator co);

    public void updateCollaboratorModified(Collaborator co);

    public void updateFilledOccasional(Collaborator co);

    public void updatePromoteOccasional(Collaborator co);

    public void updateHolidayApprovation(HolidayRequest hr);
}
