package tqs.electro.electro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.electro.electro.entities.PaymentCard;

import java.util.UUID;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {
}
