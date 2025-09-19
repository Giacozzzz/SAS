package catering.businesslogic.user;

import catering.persistence.PersistenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class User {

    public static enum Role {
        CUOCO, CHEF, ORGANIZZATORE, SERVIZIO, PROPRIETARIO
    };

    private int id;
    private String username;
    private Set<Role> roles;

    public User() {
        this(null);
    }

    public User(String username) {
        this.username = username;
        this.roles = new HashSet<>();
    }

    public boolean isCook() {
        return roles.contains(Role.CUOCO);
    }

    public boolean isChef() {
        return roles.contains(Role.CHEF);
    }

    public boolean isWaiter(){
        return roles.contains(Role.SERVIZIO);
    }

    public boolean isOrganizer(){
        return roles.contains(Role.ORGANIZZATORE);
    }

    public boolean isOwner(){
        return roles.contains(Role.PROPRIETARIO);
    }

    public boolean isCollaborator(){
        return roles.contains(Role.CUOCO)
                || roles.contains(Role.CHEF)
                || roles.contains(Role.SERVIZIO );
    }

    public String getUserName() {
        return username;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(" ").append(username);

        if (!roles.isEmpty()) {
            sb.append(" : ");
            for (User.Role r : roles) {
                sb.append(r.toString()).append(" ");
            }
        }

        return sb.toString();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean removeRole(Role role) {
        return this.roles.remove(role);
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    public HashSet<Role> getRoles() {
        return new HashSet<>(this.roles);
    }

    public void setRoles(HashSet<Role> roles){
        this.roles = roles;
    }

    // STATIC METHODS FOR PERSISTENCE

    public static User loadUserById(int id) {
        AtomicReference<User> user = new AtomicReference<>();
        String query = "SELECT * FROM Users WHERE id = ?";

        PersistenceManager.executeQuery(query, rs -> {
            user.set(new User());
            user.get().id = id;
            user.get().username = rs.getString("username");
        }, id);


        if(user.get() != null) loadRolesForUser(user.get());

        return user.get();
    }

    public static User loadUserByUsername(String username) {
        AtomicReference<User> user = new AtomicReference<>();
        String query = "SELECT * FROM Users WHERE username = ?";

        PersistenceManager.executeQuery(query, rs -> {
            user.set(new User());
            user.get().username = username;
            user.get().id = rs.getInt("id");
        }, username);


        if(user.get() != null) loadRolesForUser(user.get());

        return user.get();
    }

    public static ArrayList<User> loadAllUsers() {
        String query = "SELECT * FROM Users";
        ArrayList<User> users = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs ->  {
                User u = new User();
                u.id = rs.getInt("id");
                u.username = rs.getString("username");

                // Load roles for this user
                loadRolesForUser(u);
                users.add(u);
        });

        return users;
    }

    // Helper method to load roles for a user
    private static void loadRolesForUser(User u) {
        String query = "SELECT * FROM UserRoles WHERE user_id = ?";

        PersistenceManager.executeQuery(query, rs ->  {
                switch (rs.getInt("role_id")) {
                    case 0:
                        u.roles.add(User.Role.CUOCO);
                        break;
                    case 1:
                        u.roles.add(User.Role.CHEF);
                        break;
                    case 2:
                        u.roles.add(User.Role.ORGANIZZATORE);
                        break;
                    case 3:
                        u.roles.add(User.Role.SERVIZIO);
                        break;
                    case 4:
                        u.roles.add(Role.PROPRIETARIO);
                        break;
                }
        }, u.id); // Pass u.id as parameter
    }

    /**
     * Saves a new user to the database
     * 
     * @return true if successful, false otherwise
     */
    public void addUser() {
        String query = "INSERT INTO Users (username) VALUES(?)";

        PersistenceManager.executeUpdate(query, username);
        id = PersistenceManager.getLastId();
        saveUserRoles();
    }

    /**
     * Updates an existing user in the database
     * 
     * @return true if successful, false otherwise
     */
    public void updateUser() {
        String query = "UPDATE Users SET username = ? WHERE id = ?";

        PersistenceManager.executeUpdate(query, username, id);

        // Update user roles
        saveUserRoles();
    }

    /**
     * Deletes a user from the database
     * 
     * @return true if successful, false otherwise
     */
    public void deleteUser() {
        // First delete user roles
        String query = "DELETE FROM UserRoles WHERE user_id = ?";
        PersistenceManager.executeUpdate(query, id);

        // Then delete user
        query = "DELETE FROM Users WHERE id = ?";
        PersistenceManager.executeUpdate(query, id);

    }

    /**
     * Saves user roles to the database
     */
    private void saveUserRoles() {
        if (id == 0)
            return; // User not saved yet

        // First delete existing roles
        String query = "DELETE FROM UserRoles WHERE user_id = ?";
        PersistenceManager.executeUpdate(query, id);

        // Then insert new roles
        for (Role role : roles) {
            int roleId = getRoleId(role);
            if(roleId != -1){
                query = "INSERT INTO UserRoles (user_id, role_id) VALUES(?, ?)";
                PersistenceManager.executeUpdate(query, id, roleId);
            }
        }
    }

    /**
     * Converts Role enum to string ID for database
     */
    private int getRoleId(Role role) {
        switch (role) {
            case CUOCO:
                return 0;
            case CHEF:
                return 1;
            case ORGANIZZATORE:
                return 2;
            case SERVIZIO:
                return 3;
            case PROPRIETARIO:
                return 4;
            default:
                return -1;
        }
    }

    /**
     * Determines if this user is equal to another object.
     * Two users are considered equal if they have the same ID or, if ID is 0,
     * the same username.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        User other = (User) obj;

        // If both users have valid IDs, compare by ID
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Otherwise, if either ID is 0, compare by username
        return this.username != null && this.username.equals(other.username);
    }

    /**
     * Generates a hash code for this user.
     * The hash code is based on ID if it's valid (> 0), or username otherwise.
     *
     * @return A hash code value for this user
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        // Use ID if it's valid
        if (id > 0) {
            result = prime * result + id;
        } else {
            // Otherwise use username
            result = prime * result + (username != null ? username.hashCode() : 0);
        }

        return result;
    }
}
