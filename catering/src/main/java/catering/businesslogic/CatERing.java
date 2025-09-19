package catering.businesslogic;

import catering.businesslogic.event.EventManager;
import catering.businesslogic.kitchen.KitchenTaskManager;
import catering.businesslogic.menu.MenuManager;
import catering.businesslogic.recipe.RecipeManager;
import catering.businesslogic.shift.ShiftManager;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.UserManager;
import catering.persistence.EventPersistence;
import catering.persistence.KitchenTaskPersistence;
import catering.persistence.MenuPersistence;
import catering.persistence.StaffPersistence;

public class CatERing {
    private static CatERing singleInstance;

    public static CatERing getInstance() {
        if (singleInstance == null) {
            singleInstance = new CatERing();
        }
        return singleInstance;
    }

    private MenuManager menuMgr;
    private RecipeManager recipeMgr;
    private UserManager userMgr;
    private EventManager eventMgr;
    private KitchenTaskManager kitchenTaskMgr;
    private StaffManager staffMgr;
    private ShiftManager shiftMgr;

    private MenuPersistence menuPersistence;
    private KitchenTaskPersistence kitchenTaskPersistence;
    private StaffPersistence staffPercistence;
    private EventPersistence eventPersistence;

    private CatERing() {
        menuMgr = new MenuManager();
        recipeMgr = new RecipeManager();
        userMgr = new UserManager();
        eventMgr = new EventManager();
        kitchenTaskMgr = new KitchenTaskManager();
        staffMgr = new StaffManager();
        shiftMgr = new ShiftManager(); // Add this line to initialize ShiftManager

        menuPersistence = new MenuPersistence();
        kitchenTaskPersistence = new KitchenTaskPersistence();
        staffPercistence = new StaffPersistence();
        eventPersistence = new EventPersistence();

        menuMgr.addEventReceiver(menuPersistence);
        kitchenTaskMgr.addEventReceiver(kitchenTaskPersistence);
        staffMgr.addEventReceiver(staffPercistence);
        eventMgr.addEventReceiver(eventPersistence);
    }

    public static void main(String[] args) {
        // Get the singleton instance which initializes all managers
        CatERing app = CatERing.getInstance();

        System.out.println("CatERing application initialized successfully.");

        // Log which managers are available
        System.out.println("Available managers:");
        System.out.println("- Menu Manager: " + (app.getMenuManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Recipe Manager: " + (app.getRecipeManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- User Manager: " + (app.getUserManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Event Manager: " + (app.getEventManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Kitchen Task Manager: " + (app.getKitchenTaskManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Staff Manager: " + (app.getStaffManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Shift Manager: " + (app.getShiftManager() != null ? "OK" : "NOT AVAILABLE"));
    }

    public KitchenTaskManager getKitchenTaskManager() {
        return kitchenTaskMgr; // Return the field that was properly initialized
    }

    public ShiftManager getShiftManager() {
        return shiftMgr;
    }

    public void setShiftManager(ShiftManager shiftMgr) {
        this.shiftMgr = shiftMgr;
    }

    public MenuManager getMenuManager() {
        return menuMgr;
    }

    public void setMenuManager(MenuManager menuMgr) {
        this.menuMgr = menuMgr;
    }

    public RecipeManager getRecipeManager() {
        return recipeMgr;
    }

    public void setRecipeManager(RecipeManager recipeMgr) {
        this.recipeMgr = recipeMgr;
    }

    public UserManager getUserManager() {
        return userMgr;
    }

    public void setUserManager(UserManager userMgr) {
        this.userMgr = userMgr;
    }

    public EventManager getEventManager() {
        return eventMgr;
    }

    public void setEventManager(EventManager eventMgr) {
        this.eventMgr = eventMgr;
    }

    public void setKitchenTaskManager(KitchenTaskManager kitchenTaskMgr) {
        this.kitchenTaskMgr = kitchenTaskMgr;
    }

    public void setStaffMgr(StaffManager staffMgr) {
        this.staffMgr = staffMgr;
    }

    public StaffManager getStaffManager() {
        return staffMgr;
    }
}
