package com.egov.queryservice;

import jakarta.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1")
public class MainRestController
{
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);
    @Autowired
    TokenService tokenService;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @GetMapping("get/profile/count")
    public ResponseEntity<?> getPofileCount()
    {
        profileRepository.count();
        return new ResponseEntity<>(profileRepository.count(), HttpStatus.OK);
    }

    @GetMapping("getby/firstname/location")
    public ResponseEntity<?> getByFirstnameAndLocation(@PathParam("firstname") String firstname,
                                                       @PathParam("location") String location)
    {
        String cachekey = firstname + location;
        // check the cache before you query the database
        String cachevalue = redisTemplate.opsForValue().get(cachekey);

        if(cachevalue == null)
        {
            List<Profile> profiles = new ArrayList<>();
            profiles =  profileRepository.findByFirstnameAndLocation(firstname, location);
            //Calculate the Query and persist it in the Cache
            redisTemplate.opsForValue().set(cachekey, String.valueOf(profiles.size()));
            return new ResponseEntity<>(profiles.size(), HttpStatus.OK);
        }
        else
        {
            // return from the cache | should be faster
            return new ResponseEntity<>(cachevalue, HttpStatus.CREATED);
        }
    }

}
