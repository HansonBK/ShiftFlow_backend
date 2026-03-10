package ca.hanson.shiftflow_backend.repo;

import ca.hanson.shiftflow_backend.entitiy.SwapRequest;
import ca.hanson.shiftflow_backend.entitiy.SwapRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {

    List<SwapRequest> findByTargetUserEmailOrderByCreatedAtDesc(String email);

    List<SwapRequest> findByRequesterEmailOrderByCreatedAtDesc(String email);

    boolean existsByRequesterShiftIdAndStatusIn(Long requesterShiftId, Collection<SwapRequestStatus> statuses);

    boolean existsByTargetShiftIdAndStatusIn(Long targetShiftId, Collection<SwapRequestStatus> statuses);
}