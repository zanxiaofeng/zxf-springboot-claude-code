package contracts.users

import org.springframework.cloud.contract.spec.Contract

/**
 * Contract: Get user by ID - success scenario
 */
Contract.make {
    description "Get user by ID - should return 200 with user data"

    request {
        method GET()
        url '/api/v1/users/1'
    }

    response {
        status 200
        headers {
            header 'Content-Type', 'application/json'
        }
        body([
                code     : 'SUCCESS',
                data     : [
                        id       : 1,
                        username : 'test.user',
                        email    : 'test@example.com',
                        status   : 'ACTIVE',
                        createdAt: $(regex(iso8601WithOffset()))
                ],
                message  : null,
                timestamp: $(regex(iso8601WithOffset()))
        ])
    }
}
