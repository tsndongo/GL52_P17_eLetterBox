package com.florian.Service.mongoRepo;

import com.florian.entity.Personne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * Created by Florian on 13/11/2016.
 */

public class PersonneRepositoryImpl implements PersonneRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;



    @Override
    public boolean updatetestSauvegarde(String name, String token) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nom").is(name));
        Update update = new Update();
         update.set("note", token);
        mongoTemplate.upsert(query,update,Personne.class);

//        Personne userTest5 = mongoTemplate.findOne(query, Personne.class);
  //      System.out.println(userTest5.toString());
        return true;
    }
}
