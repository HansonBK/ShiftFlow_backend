package ca.hanson.shiftflow_backend.repo;

import ca.hanson.shiftflow_backend.entity.OpenShiftOffer;
import ca.hanson.shiftflow_backend.entity.OpenShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OpenShiftOfferRepository extends JpaRepository<OpenShiftOffer, Long> {

    List<OpenShiftOffer> findByStatusOrderByCreatedAtDesc(OpenShiftStatus status);

    List<OpenShiftOffer> findByCreatedByEmailOrderByCreatedAtDesc(String email);

    boolean existsByShiftIdAndStatusIn(Long shiftId, Collection<OpenShiftStatus> statuses);

    void deleteByShiftId(Long shiftId);
}