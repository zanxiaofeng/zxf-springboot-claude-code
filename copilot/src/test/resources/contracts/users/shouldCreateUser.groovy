package contracts.users

import org.springframework.cloud.contract.spec.Contract

/**
 * Contract: Create new user - success scenario
 */
Contract.make {
    description "Create new user - should return 201 with user data"

    request {
        method POST()
        url '/api/v1/users'
        headers {
            header 'Content-Type', 'application/json'
        }
        body([
                username: 'john.doe',
                email   : 'john.doe@example.com',
                password: 'SecurePass123!'
        ])
    }

    response {
        status 201
        headers {
            header 'Content-Type', 'application/json'
            header 'Location', $(regex('/api/v1/users/\\d+'))
        }
        body([
                code     : 'SUCCESS',
                data     : [
                        id       : $(regex(positiveInt())),
                        username : 'john.doe',
                        email    : 'john.doe@example.com',
                        status   : 'ACTIVE',
                        createdAt: $(regex(iso8601WithOffset()))
                ],
                message  : null,
                timestamp: $(regex(iso8601WithOffset()))
        ])
    }
}
