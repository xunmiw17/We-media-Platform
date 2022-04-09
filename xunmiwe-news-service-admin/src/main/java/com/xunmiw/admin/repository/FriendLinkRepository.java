package com.xunmiw.admin.repository;

import com.xunmiw.pojo.mo.FriendLinkMO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FriendLinkRepository extends MongoRepository<FriendLinkMO, String> {
}