package sakwe.example.bootjpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sakwe.example.bootjpa.model.Alien;

public interface AlienRepo extends JpaRepository<Alien, Integer> {

    List<Alien> findByTech(String tech);

    List<Alien> findByAidGreaterThan(int aid);
}
