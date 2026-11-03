package com.example.campsitereservationsystem

class Permissions {
    private val allowedActions: Map<Role., Set<Action>> = mapOf(
        Role.ENTRANT to setOf(
            Action.BROWSE_CAMPSITES,
            Action.JOIN_WAITLIST,
            Action.LEAVE_WAITLIST,
            Action.RESERVE_CAMPSITE,
            Action.CANCEL_RESERVATION,
            Action.RATE_CAMPSITE)
        ,

        Role.OWNER to setOf(
            Action.REGISTER_CAMPSITE,
            Action.EDIT_CAMPSITE,
            Action.VIEW_WAITLIST,
            Action.CREATE_LOTTERY,
            Action.SEND_NOTIFICATION,
            Action.REJECT_RESERVATION
        ),
        Role.ADMIN to setOf(
            Action.REMOVE_CAMPSITE,
            Action.REMOVE_PROFILE,
            Action.REMOVE_IMAGE,
            Action.REMOVE_OWNER,
            Action.VIEW_LOGS
        )
    )
}