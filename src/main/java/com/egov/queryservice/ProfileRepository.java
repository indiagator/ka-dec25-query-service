package com.egov.queryservice;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProfileRepository extends MongoRepository<Profile, String>
{
    List<Profile> findByFirstnameAndLocation(String firstname, String location);
}
