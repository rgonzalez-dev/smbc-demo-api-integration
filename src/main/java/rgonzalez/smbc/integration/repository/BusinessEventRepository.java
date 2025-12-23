package rgonzalez.smbc.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rgonzalez.smbc.integration.model.BusinessEvent;

@Repository
public interface BusinessEventRepository extends JpaRepository<BusinessEvent, Long> {
}
