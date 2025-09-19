package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;

import java.util.HashSet;

public class Occasional extends Collaborator{
    public Occasional() {}

    public static Collaborator create(String name, String contact, HashSet<Role> role){
        Collaborator co = new Occasional();
        co.setName(name);
        co.setContact(contact);
        co.setRoles(role);
        co.setUsername(name + ".CateERing");

        return co;
    }

    public boolean isInfoComplete(){
        return getCF() != null && getAddress() != null;
    }


    public void modifyCollaborator() {
        String query = "UPDATE Collaborators SET name = ?, contact = ?, CF = ?, address = ? WHERE user_id = ?";

        PersistenceManager.executeUpdate(query, getName(), getContact(), getCF(), getAddress(), getId());

        updateUser();
    }
}
