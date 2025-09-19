package catering.persistence;

import catering.businesslogic.staff.*;

public class StaffPersistence implements StaffEventReceiver {

    public void updateCollaboratorAdded(Collaborator co){
        co.addCollaborator();
    }

    public void updateCollaboratorRemoved(Collaborator co){
        co.removeCollaborator();
    }

    public void updateCollaboratorModified(Collaborator co) {
        if (co instanceof Permanent) {
            ((Permanent) co).modifyCollaborator();
        } else if (co instanceof Occasional) {
            ((Occasional) co).modifyCollaborator();
        }
    }


    public void updateFilledOccasional(Collaborator co) {
        co.fillInfo();
    }

    public void updatePromoteOccasional(Collaborator co) {
        ((Permanent) co).modifyCollaborator();
    }

    public void updateHolidayApprovation(HolidayRequest hr) {
            hr.modifyHolidayRequest();
            if(hr.isApproved()){
                updateCollaboratorModified(hr.getRequestOwner());
            }
    }

}
