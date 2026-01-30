package ca.hanson.shiftflow_backend.repo;

import ca.hanson.shiftflow_backend.entity.Shift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {



}
