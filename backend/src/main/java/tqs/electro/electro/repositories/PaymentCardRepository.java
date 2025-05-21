package tqs.electro.electro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.electro.electro.entities.PaymentCard;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
}
