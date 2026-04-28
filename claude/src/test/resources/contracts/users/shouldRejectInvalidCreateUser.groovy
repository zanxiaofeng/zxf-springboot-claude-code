package contracts.users

import org.springframework.cloud.contract.spec.Contract

/**
 * Contract: Create user - validation error scenario
 */
Contract.make {
    description "Create user - should return 400 when request is invalid"

    request {
        method POST()
        url '/api/v1/users'
        headers {
            header 'Content-Type', 'application/json'
        }
        body([
                username: '',
                email   : 'invalid-email',
                password: 'short'
        ])
    }

    response {
        status 400
        headers {
            header 'Content-Type', 'application/json'
        }
        body([
                code     : '002001',
                data     : null,
                message  : 'Request validation failed',
                timestamp: $(regex(iso8601WithOffset())),
                traceId  : $(regex('[a-f0-9]{16}')),
                errors   : [
                        [
                                field  : 'email',
                                message: 'Must be a valid email'
                        ]
                ]
        ])
    }
}
