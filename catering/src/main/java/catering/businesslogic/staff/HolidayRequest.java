package catering.businesslogic.staff;

import catering.util.DateUtils;
import catering.util.LogManager;
import catering.persistence.PersistenceManager;

import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class HolidayRequest {
    private static final Logger LOGGER = LogManager.getLogger(Collaborator.class);

    private Collaborator requestOwner;
    private int id;
    private Date startDate;
    private Date endDate;
    private boolean approval;
    private Date approvalDate;

    public static HolidayRequest create(Collaborator requestOwner, Date startDate, Date endDate){
        HolidayRequest hr = new HolidayRequest();
        hr.requestOwner = requestOwner;
        hr.startDate = startDate;
        hr.endDate = endDate;
        return hr;
    }

    public HolidayRequest() {}

    public Collaborator getRequestOwner() {
        return requestOwner;
    }

    public void setRequestOwner(Collaborator requestOwner) {
        this.requestOwner = requestOwner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isApproved() {
        return approval;
    }

    public void setApproval(boolean approval) {
        this.approval = approval;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public void holidayApprovation(boolean approval, Date approvalDate){
        this.approval = approval;
        this.approvalDate = approvalDate;
    }

    public static ArrayList<HolidayRequest> loadHolidayRequestsByUserId(int userId) {
        String query = "SELECT * FROM HolidaysRequests WHERE user_id = ?";

        ArrayList<HolidayRequest> requests = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs -> {
            HolidayRequest hr = new HolidayRequest();
            hr.id = rs.getInt("id");
            hr.requestOwner = Collaborator.loadCollaboratorById(rs.getInt("user_id"));

            String startDateStr = rs.getString("start_date");
            String endDateStr = rs.getString("end_date");

            if (startDateStr != null && !startDateStr.isEmpty()) {
                hr.startDate = DateUtils.safeValueOf(startDateStr);
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                hr.endDate = DateUtils.safeValueOf(endDateStr);
            }

            hr.approval = rs.getBoolean("approval");
            String approvalDateStr = rs.getString("approval_date");

            if (approvalDateStr != null && !approvalDateStr.isEmpty()) {
                hr.approvalDate = DateUtils.safeValueOf(approvalDateStr);
            }

            requests.add(hr);
        }, userId);

        return requests;
    }

    public static ArrayList<HolidayRequest> loadNewHolidaysRequests(){
        String query = "SELECT * FROM HolidaysRequests WHERE approval_date IS NULL";

        ArrayList<HolidayRequest> requests = new ArrayList<>();

        PersistenceManager.executeQuery(query, rs -> {
            HolidayRequest hr = new HolidayRequest();
            hr.id = rs.getInt("id");
            hr.requestOwner = Collaborator.loadCollaboratorById(rs.getInt("user_id"));

            String startDateStr = rs.getString("start_date");
            String endDateStr = rs.getString("end_date");

            if (startDateStr != null && !startDateStr.isEmpty()) {
                hr.startDate = DateUtils.safeValueOf(startDateStr);
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                hr.endDate = DateUtils.safeValueOf(endDateStr);
            }

            hr.approval = rs.getBoolean("approval");
            String approvalDateStr = rs.getString("approval_date");

            if (approvalDateStr != null && !approvalDateStr.isEmpty()) {
                hr.approvalDate = DateUtils.safeValueOf(approvalDateStr);
            }

            requests.add(hr);
        });

        return requests;
    }

    public static HolidayRequest loadRequestById(int hrId){
        String query = "SELECT * FROM HolidaysRequests WHERE id = ?";

        AtomicReference<HolidayRequest> holidayRequest = new AtomicReference<>();

        PersistenceManager.executeQuery(query, rs -> {
            HolidayRequest hr = new HolidayRequest();
            hr.id = rs.getInt("id");
            hr.requestOwner = Collaborator.loadCollaboratorById(rs.getInt("user_id"));

            String startDateStr = rs.getString("start_date");
            String endDateStr = rs.getString("end_date");

            if (startDateStr != null && !startDateStr.isEmpty()) {
                hr.startDate = DateUtils.safeValueOf(startDateStr);
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                hr.endDate = DateUtils.safeValueOf(endDateStr);
            }

            hr.approval = rs.getBoolean("approval");
            String approvalDateStr = rs.getString("approval_date");

            if (approvalDateStr != null && !approvalDateStr.isEmpty()) {
                hr.approvalDate = DateUtils.safeValueOf(approvalDateStr);
            }

            holidayRequest.set(hr);
        }, hrId);

        return holidayRequest.get();
    }

    public void addHolidayRequest(){
        String query = "INSERT INTO HolidaysRequests (user_id, start_date, end_date) VALUES (?, ?, ?)";

        PersistenceManager.executeUpdate(query, requestOwner.getId(), startDate, endDate);
        id = PersistenceManager.getLastId();
    }

    public void modifyHolidayRequest() {
        String query = "UPDATE HolidaysRequests SET start_date = ?, end_date = ?, approval = ?, approval_date = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, new Date(startDate.getTime()), new Date(endDate.getTime()), approval ? 1 : 0, approvalDate != null ? new Date(approvalDate.getTime()) : null, id);
    }

    public void removeHolidayRequest() {
        String query = "DELETE FROM HolidaysRequests WHERE id = ?";
        PersistenceManager.executeUpdate(query, id);
    }

    public static void removeHolidayrequestByUserId(int userId){
        String query = "DELETE FROM HolidaysRequests WHERE user_id = ?";
        PersistenceManager.executeUpdate(query, userId);
    }
}
