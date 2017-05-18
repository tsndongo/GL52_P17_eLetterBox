package com.florian.Service.mongoRepo;

import com.florian.entity.Personne;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

/**
 * Created by Florian on 13/11/2016.
 */
@Repository
@RepositoryRestResource()
public interface PersonneRepository extends MongoRepository<Personne,String>,PersonneRepositoryCustom {
    @org.springframework.data.mongodb.repository.Query("{'reservationName':?0}")
    Collection<Personne> findByReservationName(String rn);

}
