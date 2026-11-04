package com.example.campsitereservationsystem

class Permissions {
    private val allowedActions: Map<Role, Set<Action>> = mapOf(
        Role.ENTRANT to setOf(
            Action.BROWSE_CAMPSITES,
            Action.JOIN_WAITLIST,
            Action.LEAVE_WAITLIST,
            Action.RESERVE_CAMPSITE,
            Action.CANCEL_RESERVATION,
            Action.RATE_CAMPSITE
        ),
        Role.OWNER to setOf(
            Action.REGISTER_CAMPSITE,
            Action.EDIT_CAMPSITE,
            Action.CREATE_LOTTERY,
            Action.VIEW_WAITLIST,
            Action.REJECT_RESERVATION,
            Action.SEND_NOTIFICATION
        ),
        Role.ADMIN to setOf(
            Action.REMOVE_CAMPSITE,
            Action.REMOVE_PROFILE,
            Action.REMOVE_IMAGE,
            Action.REMOVE_OWNER,
            Action.VIEW_LOGS
        )
    )

    // Actions only for owners
    private val ownerRestricted: Set<Action> = setOf(
        Action.EDIT_CAMPSITE,
        Action.CREATE_LOTTERY,
        Action.SEND_NOTIFICATION,
        Action.REJECT_RESERVATION,
        Action.VIEW_WAITLIST
    )

    fun isAllowed(
        roles: Set<Role>,
        action: Action,
        userId: String? = null,
        resourceOwnerId: String? = null
    ): Boolean {
        return roles.any { role ->
            val permitted = allowedActions[role]?.contains(action) == true
            when {
                !permitted -> false
                role == Role.OWNER && action in ownerRestricted ->
                    resourceOwnerId != null && resourceOwnerId == userId

                else -> true
            }
        }
    }
    fun actionsFor(role: Role): Set<Action> = allowedActions[role].orEmpty()
}