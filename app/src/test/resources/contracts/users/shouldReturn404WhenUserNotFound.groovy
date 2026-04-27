package contracts.users

import org.springframework.cloud.contract.spec.Contract

/**
 * Contract: Get user by ID - not found scenario
 */
Contract.make {
    description "Get user by ID - should return 404 when user not found"

    request {
        method GET()
        url '/api/v1/users/999'
    }

    response {
        status 404
        headers {
            header 'Content-Type', 'application/json'
        }
        body([
                code     : '000003',
                data     : null,
                message  : 'Resource not found',
                timestamp: $(regex(iso8601WithOffset())),
                traceId  : $(regex('[a-f0-9]{16}'))
        ])
    }
}
