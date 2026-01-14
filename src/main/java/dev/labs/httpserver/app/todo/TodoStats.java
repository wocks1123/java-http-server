package dev.labs.httpserver.app.todo;

public class TodoStats {

    private final String userId;
    private int totalCount;
    private int completedCount;

    public TodoStats(String userId) {
        this.userId = userId;
        this.totalCount = 0;
        this.completedCount = 0;
    }

    public TodoStats(String userId, int totalCount, int completedCount) {
        this.userId = userId;
        this.totalCount = totalCount;
        this.completedCount = completedCount;
    }

    public void increaseTotalCount() {
        this.totalCount++;
    }

    public void increaseCompletedCount() {
        this.completedCount++;
    }

    public void decreaseTotalCount() {
        if (this.totalCount > 0) {
            this.totalCount--;
        }
    }

    public void decreaseCompletedCount() {
        if (this.completedCount > 0) {
            this.completedCount--;
        }
    }

    public String getUserId() {
        return userId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }
}
