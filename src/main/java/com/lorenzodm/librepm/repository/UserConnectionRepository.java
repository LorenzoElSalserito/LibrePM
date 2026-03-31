package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnection, String> {

    // Trova connessione specifica
    @Query("SELECT c FROM UserConnection c WHERE " +
           "(c.requester.id = :u1 AND c.target.id = :u2) OR " +
           "(c.requester.id = :u2 AND c.target.id = :u1)")
    Optional<UserConnection> findConnection(@Param("u1") String userId1, @Param("u2") String userId2);

    // Trova tutte le connessioni accettate (amici)
    @Query("SELECT c FROM UserConnection c WHERE " +
           "(c.requester.id = :userId OR c.target.id = :userId) AND c.status = 'ACCEPTED'")
    List<UserConnection> findAcceptedByUserId(@Param("userId") String userId);

    // Trova richieste in arrivo (pending)
    @Query("SELECT c FROM UserConnection c WHERE c.target.id = :userId AND c.status = 'PENDING'")
    List<UserConnection> findPendingIncoming(@Param("userId") String userId);

    // Trova richieste inviate (pending)
    @Query("SELECT c FROM UserConnection c WHERE c.requester.id = :userId AND c.status = 'PENDING'")
    List<UserConnection> findPendingOutgoing(@Param("userId") String userId);
}
