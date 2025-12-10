package com.egov.queryservice;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Consumer
{

    private final Logger logger = LoggerFactory.getLogger(Consumer.class);
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "profile-events", groupId = "query-consumer-group-1")
    public void consumeProfileEvents(String message) throws IOException
    {
        //analytics_counter.increment();
        ObjectMapper mapper  = new ObjectMapper();
        ProfileEvent datum =  mapper.readValue(message, ProfileEvent.class);
        // here goes the logic to refresh the cache which will be triggered once a message is consumed
        // sub-optimal logic to delete the cache entry based on firstname+location
        // slightly more optimized approach is to update the cache entry if it exists
        String cachekey =  datum.getProfile().getFirstname().concat(datum.getProfile().getLocation());
        //redisTemplate.delete(cachekey);
        String cachevalue = redisTemplate.opsForValue().get(cachekey);
        if(cachevalue != null)
        {
            Integer count = Integer.parseInt(cachevalue);
            if(datum.getType().equals("CREATE"))
            {
                count = count + 1;
            }
            else if(datum.getType().equals("DELETE"))
            {
                count = count - 1;
            }
            redisTemplate.opsForValue().set(cachekey,count.toString());
        }


        // query for this key will be recalculated the next time
        logger.info(String.format("#### -> Consumed message -> %s", datum));
    }

}

