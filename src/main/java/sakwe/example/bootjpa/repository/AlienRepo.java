package sakwe.example.bootjpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sakwe.example.bootjpa.model.Alien;
import sakwe.example.bootjpa.model.UserAccount;

// JpaRepository provides the standard database operations for Alien.
public interface AlienRepo extends JpaRepository<Alien, Integer> {

    List<Alien> findByTech(String tech);

    List<Alien> findByAidGreaterThan(int aid);

    List<Alien> findByOwnerOrderByAidAsc(UserAccount owner);

    Optional<Alien> findByAidAndOwner(Integer aid, UserAccount owner);
}
