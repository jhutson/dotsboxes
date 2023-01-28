package com.hutsondev.authservice.repository;

import com.hutsondev.authservice.model.User;

public interface UserStore {

  User get(String id);
}
