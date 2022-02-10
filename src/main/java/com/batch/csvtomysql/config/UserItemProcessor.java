package com.batch.csvtomysql.config;

import com.batch.csvtomysql.entity.User;
import org.springframework.batch.item.ItemProcessor;

public class UserItemProcessor implements ItemProcessor<User,User> {


    @Override
    public User process(User user) throws Exception {
        return user;
    }
}
