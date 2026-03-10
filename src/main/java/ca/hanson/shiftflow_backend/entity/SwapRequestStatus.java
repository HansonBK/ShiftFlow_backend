package ca.hanson.shiftflow_backend.entitiy;

public enum SwapRequestStatus {
    PENDING,
    ACCEPTED_BY_TARGET,
    DECLINED_BY_TARGET,
    APPROVED,
    REJECTED,
    CANCELLED
}