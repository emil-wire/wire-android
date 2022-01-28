package com.wire.android.shared.session

import com.wire.android.core.exception.Failure
import com.wire.android.core.functional.Either

interface SessionRepository {
    suspend fun save(session: Session, current: Boolean): Either<Failure, Unit>

    suspend fun currentSession(): Either<Failure, Session>

    suspend fun userSession(userId: String): Either<Failure, Session>

    suspend fun accessToken(): Either<Failure, String>

    suspend fun newAccessToken(refreshToken: String): Either<Failure, Session>

    suspend fun doesCurrentSessionExist(): Either<Failure, Boolean>

    suspend fun setSessionCurrent(userId: String): Either<Failure, Unit>

    suspend fun setClientIdToUser(userId: String, clientId: String): Either<Failure, Unit>

    suspend fun currentClientId(): Either<Failure, String>
}